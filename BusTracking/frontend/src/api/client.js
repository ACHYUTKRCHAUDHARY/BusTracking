import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
});

export const getNearbyBuses = (lat, lng, radiusMeters = 2000, limit = 30) =>
  client
    .get('/buses/nearby', { params: { lat, lng, radiusMeters, limit } })
    .then((res) => res.data);

export const getRoutes = () => client.get('/routes').then((res) => res.data);

export const searchRoutes = (query, limit = 8) =>
  client
    .get('/routes/search', { params: { query, limit } })
    .then((res) => res.data);

// Stops are paginated on the backend; pull a large page once so routes/stop
// clicks can be resolved to lat/lng entirely on the client.
export const getAllStops = (size = 1000) =>
  client.get('/stops', { params: { page: 0, size } }).then((res) => res.data.content);

export const getNearbyStops = (lat, lng, radiusMeters = 1000, limit = 20) =>
  client
    .get('/stops/nearby', { params: { lat, lng, radiusMeters, limit } })
    .then((res) => res.data);

export default client;
