import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const CONTACT = __ENV.LOADTEST_CONTACT || "user1@example.com";
const PASSWORD = __ENV.LOADTEST_PASSWORD || "user123";

function toCell(value: unknown): string {
  if (value === null || value === undefined) {
    return "";
  }
  const text = String(value);
  if (text.includes(",") || text.includes("\n") || text.includes("\"")) {
    return `"${text.replace(/\"/g, '""')}"`;
  }
  return text;
}

function toSummaryCsv(data: any): string {
  const header = ["metric", "type", "avg", "min", "med", "max", "p90", "p95", "p99", "rate", "count", "value"];
  const rows = [header.join(",")];

  for (const [metricName, metric] of Object.entries<any>(data.metrics || {})) {
    const values = metric.values || {};
    const row = [
      metricName,
      metric.type || "",
      values.avg,
      values.min,
      values.med,
      values.max,
      values["p(90)"],
      values["p(95)"],
      values["p(99)"],
      values.rate,
      values.count,
      values.value,
    ].map(toCell);
    rows.push(row.join(","));
  }

  return `${rows.join("\n")}\n`;
}

export const options = {
  stages: [
    { duration: "2m", target: 50 },
    { duration: "3m", target: 100 },
    { duration: "3m", target: 150 },
    { duration: "3m", target: 200 },
    { duration: "2m", target: 0 },
  ],
  thresholds: {
    checks: ["rate>0.98"],
    http_req_failed: ["rate<0.02"],
    http_req_duration: ["p(95)<800", "p(99)<1500"],
  },
};

function login() {
  const payload = JSON.stringify({
    contact: CONTACT,
    password: PASSWORD,
  });

  const res = http.post(`${BASE_URL}/api/auth/login`, payload, {
    headers: { "Content-Type": "application/json" },
  });

  const loginOk = check(res, {
    "login status 200": (r) => r.status === 200,
    "login returns token": (r) => Boolean(r.json("token")),
  });

  if (!loginOk) {
    return { Authorization: "" };
  }

  return {
    Authorization: `Bearer ${res.json("token")}`,
  };
}

export default function () {
  const headers = login();

  const eventsRes = http.get(`${BASE_URL}/api/events`, { headers });
  check(eventsRes, { "GET /api/events is 200": (r) => r.status === 200 });

  const reservationsRes = http.get(`${BASE_URL}/api/reservations/user/1`, { headers });
  check(reservationsRes, {
    "GET /api/reservations/user/1 is 200": (r) => r.status === 200,
  });

  sleep(Math.random() * 0.6 + 0.1);
}

export function handleSummary(data: any) {
  return {
    "stresstest-summary.json": JSON.stringify(data, null, 2),
    "stresstest-summary.csv": toSummaryCsv(data),
  };
}
