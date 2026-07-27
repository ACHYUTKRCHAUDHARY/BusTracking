import { useCallback, useEffect, useRef, useState } from 'react';
import BusMap from './components/BusMap';
import SearchBar from './components/SearchBar';
import { getAllStops, getNearbyBuses } from './api/client';
import './App.css';

function App() {
  const [userLocation, setUserLocation] = useState(null);
  const [locationError, setLocationError] = useState(null);
  const [buses, setBuses] = useState([]);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [stopsById, setStopsById] = useState({});
  const [selectedRoute, setSelectedRoute] = useState(null);

  const userLocationRef = useRef(userLocation);
  useEffect(() => {
    userLocationRef.current = userLocation;
  }, [userLocation]);

  // Track the user's position continuously.
  useEffect(() => {
    if (!navigator.geolocation) {
      setLocationError('Geolocation is not supported by this browser.');
      return;
    }
    const watchId = navigator.geolocation.watchPosition(
      (pos) => {
        setUserLocation({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        setLocationError(null);
      },
      (err) => setLocationError(err.message),
      { enableHighAccuracy: true, maximumAge: 10000, timeout: 10000 }
    );
    return () => navigator.geolocation.clearWatch(watchId);
  }, []);

  // Load all stops once so route selections can be resolved to lat/lng
  // client-side (the backend has no "stops for route, with coordinates" endpoint).
  useEffect(() => {
    getAllStops()
      .then((stops) => {
        const map = {};
        stops.forEach((s) => {
          map[s.id] = s;
        });
        setStopsById(map);
      })
      .catch((err) => console.error('Failed to load stops', err));
  }, []);

  const fetchBuses = useCallback(() => {
    const loc = userLocationRef.current;
    if (!loc) return;
    getNearbyBuses(loc.lat, loc.lng)
      .then((data) => {
        setBuses(data);
        setLastUpdated(new Date());
      })
      .catch((err) => console.error('Failed to fetch nearby buses', err));
  }, []);

  // Fetch as soon as we have a location, then keep polling every 10s.
  useEffect(() => {
    if (userLocation) fetchBuses();
  }, [userLocation, fetchBuses]);

  useEffect(() => {
    const interval = setInterval(fetchBuses, 10000);
    return () => clearInterval(interval);
  }, [fetchBuses]);

  const handleSelectRoute = (route) => {
    const stops = (route.stopIds || [])
      .map((id) => stopsById[id])
      .filter(Boolean)
      .map((s) => ({ id: s.id, name: s.name, lat: s.latitude, lng: s.longitude }));
    setSelectedRoute({ ...route, stops });
  };

  return (
    <div className="app-container">
      <SearchBar onSelectRoute={handleSelectRoute} />

      {locationError && <div className="banner banner-error">{locationError}</div>}

      {selectedRoute && (
        <div className="route-panel">
          <div>
            <strong>Route {selectedRoute.routeCode}</strong>
            {selectedRoute.name ? ` — ${selectedRoute.name}` : ''}
          </div>
          <div className="route-panel-meta">{selectedRoute.stops.length} stops on map</div>
          <button onClick={() => setSelectedRoute(null)}>Clear</button>
        </div>
      )}

      <div className="status-bar">
        {buses.length} bus{buses.length === 1 ? '' : 'es'} nearby
        {lastUpdated && ` · updated ${lastUpdated.toLocaleTimeString()}`}
      </div>

      <BusMap
        userLocation={userLocation}
        buses={buses}
        selectedRoute={selectedRoute}
        onSelectStop={() => {}}
      />
    </div>
  );
}

export default App;
