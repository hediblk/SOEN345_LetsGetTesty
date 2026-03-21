# SOEN345_LetsGetTesty
SOEN 345 Team Project - Winter 2026

## DB setup
The PostgreSQL setup script is in `src/backend/database/init.sql`

Create the database once (have postgreSQL installed and running):
```bash
createdb lets_get_testy
```

Run the init script to create tables and insert sample data:
```bash
psql -U postgres -d lets_get_testy -f src/backend/database/init.sql
```

Check if it worked by connecting to the database:
```bash
psql -U postgres -d lets_get_testy
SELECT * FROM users;
```
It should show the 2 dummy users inserted.

Exit with `\q`

## Run the backend
```bash
cd src/backend
./mvnw spring-boot:run
```

The backend starts on `http://localhost:8080`.

## Run the frontend
Open a second terminal
```bash
cd src/frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173`.
"backend running" should be displayed
