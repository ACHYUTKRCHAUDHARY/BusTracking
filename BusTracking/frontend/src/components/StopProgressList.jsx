import { memo } from 'react';

const PROGRESS_LABEL = {
  PASSED: 'Passed',
  CURRENT: 'Current',
  NEXT: 'Next',
  UPCOMING: 'Upcoming',
};

function StopProgressList({ stops }) {
  if (!stops?.length) return <p>No stop data for this route.</p>;

  return (
    <ol className="stop-progress-list">
      {stops.map((stop) => (
        <li key={stop.stopId} className={stop.progress ? `stop-item stop-${stop.progress.toLowerCase()}` : 'stop-item'}>
          <span className="stop-seq">{stop.sequence}</span>
          <span className="stop-name">{stop.name}</span>
          {stop.progress && <span className="stop-badge">{PROGRESS_LABEL[stop.progress]}</span>}
        </li>
      ))}
    </ol>
  );
}

export default memo(StopProgressList);
