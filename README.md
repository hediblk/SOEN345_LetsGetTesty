# SOEN345_LetsGetTesty
SOEN 345 Team Project - Winter 2026

## Shared database on Supabase
The backend uses a shared Supabase Postgres database.
In `src/backend`:
```bash
cp .env_example .env
```

Paste the secret values from the shared Supabase project into `.env`:
```dotenv
SPRING_DATASOURCE_URL="paste-the-full-supabase-session-pooler-jdbc-string-here"
APP_AUTH_JWT_SECRET="paste-the-shared-jwt-secret-here"

# Optional: only if you want email notifications to send real mail
APP_NOTIFICATION_EMAIL=your.name@gmail.com
APP_NOTIFICATION_EMAIL_PASSWORD=your-16-character-app-password
```

Keep `SPRING_DATASOURCE_URL` quoted because the Supabase JDBC string contains `&`.

## Run the backend

In `src/backend`:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

The backend starts on `http://localhost:8080`.

## Run the frontend

Open a second terminal:

```bash
cd src/frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173` and continues to call the backend on `http://localhost:8080`.
