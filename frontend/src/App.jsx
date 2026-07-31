import { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import NavBar from './components/NavBar';
import './App.css';

// Lazy load all pages so they are only downloaded when the user visits them
const HomePage = lazy(() => import('./pages/HomePage'));
const BusSearchPage = lazy(() => import('./pages/BusSearchPage'));
const RouteDetailPage = lazy(() => import('./pages/RouteDetailPage'));
const StopsNearbyPage = lazy(() => import('./pages/StopsNearbyPage'));
const PassingNearMePage = lazy(() => import('./pages/PassingNearMePage'));
const JourneyPlannerPage = lazy(() => import('./pages/JourneyPlannerPage'));
const ReplayPage = lazy(() => import('./pages/ReplayPage'));
const AdminDashboardPage = lazy(() => import('./pages/AdminDashboardPage'));

// A simple loading indicator shown while a page's code is downloading
const PageLoader = () => (
  <div style={{ padding: '24px', color: 'var(--text-muted)' }}>
    Loading page...
  </div>
);

function App() {
  return (
    <BrowserRouter>
      <div className="app-shell">
        <NavBar />
        <div className="page-outlet">
          <Suspense fallback={<PageLoader />}>
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/search" element={<BusSearchPage />} />
              <Route path="/route" element={<RouteDetailPage />} />
              <Route path="/stops" element={<StopsNearbyPage />} />
              <Route path="/passing" element={<PassingNearMePage />} />
              <Route path="/journey" element={<JourneyPlannerPage />} />
              <Route path="/replay" element={<ReplayPage />} />
              <Route path="/admin" element={<AdminDashboardPage />} />
            </Routes>
          </Suspense>
        </div>
      </div>
    </BrowserRouter>
  );
}

export default App;
