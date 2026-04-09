import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const CONTACT = __ENV.LOADTEST_CONTACT || "user1@example.com";
const PASSWORD = __ENV.LOADTEST_PASSWORD || "user123";
const VUS_BROWSE = Number(__ENV.VUS_BROWSE || 80);
const VUS_RESERVATIONS = Number(__ENV.VUS_RESERVATIONS || 30);
const TEST_DURATION = __ENV.TEST_DURATION || "5m";

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
  scenarios: {
    // Simulates most users reading the event list.
    browse_events: {
      executor: "constant-vus",
      vus: VUS_BROWSE,
      duration: TEST_DURATION,
      exec: "browseEvents",
    },
    // Simulates users reading their own reservations.
    reservation_reads: {
      executor: "constant-vus",
      vus: VUS_RESERVATIONS,
      duration: TEST_DURATION,
      exec: "readReservations",
    },
  },
  thresholds: {
    checks: ["rate>0.99"],
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<500", "p(99)<1000"],
  },
};

function getAuthHeaders() {
  const payload = JSON.stringify({
    contact: CONTACT,
    password: PASSWORD,
  });

  const res = http.post(`${BASE_URL}/api/auth/login`, payload, {
    headers: { "Content-Type": "application/json" },
  });

  const loginOk = check(res, {
    "login 200": (r) => r.status === 200,
    "login has token": (r) => Boolean(r.json("token")),
  });

  if (!loginOk) {
    return { Authorization: "" };
  }

  return {
    Authorization: `Bearer ${res.json("token")}`,
  };
}

export function browseEvents() {
  const headers = getAuthHeaders();
  const res = http.get(`${BASE_URL}/api/events`, {
    headers,
  });

  check(res, {
    "events 200": (r) => r.status === 200,
    "events response is array": (r) => Array.isArray(r.json()),
  });

  sleep(Math.random() * 0.4 + 0.1);
}

export function readReservations() {
  const headers = getAuthHeaders();
  const res = http.get(`${BASE_URL}/api/reservations/user/1`, {
    headers,
  });

  check(res, { "user reservations 200": (r) => r.status === 200 });
  sleep(Math.random() * 0.5 + 0.1);
}

export function handleSummary(data: any) {
  return {
    "loadtest-summary.json": JSON.stringify(data, null, 2),
    "loadtest-summary.csv": toSummaryCsv(data),
  };
}