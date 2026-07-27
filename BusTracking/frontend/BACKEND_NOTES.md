# DTC Bus Tracker — Frontend Requirements

This folder is a placeholder for the React frontend. No frontend code has been
written yet — this document is the spec your friend should build from.

> Verified against the actual backend code in `../backend` on 2026-07-27.
> A couple of details in the original draft didn't match the real API —
> those are called out below.

## 1. Setup

```bash
npx create-react-app dtc-frontend
cd dtc-frontend
npm install axios leaflet react-leaflet
npm start
```

Or with Vite (lighter, faster):

```bash
npm create vite@latest dtc-frontend -- --template react
cd dtc-frontend
npm install axios leaflet react-leaflet
npm run dev
```

## 2. Backend

Running at `http://localhost:8080/api` (Spring Boot, port 8080, see
`backend/src/main/resources/application.properties`).

**CORS is now configured** (`backend/src/main/java/com/dtc/bus_tracker/config/CorsConfig.java`),
allowing `http://localhost:3000` (CRA) and `http://localhost:5173` (Vite) to
call `/api/**` directly — no proxy needed. If your friend runs the dev
server on a different port, add it to `allowedOrigins` in that file.

## 3. Real API endpoints (confirmed from source)

```
GET /api/buses/nearby?lat={lat}&lng={lng}&radiusMeters={m}&limit={n}
GET /api/routes
GET /api/routes/{id}
GET /api/routes/search?query={text}&limit={n}
GET /api/stops?page={p}&size={s}
GET /api/stops/{id}
GET /api/stops/nearby?lat={lat}&lng={lng}&radiusMeters={m}&limit={n}
```

Note the param is **`radiusMeters`**, not `radius` — defaults to `1000` on
`/buses/nearby` and `/stops/nearby` if omitted. `limit` defaults to `10`.

There's currently **no endpoint that returns "routes serving a stop"** — the
"Stop Details" page idea (click stop → show routes + upcoming buses) isn't
supported by the backend yet. Either scope that out for now or ask the
backend to add it (e.g. `GET /api/stops/{id}/routes`).

## 4. Response shapes (from the actual DTOs)

`GET /api/buses/nearby`:
```json
{
  "vehicleId": "DL1PD6882",
  "routeCode": "2179",
  "routeName": "...",
  "stopName": "Shivaji Stadium",
  "distanceToStop": 80.5,
  "distanceToUser": 150.0,
  "etaMinutes": 2,
  "busLatitude": 28.6325,
  "busLongitude": 77.2170
}
```

`GET /api/routes` / `/api/routes/search` / `/api/routes/{id}`:
```json
{
  "id": 1,
  "routeCode": "2179",
  "name": "...",
  "stopIds": [1, 2, 3],
  "stopNames": ["Stop A", "Stop B", "Stop C"]
}
```

`GET /api/stops/nearby` / `/api/stops`:
```json
{
  "id": 1,
  "stopId": "...",
  "name": "Shivaji Stadium",
  "latitude": 28.6325,
  "longitude": 77.2170,
  "sequenceNumber": 1
}
```

## 5. Pages to build

**A. Main Map Page (most important)**
- Map centered on Delhi (28.6139, 77.2090)
- Get user location via browser Geolocation API
- Fetch nearby buses every 10 seconds
- Show buses as markers; click marker → popup with route, ETA, vehicle ID

**B. Route Search**
- Search box, autocomplete against `/api/routes/search?query=`
- Click a route → highlight its stops on the map (draw path from `stopIds`
  order — backend doesn't return lat/lng per stop on the route directly, so
  you'll need to cross-reference stop IDs against `/api/stops/{id}` or fetch
  `/api/stops` and join client-side)

**C. Stop Details**
- Click a stop → show upcoming buses for it (filter `/api/buses/nearby`
  results by `stopName`, since there's no dedicated "buses at this stop"
  endpoint yet)

## 6. Example: fetch and render buses on the map

```javascript
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import axios from 'axios';
import { useEffect, useState } from 'react';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

const API = 'http://localhost:8080/api';

function BusMap() {
  const [buses, setBuses] = useState([]);
  const [userLocation, setUserLocation] = useState(null);

  useEffect(() => {
    navigator.geolocation.getCurrentPosition(pos => {
      setUserLocation({ lat: pos.coords.latitude, lng: pos.coords.longitude });
    });
  }, []);

  useEffect(() => {
    if (!userLocation) return;
    const fetchBuses = () => {
      axios.get(`${API}/buses/nearby`, {
        params: { lat: userLocation.lat, lng: userLocation.lng, radiusMeters: 2000, limit: 20 }
      }).then(res => setBuses(res.data));
    };
    fetchBuses();
    const interval = setInterval(fetchBuses, 10000);
    return () => clearInterval(interval);
  }, [userLocation]);

  return (
    <MapContainer center={[28.6139, 77.2090]} zoom={13} style={{ height: '100vh' }}>
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      {buses.map(bus => (
        <Marker key={bus.vehicleId} position={[bus.busLatitude, bus.busLongitude]}>
          <Popup>
            <strong>Route {bus.routeCode}</strong><br />
            ETA: {bus.etaMinutes} mins<br />
            Bus: {bus.vehicleId}
          </Popup>
        </Marker>
      ))}
    </MapContainer>
  );
}

export default BusMap;
```

## 7. Milestones

**Day 1** — map with buses near user, markers with route + ETA, auto-refresh.
**Day 2** — route search, click bus/route for details.
**Day 3** — polish UI, mobile responsive, deploy to Vercel.

## 8. Testing checklist

- [ ] Map loads centered on Delhi
- [ ] User location marker appears (after granting permission)
- [ ] Bus markers appear and refresh every 10s
- [ ] Marker click shows route + ETA + vehicle ID
- [ ] Route search returns matches
- [ ] Works on mobile viewport

## 9. Deployment

- Frontend: `npm run build`, deploy to Vercel.
- Backend: keep running locally for now, or deploy to Railway/Render later.

## 10. One-line brief for your friend

> Build React app. Backend at localhost:8080/api (CORS already enabled for
> localhost:3000/5173). APIs: `/buses/nearby?lat&lng&radiusMeters&limit`,
> `/routes`, `/routes/search?query`, `/stops/nearby`. Use Leaflet for the
> map. Fetch buses every 10s, show markers with route number + ETA. Get
> user location via Geolocation API. Add route search. Deploy to Vercel.
