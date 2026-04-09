# Load Testing Setup (k6)

This project now includes two k6 scripts:

- `performance/loadtest.ts`: baseline test for 100+ concurrent users.
- `performance/stresstest.ts`: ramp test that pushes until performance starts degrading.

## 1) Prerequisites

Install k6 on macOS:

```bash
brew install k6
```

Start required services:

1. PostgreSQL running and initialized with `database/init.sql`
2. Backend running on `http://localhost:8080`

## 2) Baseline test (100 concurrent users)

From `src/backend/src/test/performance` run:

```bash
k6 run -e BASE_URL=http://localhost:8080 -e VUS_BROWSE=80 -e VUS_RESERVATIONS=30 -e TEST_DURATION=5m loadtest.ts
```

This simulates 110 concurrent users total and validates:

- `http_req_failed < 1%`
- `p95 < 500 ms`
- `p99 < 1000 ms`
- checks pass rate > 99%

## 3) Stress test (find breaking point)

From `src/backend/src/test/performance` run:

```bash
k6 run -e BASE_URL=http://localhost:8080 stresstest.ts
```

Stages:

- 50 users for 2m
- 100 users for 3m
- 150 users for 3m
- 200 users for 3m
- ramp down to 0 for 2m

## 4) Optional environment variables

If login credentials differ from seeded DB values:

```bash
-e LOADTEST_CONTACT=user1@example.com
-e LOADTEST_PASSWORD=user123
```

## 5) Result interpretation

If thresholds fail, check:

- backend CPU/memory
- database connection pool and slow queries
- error distribution by endpoint/status code

Tune backend and rerun until baseline passes consistently.

## 6) Automatic result export

Each run automatically writes summary files in `src/backend/src/test`:

- `performance/loadtest-summary.json`
- `performance/loadtest-summary.csv`
- `performance/stresstest-summary.json`
- `performance/stresstest-summary.csv`

The CSV contains one row per k6 metric with common stats (avg, p95, p99, rate, count, etc.).

## 7) CI gate and artifacts

The CI workflow includes:

- `load-test-gate`: runs on push/PR and fails the pipeline if load thresholds are not met.
- `stress-test`: runs on manual workflow dispatch and uploads stress artifacts.

Both jobs upload generated JSON/CSV files as GitHub Actions artifacts.
