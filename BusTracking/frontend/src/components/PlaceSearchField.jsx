import { useEffect, useState } from 'react';
import { searchPlaces } from '../api/client';

// Debounced place-name autocomplete, styled to match the map's route SearchBar.
export default function PlaceSearchField({ placeholder, value, onChange, onSelect }) {
  const [results, setResults] = useState([]);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (value.trim().length < 3) {
      setResults([]);
      return;
    }
    const handle = setTimeout(() => {
      searchPlaces(value)
        .then(setResults)
        .catch(() => setResults([]));
    }, 400);
    return () => clearTimeout(handle);
  }, [value]);

  return (
    <div className="place-search">
      <input
        type="text"
        placeholder={placeholder}
        value={value}
        onChange={(e) => {
          onChange(e.target.value);
          setOpen(true);
        }}
        onFocus={() => setOpen(true)}
        // onMouseDown on the option fires before this blur closes the list
        onBlur={() => setTimeout(() => setOpen(false), 150)}
        required
      />
      {open && results.length > 0 && (
        <ul className="search-results">
          {results.map((place, i) => (
            <li
              key={i}
              onMouseDown={() => {
                onSelect(place);
                setOpen(false);
              }}
            >
              {place.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
