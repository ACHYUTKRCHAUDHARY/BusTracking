import { memo, useCallback, useEffect, useState } from 'react';
import { searchPlaces } from '../api/client';

function PlaceSearchField({ placeholder, value, onChange, onSelect }) {
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
    }, 350);
    return () => clearTimeout(handle);
  }, [value]);

  const handleSelect = useCallback(
    (place) => {
      onSelect(place);
      setOpen(false);
    },
    [onSelect]
  );

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
        onBlur={() => setTimeout(() => setOpen(false), 150)}
        required
      />
      {open && results.length > 0 && (
        <ul className="search-results">
          {results.map((place, i) => (
            <li key={i} onMouseDown={() => handleSelect(place)}>
              {place.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default memo(PlaceSearchField);
