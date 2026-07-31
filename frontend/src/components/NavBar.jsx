import { NavLink } from 'react-router-dom';

const LINKS = [
  { to: '/', label: 'Live Map', end: true },
  { to: '/search', label: 'Search' },
  { to: '/route', label: 'Route' },
  { to: '/stops', label: 'Stops' },
  { to: '/passing', label: 'Passing' },
  { to: '/journey', label: 'Planner' },
  { to: '/replay', label: 'Replay' },
  { to: '/admin', label: 'Admin' },
];

export default function NavBar() {
  return (
    <nav className="nav-bar">
      <div className="nav-brand">
        <span>🚌 Delhi Bus Tracker</span>
        <span className="nav-brand-badge">LIVE</span>
      </div>
      <div className="nav-links">
        {LINKS.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            end={link.end}
            className={({ isActive }) => 'nav-link' + (isActive ? ' nav-link-active' : '')}
          >
            {link.label}
          </NavLink>
        ))}
      </div>
    </nav>
  );
}
