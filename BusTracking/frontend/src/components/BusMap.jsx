import { memo, useCallback, useEffect, useMemo, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, ZoomControl, useMap } from 'react-leaflet';
import { userIcon, stopIcon, busIcon } from '../utils/leafletIcons';

const DELHI_CENTER = [28.6139, 77.209];

function RecenterOnFirstFix({ userLocation }) {
  const map = useMap();
  const centered = useRef(false);

  useEffect(() => {
    if (userLocation && !centered.current) {
      map.setView([userLocation.lat, userLocation.lng], 14);
      centered.current = true;
    }
  }, [userLocation, map]);

  return null;
}

function BusMap({ userLocation, buses = [], selectedRoute, onSelectStop }) {
  // Memoize unique buses list to prevent duplicate vehicle keys and redundant map marker allocations
  const uniqueBuses = useMemo(() => {
    if (!buses?.length) return [];
    return Array.from(new Map(buses.map((b) => [b.vehicleId, b])).values());
  }, [buses]);

  // Memoize buses lookup by stop name
  const busesAtStop = useCallback(
    (stopName) => uniqueBuses.filter((b) => b.stopName === stopName),
    [uniqueBuses]
  );

  const routePolyline = useMemo(() => {
    if (!selectedRoute?.stops?.length) return [];
    return selectedRoute.stops.map((s) => [s.lat, s.lng]);
  }, [selectedRoute]);

  return (
    <MapContainer center={DELHI_CENTER} zoom={13} zoomControl={false} style={{ height: '100%', width: '100%' }}>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/attributions">CARTO</a>'
        url="https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"
        maxZoom={19}
      />
      <ZoomControl position="bottomright" />

      <RecenterOnFirstFix userLocation={userLocation} />

      {userLocation && (
        <Marker position={[userLocation.lat, userLocation.lng]} icon={userIcon}>
          <Popup>You are here</Popup>
        </Marker>
      )}

      {uniqueBuses.map((bus) => (
        <Marker
          key={bus.vehicleId}
          position={[bus.busLatitude, bus.busLongitude]}
          icon={busIcon(bus.routeCode)}
        >
          <Popup>
            <strong>Route {bus.routeCode}</strong>
            {bus.routeName ? ` — ${bus.routeName}` : ''}
            <br />
            Vehicle: {bus.vehicleId}
            <br />
            ETA: {bus.etaMinutes != null ? `${bus.etaMinutes} min` : 'N/A'}
            <br />
            {bus.stopName && (
              <>
                Next stop: {bus.stopName}
                <br />
              </>
            )}
            {bus.distanceToUser != null && <>Distance: {Math.round(bus.distanceToUser)} m</>}
          </Popup>
        </Marker>
      ))}

      {routePolyline.length > 0 && (
        <>
          <Polyline
            positions={routePolyline}
            pathOptions={{ color: '#2563eb', weight: 4, opacity: 0.8 }}
          />
          {selectedRoute.stops.map((stop) => {
            const upcoming = busesAtStop(stop.name);
            return (
              <Marker
                key={stop.id}
                position={[stop.lat, stop.lng]}
                icon={stopIcon}
                eventHandlers={{ click: () => onSelectStop?.(stop) }}
              >
                <Popup>
                  <strong>{stop.name}</strong>
                  <br />
                  {upcoming.length > 0 ? (
                    <>
                      Upcoming buses:
                      <ul style={{ margin: '4px 0', paddingLeft: 18 }}>
                        {upcoming.map((b) => (
                          <li key={b.vehicleId}>
                            Route {b.routeCode} — {b.etaMinutes} min
                          </li>
                        ))}
                      </ul>
                    </>
                  ) : (
                    'No buses currently tracked at this stop'
                  )}
                </Popup>
              </Marker>
            );
          })}
        </>
      )}
    </MapContainer>
  );
}

// React.memo prevents full map component re-renders when parent state updates unrelated props
export default memo(BusMap);
