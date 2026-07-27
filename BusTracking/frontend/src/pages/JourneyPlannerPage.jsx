import { useState } from 'react';
import { planJourney } from '../api/client';

export default function JourneyPlannerPage() {
  const [sourceLat, setSourceLat] = useState('');
  const [sourceLng, setSourceLng] = useState('');
  const [destinationLat, setDestinationLat] = useState('');
  const [destinationLng, setDestinationLng] = useState('');
  const [options, setOptions] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const useMyLocation = () => {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition((pos) => {
      setSourceLat(pos.coords.latitude.toFixed(6));
      setSourceLng(pos.coords.longitude.toFixed(6));
    });
  };

  const submit = (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    planJourney(Number(sourceLat), Number(sourceLng), Number(destinationLat), Number(destinationLng))
      .then((res) => setOptions(res.options))
      .catch(() => setError('Could not plan a journey between those points.'))
      .finally(() => setLoading(false));
  };

  return (
    <div className="page-panel">
      <h2>Smart Journey Planner</h2>

      <form className="journey-form" onSubmit={submit}>
        <fieldset>
          <legend>Source</legend>
          <input type="number" step="any" placeholder="Latitude" value={sourceLat} onChange={(e) => setSourceLat(e.target.value)} required />
          <input type="number" step="any" placeholder="Longitude" value={sourceLng} onChange={(e) => setSourceLng(e.target.value)} required />
          <button type="button" onClick={useMyLocation}>
            Use my location
          </button>
        </fieldset>
        <fieldset>
          <legend>Destination</legend>
          <input type="number" step="any" placeholder="Latitude" value={destinationLat} onChange={(e) => setDestinationLat(e.target.value)} required />
          <input type="number" step="any" placeholder="Longitude" value={destinationLng} onChange={(e) => setDestinationLng(e.target.value)} required />
        </fieldset>
        <button type="submit" disabled={loading}>
          {loading ? 'Planning…' : 'Plan Journey'}
        </button>
      </form>

      {error && <div className="banner banner-error" style={{ position: 'static', marginTop: 12 }}>{error}</div>}

      {options && options.length === 0 && <p style={{ marginTop: 16 }}>No route connects those two points yet.</p>}

      <div className="list-cards" style={{ marginTop: 16 }}>
        {options?.map((option, i) => (
          <div key={i} className="card">
            <div className="card-row">
              <strong>{option.direct ? 'Direct' : `Transfer (${option.legs.length} legs)`}</strong>
              <span>{option.totalMinutes} min total</span>
            </div>
            <p>Walk to boarding stop: {Math.round(option.walkToBoardMeters)} m</p>
            {option.legs.map((leg, j) => (
              <div key={j} className="card-row">
                <span>
                  Route {leg.routeCode} {leg.routeName ? `— ${leg.routeName}` : ''}: {leg.boardStopName} → {leg.alightStopName}
                </span>
                <span>{leg.inVehicleMinutes} min</span>
              </div>
            ))}
            <p>Walk from alighting stop: {Math.round(option.walkFromAlightMeters)} m</p>
          </div>
        ))}
      </div>
    </div>
  );
}
