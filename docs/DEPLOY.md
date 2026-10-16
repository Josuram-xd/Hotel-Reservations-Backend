# Deploying the backend on Railway

The app is a single container built from the `Dockerfile` (Maven build, then a Java 21 JRE image).
Data lives in memory, so every restart or redeploy starts with no reservations.

## Steps

1. In [Railway](https://railway.com), create a **New Project** and choose **Deploy from GitHub repo**, then pick `Hotel-Reservations-Backend`.
2. Railway detects the `Dockerfile` and builds it. `railway.json` already sets the health check to `/actuator/health`.
3. In the service **Variables** tab add:
   - `CORS_ALLOWED_ORIGINS` = the Vercel URL of the frontend, for example `https://hotel-aurora.vercel.app`.
     Several origins can be separated by commas. No trailing slash.
   - `PORT` is injected by Railway automatically. Do not set it.
4. In **Settings > Networking**, click **Generate Domain** to get the public URL, for example `https://hotel-reservations-backend.up.railway.app`.
5. In Vercel, set the frontend variable `VITE_API_URL` to that URL (no trailing slash) and redeploy the frontend.

## Smoke test (task 7.4)

Replace `$API` with the Railway URL and run, in order:

```bash
curl "$API/actuator/health"                                   # {"status":"UP"}
curl "$API/api/hotel/disponibilidad?checkIn=2026-12-01&checkOut=2026-12-03"   # 15 rooms
```

Then run the full lifecycle (create a reservation, add a service, check in, check out, read the invoice)
with the `curl` examples of the README. Finally open the frontend and repeat the flow from the browser;
if the browser blocks the calls, `CORS_ALLOWED_ORIGINS` does not match the frontend origin exactly.
