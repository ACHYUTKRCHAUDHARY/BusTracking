import { useEffect, useState } from 'react';
import { adminLogin, clearAdminToken, getAdminStats, getAdminToken, setAdminToken } from '../api/client';

export default function AdminDashboardPage() {
  const [token, setToken] = useState(getAdminToken());
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loginError, setLoginError] = useState(null);
  const [stats, setStats] = useState(null);
  const [statsError, setStatsError] = useState(null);

  const login = (e) => {
    e.preventDefault();
    setLoginError(null);
    adminLogin(username, password)
      .then((res) => {
        setAdminToken(res.token);
        setToken(res.token);
      })
      .catch((err) => setLoginError(err.response?.data?.message || 'Login failed.'));
  };

  const logout = () => {
    clearAdminToken();
    setToken(null);
    setStats(null);
  };

  const loadStats = () => {
    getAdminStats()
      .then((data) => {
        setStats(data);
        setStatsError(null);
      })
      .catch((err) => {
        if (err.response?.status === 401 || err.response?.status === 403) {
          logout();
        } else {
          setStatsError('Failed to load admin stats.');
        }
      });
  };

  useEffect(() => {
    if (!token) return undefined;
    loadStats();
    const interval = setInterval(loadStats, 10000);
    return () => clearInterval(interval);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  if (!token) {
    return (
      <div className="page-panel">
        <h2>Admin Login</h2>
        <form className="inline-form" onSubmit={login}>
          <input type="text" placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} />
          <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} />
          <button type="submit">Log in</button>
        </form>
        {loginError && <div className="banner banner-error" style={{ position: 'static', marginTop: 12 }}>{loginError}</div>}
      </div>
    );
  }

  return (
    <div className="page-panel">
      <div className="card-row">
        <h2>Admin Dashboard</h2>
        <button onClick={logout}>Log out</button>
      </div>

      {statsError && <div className="banner banner-error" style={{ position: 'static' }}>{statsError}</div>}

      {stats && (
        <>
          <div className="kpi-grid">
            <div className="kpi-card">
              <div className="kpi-value">{stats.liveBusCount}</div>
              <div className="kpi-label">Live Buses</div>
            </div>
            <div className="kpi-card">
              <div className="kpi-value">{stats.offlineBusCount}</div>
              <div className="kpi-label">Offline Buses</div>
            </div>
            <div className="kpi-card">
              <div className="kpi-value">{stats.delayedBusCount}</div>
              <div className="kpi-label">Delayed Buses</div>
            </div>
            <div className="kpi-card">
              <div className="kpi-value">{stats.averageSpeedKmh}</div>
              <div className="kpi-label">Avg Speed (km/h)</div>
            </div>
          </div>

          <h3 style={{ marginTop: 20 }}>Route Statistics</h3>
          <table className="data-table">
            <thead>
              <tr>
                <th>Route</th>
                <th>Active Buses</th>
              </tr>
            </thead>
            <tbody>
              {stats.routeStats.map((r) => (
                <tr key={r.routeCode}>
                  <td>{r.routeCode}</td>
                  <td>{r.activeBusCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {stats.routeStats.length === 0 && <p>No routes currently reporting live buses.</p>}
        </>
      )}
    </div>
  );
}
