import React, { useCallback, useEffect, useMemo, useReducer, useState } from 'react';
import {
  Activity,
  AlertOctagon,
  AlertTriangle,
  Battery,
  CheckCircle2,
  Compass,
  FileCheck2,
  Layers,
  MapPin,
  Navigation,
  PhoneCall,
  Radio,
  Search,
  Shield,
  ShieldAlert,
  ShieldCheck,
  Target,
  Users,
  Zap
} from 'lucide-react';
import {
  fetchActiveTrips,
  fetchGeofences,
  fetchHazards,
  fetchHealth,
  fetchIncidents,
  fetchResponders,
  fetchSearchProbability,
  fetchTrajectory,
  matchResponders,
  updateIncidentStatus,
  verifyVoucher,
  WS_URL
} from './api';
import {
  createInitialDashboardState,
  dashboardReducer,
  selectCollections,
  selectDashboardSubjects,
  selectSelectedSubject
} from './state/dashboardReducer';

import Sidebar from './components/Sidebar';
import TopBar from './components/TopBar';
import KpiMetrics from './components/KpiMetrics';
import GeospatialMap from './components/GeospatialMap';
import SubjectFeed from './components/SubjectFeed';
import SubjectDetailDrawer from './components/SubjectDetailDrawer';

import './App.css';

export default function App() {
  const [state, dispatch] = useReducer(dashboardReducer, undefined, createInitialDashboardState);
  const [rescueEvaluation, setRescueEvaluation] = useState(null);
  const [searchProbability, setSearchProbability] = useState(null);
  const [verifyInput, setVerifyInput] = useState('');
  const [verifyResult, setVerifyResult] = useState(null);
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  // Navigation & Search / Filter states
  const [activeNav, setActiveNav] = useState('map');
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState('all');

  const subjects = useMemo(() => selectDashboardSubjects(state), [state]);
  const selectedSubject = useMemo(() => selectSelectedSubject(state), [state]);
  const { incidents, trips, geofences, hazards, responders } = useMemo(() => selectCollections(state), [state]);

  const selectedTrajectory = selectedSubject?.trajectory || [];
  const trajectoryPoints = selectedTrajectory
    .map(point => [Number(point.lat), Number(point.lon)])
    .filter(([lat, lon]) => Number.isFinite(lat) && Number.isFinite(lon));

  // Hydrate initial data from backend API
  const hydrateDashboard = useCallback(async () => {
    setLoading(true);
    try {
      const [healthResult, incidentList, geofenceList, hazardList, responderList, tripList] = await Promise.all([
        fetchHealth().catch(() => null),
        fetchIncidents().catch(() => []),
        fetchGeofences().catch(() => []),
        fetchHazards().catch(() => []),
        fetchResponders().catch(() => []),
        fetchActiveTrips().catch(() => [])
      ]);

      const breadcrumbPairs = await Promise.all(
        tripList.map(async trip => {
          const tripId = trip.id || trip.tripId;
          if (!tripId) return null;
          const trail = await fetchTrajectory(tripId).catch(() => []);
          return [tripId, Array.isArray(trail) ? trail : []];
        })
      );

      setHealth(healthResult);
      dispatch({
        type: 'HYDRATE_SUCCESS',
        receivedAt: new Date().toISOString(),
        payload: {
          incidents: incidentList,
          geofences: geofenceList,
          hazards: hazardList,
          responders: responderList,
          trips: tripList,
          breadcrumbsByTripId: Object.fromEntries(breadcrumbPairs.filter(Boolean))
        }
      });
    } catch (error) {
      console.error('Dashboard hydration failed:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    hydrateDashboard();
  }, [hydrateDashboard]);

  // WebSocket Live Stream Listener
  useEffect(() => {
    let ws = null;
    let reconnectTimer = null;
    let shouldReconnect = true;

    const connectWs = () => {
      if (!shouldReconnect) return;
      ws = new WebSocket(WS_URL);

      ws.onopen = () => dispatch({ type: 'WS_CONNECTED' });

      ws.onmessage = event => {
        try {
          const data = JSON.parse(event.data);
          const receivedAt = data.timestamp || new Date().toISOString();
          dispatch({ type: data.type, payload: data.payload, receivedAt });
        } catch (error) {
          console.error('Invalid WebSocket payload:', error);
        }
      };

      ws.onclose = () => {
        if (!shouldReconnect) return;
        dispatch({ type: 'WS_RECONNECTING' });
        reconnectTimer = setTimeout(connectWs, 3000);
      };

      ws.onerror = () => {
        dispatch({ type: 'WS_OFFLINE' });
        ws.close();
      };
    };

    connectWs();

    return () => {
      shouldReconnect = false;
      if (reconnectTimer) clearTimeout(reconnectTimer);
      if (ws) ws.close();
    };
  }, []);

  // Fetch search probability whenever the selected subject coordinates change
  useEffect(() => {
    setRescueEvaluation(null);
    setSearchProbability(null);
    if (!selectedSubject?.lat || !selectedSubject?.lon) return;

    fetchSearchProbability({
      lastBreadcrumb: { lat: selectedSubject.lat, lon: selectedSubject.lon },
      speedMetersPerSec: 1.2,
      elapsedTimeMins: selectedSubject.staleStatus === 'LIVE' ? 2 : 60,
      lastDirectionDeg: 45
    })
      .then(setSearchProbability)
      .catch(error => console.error('Search probability fetch failed:', error));
  }, [selectedSubject?.subjectId, selectedSubject?.lat, selectedSubject?.lon, selectedSubject?.staleStatus]);

  const handleSelectSubject = subjectId => {
    dispatch({ type: 'SELECT_SUBJECT', subjectId });
  };

  const handleMatchResponders = async () => {
    if (!selectedSubject?.lat || !selectedSubject?.lon) return;
    try {
      const result = await matchResponders(selectedSubject.lat, selectedSubject.lon);
      setRescueEvaluation(result);
    } catch (error) {
      console.error('Responder match failed:', error);
      setRescueEvaluation({ divergenceExplanation: 'Responder matching is unavailable while the gateway is offline.' });
    }
  };

  const handleUpdateIncidentState = async status => {
    if (!selectedSubject?.incidentId) return;
    try {
      const updated = await updateIncidentStatus(selectedSubject.incidentId, status);
      dispatch({ type: 'INCIDENT_STATUS_CHANGED', payload: updated, receivedAt: new Date().toISOString() });
    } catch (error) {
      console.error('Incident state update failed:', error);
    }
  };

  const handleVerifyContract = async () => {
    if (!verifyInput.trim()) return;
    try {
      setVerifyResult(await verifyVoucher(verifyInput.trim()));
    } catch {
      setVerifyResult({ valid: false, reason: 'Verification request failed' });
    }
  };

  // Auto-select initial subject if none selected
  useEffect(() => {
    if (subjects.length > 0 && !state.selectedSubjectId) {
      dispatch({ type: 'SELECT_SUBJECT', subjectId: subjects[0].subjectId });
    }
  }, [subjects, state.selectedSubjectId]);

  const liveCount = subjects.filter(subject => subject.staleStatus === 'LIVE').length;
  const staleCount = subjects.filter(subject => subject.isStale || subject.staleStatus === 'EMERGENCY_STALE' || subject.staleStatus === 'STALE').length;
  const activeIncidentCount = incidents.filter(incident => incident.status !== 'RESOLVED').length;

  return (
    <div className="dashboard-app-root">
      {/* 1. Left Nav Sidebar (Auto-collapsible on hover, no logo, no user card) */}
      <Sidebar
        activeNav={activeNav}
        onSelectNav={nav => {
          setActiveNav(nav);
          if (nav === 'sos') setActiveFilter('sos');
          else if (nav === 'trips') setActiveFilter('all');
        }}
        activeSosCount={activeIncidentCount}
        connectionStatus={state.connectionStatus}
      />

      {/* 2. Main Dashboard Area */}
      <div className="dashboard-main-canvas">
        {/* Top Navigation / Search Bar */}
        <TopBar
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
          activeFilter={activeFilter}
          onFilterChange={setActiveFilter}
          activeSosCount={activeIncidentCount}
          onRefresh={hydrateDashboard}
          loading={loading}
        />

        {/* Content Canvas */}
        <main className="dashboard-content-area">
          {/* Navigation View Switcher */}
          {activeNav === 'map' && (
            <div className="dashboard-workspace-grid">
              <GeospatialMap
                subjects={subjects}
                selectedSubject={selectedSubject}
                onSelectSubject={handleSelectSubject}
                geofences={geofences}
                hazards={hazards}
                responders={responders}
                searchProbability={searchProbability}
                trajectoryPoints={trajectoryPoints}
              />
              <div className="analytics-inspector-column full-inspector">
                <SubjectDetailDrawer
                  selectedSubject={selectedSubject}
                  onUpdateIncidentState={handleUpdateIncidentState}
                  onEvaluateRescue={handleMatchResponders}
                  rescueEvaluation={rescueEvaluation}
                  verifyInput={verifyInput}
                  onVerifyInputChange={setVerifyInput}
                  onVerifyContract={handleVerifyContract}
                  verifyResult={verifyResult}
                />
              </div>
            </div>
          )}

          {activeNav === 'sos' && (
            <div className="dashboard-workspace-grid">
              <GeospatialMap
                subjects={subjects.filter(s => s.status === 'SOS' || s.staleStatus === 'EMERGENCY_STALE')}
                selectedSubject={selectedSubject}
                onSelectSubject={handleSelectSubject}
                geofences={geofences}
                hazards={hazards}
                responders={responders}
                searchProbability={searchProbability}
                trajectoryPoints={trajectoryPoints}
              />
              <div className="analytics-inspector-column full-inspector">
                <SubjectDetailDrawer
                  selectedSubject={selectedSubject}
                  onUpdateIncidentState={handleUpdateIncidentState}
                  onEvaluateRescue={handleMatchResponders}
                  rescueEvaluation={rescueEvaluation}
                  verifyInput={verifyInput}
                  onVerifyInputChange={setVerifyInput}
                  onVerifyContract={handleVerifyContract}
                  verifyResult={verifyResult}
                />
              </div>
            </div>
          )}

          {activeNav === 'trips' && (
            <div className="dashboard-full-view">
              <div className="glass-panel" style={{ padding: '1.25rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                    <Users size={20} color="var(--primary-cyan)" />
                    <h3 style={{ fontSize: '1.1rem', fontWeight: 800 }}>Monitored Tourist Trips & GPS Streams</h3>
                  </div>
                  <span className="badge badge-cyan">{trips.length} Active Trips</span>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '1rem' }}>
                  {subjects.map(subject => {
                    const isSos = subject.status === 'SOS' || subject.staleStatus === 'EMERGENCY_STALE';
                    const batt = subject.batteryPercent != null ? subject.batteryPercent : null;
                    const isSelected = selectedSubject?.subjectId === subject.subjectId;

                    return (
                      <div
                        key={subject.subjectId}
                        className="glass-card"
                        style={{
                          cursor: 'pointer',
                          display: 'flex',
                          flexDirection: 'column',
                          gap: '0.65rem',
                          border: isSelected ? '1px solid var(--border-glow-cyan)' : '1px solid rgba(255, 255, 255, 0.65)',
                          background: isSelected ? 'rgba(255, 255, 255, 0.85)' : 'var(--bg-glass-card)'
                        }}
                        onClick={() => {
                          handleSelectSubject(subject.subjectId);
                          setActiveNav('map');
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <div
                              style={{
                                width: 32,
                                height: 32,
                                borderRadius: 10,
                                background: isSos ? 'rgba(225, 29, 72, 0.12)' : 'rgba(2, 132, 199, 0.1)',
                                color: isSos ? '#E11D48' : '#0284C7',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontWeight: 800,
                                fontSize: '0.8rem'
                              }}
                            >
                              {isSos ? '!' : subject.touristId?.slice(-2) || 'T'}
                            </div>
                            <div>
                              <strong style={{ fontSize: '0.96rem', color: 'var(--text-bright)', display: 'block' }}>
                                {subject.touristId}
                              </strong>
                              <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)', fontWeight: 600 }}>
                                {subject.tripId || 'Direct GPS Track'}
                              </span>
                            </div>
                          </div>

                          <span className={isSos ? 'badge badge-danger' : 'badge badge-safe'}>
                            {subject.status}
                          </span>
                        </div>

                        <div style={{ background: 'rgba(255, 255, 255, 0.5)', padding: '0.55rem 0.75rem', borderRadius: 12, fontSize: '0.78rem' }}>
                          <div style={{ color: 'var(--text-muted)', fontSize: '0.7rem', textTransform: 'uppercase', fontWeight: 700 }}>
                            CHOSEN ITINERARY ROUTE
                          </div>
                          <strong style={{ color: 'var(--text-bright)', fontSize: '0.84rem' }}>
                            {subject.plannedRouteId || 'Not specified'}
                          </strong>
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.4rem', fontSize: '0.76rem' }}>
                          <div style={{ background: 'rgba(255, 255, 255, 0.4)', padding: '0.4rem 0.6rem', borderRadius: 8 }}>
                            <span style={{ fontSize: '0.66rem', color: 'var(--text-dim)', display: 'block' }}>BATTERY HEALTH</span>
                            {batt != null ? (
                              <strong style={{ color: batt <= 20 ? '#E11D48' : batt <= 50 ? '#D97706' : '#059669' }}>
                                {batt}% ({batt <= 20 ? 'Critical' : 'Good'})
                              </strong>
                            ) : (
                              <strong style={{ color: 'var(--text-dim)' }}>--</strong>
                            )}
                          </div>
                          <div style={{ background: 'rgba(255, 255, 255, 0.4)', padding: '0.4rem 0.6rem', borderRadius: 8 }}>
                            <span style={{ fontSize: '0.66rem', color: 'var(--text-dim)', display: 'block' }}>GPS ACCURACY</span>
                            <strong style={{ color: 'var(--text-bright)' }}>
                              {subject.accuracyMeters != null ? `±${Math.round(subject.accuracyMeters)}m` : '--'}
                            </strong>
                          </div>
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.72rem', color: 'var(--text-dim)', marginTop: '0.2rem' }}>
                          <span>Zone: <strong>{subject.currentZoneId || 'Not determined'}</strong></span>
                          <span style={{ color: 'var(--primary-cyan)', fontWeight: 600 }}>Click to Inspect Full Details &rarr;</span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          )}

          {activeNav === 'hazards' && (
            <div className="dashboard-full-view">
              <div className="glass-panel" style={{ padding: '1.25rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                    <Layers size={20} color="var(--accent-amber)" />
                    <h3 style={{ fontSize: '1.1rem', fontWeight: 800 }}>Active Geofences & Hazard Zones</h3>
                  </div>
                  <span className="badge badge-caution">{hazards.length} Reported Roadblocks</span>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1rem' }}>
                  {hazards.map(hazard => (
                    <div key={hazard.id} className="glass-card">
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <strong style={{ fontSize: '0.95rem' }}>{hazard.hazardType || hazard.type || 'Hazard Warning'}</strong>
                        <span className="badge badge-caution">{hazard.status || 'Active'}</span>
                      </div>
                      <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.4rem' }}>
                        {hazard.description || 'Reported landslide or road blockage on mountain pass corridor.'}
                      </p>
                      <div style={{ fontSize: '0.74rem', color: 'var(--text-dim)', marginTop: '0.6rem', fontFamily: 'monospace' }}>
                        Coordinates: {Number(hazard.lat).toFixed(4)}, {Number(hazard.lon).toFixed(4)}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {activeNav === 'responders' && (
            <div className="dashboard-full-view">
              <div className="glass-panel" style={{ padding: '1.25rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                    <Activity size={20} color="var(--accent-purple)" />
                    <h3 style={{ fontSize: '1.1rem', fontWeight: 800 }}>SDRF & Emergency Dispatch Units</h3>
                  </div>
                  <span className="badge badge-purple">{responders.length} Units Ready</span>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1rem' }}>
                  {responders.map(responder => (
                    <div key={responder.id} className="glass-card">
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <strong style={{ fontSize: '0.95rem' }}>{responder.name}</strong>
                        <span className="badge badge-safe">{responder.status || 'Available'}</span>
                      </div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.4rem' }}>
                        Type: {responder.vehicle || responder.type || 'SDRF Rapid Rescue'}
                      </div>
                      <div style={{ fontSize: '0.74rem', color: 'var(--text-dim)', marginTop: '0.6rem', fontFamily: 'monospace' }}>
                        Base: {Number(responder.lat).toFixed(4)}, {Number(responder.lon).toFixed(4)}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {activeNav === 'verify' && (
            <div className="dashboard-full-view">
              <div className="glass-panel" style={{ padding: '1.5rem', maxWidth: '650px', margin: '0 auto', width: '100%' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1rem' }}>
                  <FileCheck2 size={22} color="var(--primary-cyan)" />
                  <h3 style={{ fontSize: '1.15rem', fontWeight: 800 }}>Blockchain Voucher Verification</h3>
                </div>
                <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
                  Validate pseudonymous tourist ID vouchers stored on Sepolia/Amoy testnets using cryptographic hash commitments without exposing raw identity documents.
                </p>

                <div style={{ display: 'flex', gap: '0.6rem', marginBottom: '1rem' }}>
                  <input
                    type="text"
                    className="topbar-search-input"
                    style={{
                      background: '#FFFFFF',
                      border: '1px solid var(--border-subtle)',
                      borderRadius: 12,
                      padding: '0.65rem 0.9rem',
                      fontSize: '0.85rem',
                      flex: 1
                    }}
                    placeholder="Enter keccak256(TouristID + ':' + Salt)"
                    value={verifyInput}
                    onChange={e => setVerifyInput(e.target.value)}
                  />
                  <button type="button" className="btn-primary" onClick={handleVerifyContract}>
                    Verify Voucher
                  </button>
                </div>

                {verifyResult && (
                  <div
                    style={{
                      padding: '0.85rem 1rem',
                      borderRadius: 12,
                      background: verifyResult.valid || verifyResult.isValid ? 'rgba(5, 150, 105, 0.1)' : 'rgba(225, 29, 72, 0.1)',
                      border: '1px solid',
                      borderColor: verifyResult.valid || verifyResult.isValid ? 'rgba(5, 150, 105, 0.3)' : 'rgba(225, 29, 72, 0.3)',
                      fontSize: '0.85rem',
                      color: verifyResult.valid || verifyResult.isValid ? '#059669' : '#E11D48',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem',
                      fontWeight: 600
                    }}
                  >
                    {verifyResult.valid || verifyResult.isValid ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
                    <span>
                      {verifyResult.valid || verifyResult.isValid
                        ? 'Cryptographic Voucher Verified & Active on Blockchain'
                        : `Voucher Invalid: ${verifyResult.reason || 'Record not found'}`}
                    </span>
                  </div>
                )}
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
