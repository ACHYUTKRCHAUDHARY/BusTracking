import { useEffect, useState } from 'react';
import { searchRoutes } from '../api/client';

export default function SearchBar({ onSelectRoute }) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (!query.trim()) {
      setResults([]);
      return;
    }
    const handle = setTimeout(() => {
      searchRoutes(query)
        .then(setResults)
        .catch(() => setResults([]));
    }, 300);
    return () => clearTimeout(handle);
  }, [query]);

  return (
    <div className="search-bar">
      <input
        type="text"
        placeholder="Search route number..."
        value={query}
        onChange={(e) => {
          setQuery(e.target.value);
          setOpen(true);
        }}
        onFocus={() => setOpen(true)}
        onBlur={() => setTimeout(() => setOpen(false), 150)}
      />
      {open && results.length > 0 && (
        <ul className="search-results">
          {results.map((route) => (
            <li
              key={route.id}
              // onMouseDown fires before the input's onBlur closes the list
              onMouseDown={() => {
                onSelectRoute(route);
                setQuery(route.routeCode);
                setOpen(false);
              }}
            >
              <strong>{route.routeCode}</strong> {route.name}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
