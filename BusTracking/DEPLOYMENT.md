# Deploying to Railway

This repo has two deployable services: `backend` (Spring Boot) and `frontend`
(Vite/React, served by nginx). Each has its own `Dockerfile` and `railway.toml`,
so create **two separate Railway services** pointing at the same repo with
different root directories.

## 1. Rotate the DTC API key

The key that used to live in `application-local.properties` was committed to
git history (`LYDla3lyGiBHzBuK2dYqJUhbiLA407oR`) and a newer key was briefly
hardcoded into several properties files in this working tree. **Get a fresh
key from the OTD/DTC portal** and treat both old ones as burned — don't reuse
them as `DTC_API_KEY` below.

## 2. Backend service

1. New Railway service → root directory `backend` → builder auto-detects
   `Dockerfile`.
2. Add plugins: **PostgreSQL** and **Redis** (Railway → "New" → Database).
   Railway injects `PGHOST/PGPORT/PGDATABASE/PGUSER/PGPASSWORD` and
   `REDIS_URL` automatically into the backend service if you reference the
   plugin, or you can wire them manually as service variables.
3. Set these environment variables on the backend service:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DTC_API_KEY=<your rotated key>`
   - `CORS_ALLOWED_ORIGINS=https://<your-frontend-service>.up.railway.app`
     (add your custom domain too once you have one, comma-separated)
   - Railway sets `PORT` automatically — the app already binds to it.
4. Deploy. Watch the build logs for the LFS-pointer check — if it fails with
   "GTFS.zip is a Git LFS pointer, not the real archive", Railway's checkout
   didn't fetch LFS content; enable Git LFS in the service's source settings
   (or switch to the "move GTFS out of the repo" approach if Railway has no
   such toggle for your plan).
5. Health check is wired to `/actuator/health` via `railway.toml`.

## 3. Frontend service

1. New Railway service → root directory `frontend` → builder auto-detects
   `Dockerfile`.
2. Set a **build-time** variable (Railway calls these "Build Variables", not
   regular service variables — Vite bakes `VITE_*` values in at build time):
   - `VITE_API_URL=https://<your-backend-service>.up.railway.app/api`
3. Deploy. nginx serves the static build and listens on Railway's `$PORT`.

## 4. Order of operations

Deploy the backend first, copy its public Railway URL, then set
`VITE_API_URL` on the frontend and deploy it. Once the frontend has its own
public URL, go back and set `CORS_ALLOWED_ORIGINS` on the backend to match,
then redeploy the backend (env var changes require a redeploy to take effect).

## 5. What's intentionally different from local dev

- No Kafka in production: `DtcIngestionService` writes straight to the
  Redis-backed `BusLocationStore` (see `DirectBusLocationPublisher`). Kafka is
  still used for the `local`/`dev` profiles via `backend/compose.yaml` if you
  want to run the full pipeline locally.
- `spring.jpa.hibernate.ddl-auto=update` is kept as-is for now — fine for a
  small solo project, but if this grows, switch to migrations (Flyway/Liquibase).
- **GTFS import scope**: local/dev/demo import a small ~6km box around
  Connaught Place for fast iteration. Prod (`gtfs.import.radius-degrees=0`)
  imports the *full* Delhi/NCR feed (~10k stops, 3.7M stop_times rows) so real
  users anywhere in the city get coverage. This only happens once, on first
  boot after a fresh Postgres — expect the first deploy's startup to take
  noticeably longer while it batch-inserts everything. The app is still
  reachable during that time (the HTTP server starts before the import
  finishes), but `/api/routes`, `/api/stops`, `/api/buses/nearby` will return
  empty/partial results until it completes.
