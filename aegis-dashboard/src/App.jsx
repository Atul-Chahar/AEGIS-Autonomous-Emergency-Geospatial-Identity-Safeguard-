import React, { useCallback, useEffect, useMemo, useReducer, useState } from 'react';
import {
  Activity,
  AlertCircle,
  AlertOctagon,
  AlertTriangle,
  Battery,
  CheckCircle2,
  Clock,
  Compass,
  MapPin,
  Navigation,
  PhoneCall,
  Radio,
  Shield,
  Target
} from 'lucide-react';
import { Circle, MapContainer, Marker, Polygon, Polyline, Popup, TileLayer } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

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

const INCIDENT_STATES = ['OPEN', 'ACKNOWLEDGED', 'TEAM_DISPATCHED', 'SEARCHING', 'LOCATED', 'RESOLVED'];

const iconByStatus = {
  ACTIVE: createCustomIcon('#10B981'),
  CAUTION: createCustomIcon('#F59E0B'),
  LIVE: createCustomIcon('#10B981'),
  RECENT: createCustomIcon('#F59E0B'),
  STALE: createCustomIcon('#64748B'),
  EMERGENCY_STALE: createCustomIcon('#EF4444'),
  SOS: createCustomIcon('#EF4444'),
  SEARCHING: createCustomIcon('#8B5CF6'),
  RESOLVED: createCustomIcon('#3B82F6')
};

const iconResponder = createCustomIcon('#3B82F6');
const iconHazard = createCustomIcon('#F59E0B');

function createCustomIcon(color) {
  return L.divIcon({
    className: 'custom-leaflet-marker',
    html: `<div style="background-color:${color};width:18px;height:18px;border-radius:50%;border:3px solid white;box-shadow:0 0 12px ${color};"></div>`,
    iconSize: [24, 24],
    iconAnchor: [12, 12]
  });
}

export default function App() {
  const [state, dispatch] = useReducer(dashboardReducer, undefined, createInitialDashboardState);
  const [rescueEvaluation, setRescueEvaluation] = useState(null);
  const [searchProbability, setSearchProbability] = useState(null);
  const [verifyInput, setVerifyInput] = useState('');
  const [verifyResult, setVerifyResult] = useState(null);
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  const subjects = useMemo(() => selectDashboardSubjects(state), [state]);
  const selectedSubject = useMemo(() => selectSelectedSubject(state), [state]);
  const { incidents, trips, geofences, hazards, responders } = useMemo(() => selectCollections(state), [state]);
  const selectedTrajectory = selectedSubject?.trajectory || [];
  const trajectoryPoints = selectedTrajectory
    .map(point => [Number(point.lat), Number(point.lon)])
    .filter(([lat, lon]) => Number.isFinite(lat) && Number.isFinite(lon));

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
      .catch(error => console.error('Search probability failed:', error));
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

  const liveCount = subjects.filter(subject => subject.staleStatus === 'LIVE').length;
  const staleCount = subjects.filter(subject => subject.isStale || subject.staleStatus === 'EMERGENCY_STALE').length;
  const activeIncidentCount = incidents.filter(incident => incident.status !== 'RESOLVED').length;

  return (
    <div style={styles.shell}>
      <header className="glass-panel" style={styles.header}>
        <div style={styles.brandGroup}>
          <div style={styles.brandMark}>
            <Shield size={28} color="#FFFFFF" />
          </div>
          <div>
            <h1 style={styles.title}>AEGIS Command Center</h1>
            <p style={styles.subtitle}>Active trip monitoring, last-known safety location, and emergency dispatch.</p>
          </div>
        </div>

        <div style={styles.headerBadges}>
          <span className="badge badge-purple">API {health?.status || 'DEV'}</span>
          <span className={activeIncidentCount > 0 ? 'badge badge-danger' : 'badge badge-safe'}>
            <AlertTriangle size={14} /> {activeIncidentCount} Active SOS
          </span>
          <span className={state.connectionStatus === 'LIVE' ? 'badge badge-safe' : 'badge badge-danger'}>
            <Radio size={14} /> {state.connectionStatus}
          </span>
        </div>
      </header>

      <main style={styles.mainGrid}>
        <section style={styles.leftColumn}>
          <div style={styles.statsGrid}>
            <MetricCard label="Monitored Active Trips" value={`${subjects.length} Subjects`} tone="main" detail={`${liveCount} live, ${staleCount} stale`} />
            <MetricCard label="Critical Emergency SOS" value={activeIncidentCount} tone="danger" detail={activeIncidentCount > 0 ? 'Requires operator action' : 'All clear'} />
            <MetricCard
              label="Search Area Reduction"
              value={`${searchProbability?.metrics?.areaReductionPercent || 0}%`}
              tone="safe"
              detail={searchProbability?.metrics
                ? `${searchProbability.metrics.searchAreaBeforeBlackBoxKm2} km2 to ${searchProbability.metrics.searchAreaAfterBlackBoxKm2} km2`
                : 'Estimated after subject selection'}
              icon={<Target size={14} />}
            />
            <MetricCard label="Available Responders" value={`${responders.length} Units`} tone="cyan" detail="Police, rescue, medical" />
          </div>

          <div className="glass-panel" style={styles.mapPanel}>
            <MapContainer center={[25.25, 91.5]} zoom={10} style={styles.map}>
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />

              {geofences.map(gf => renderGeofence(gf))}
              {hazards.map(hazard => renderHazard(hazard))}
              {renderSearchSectors(searchProbability)}

              {trajectoryPoints.length > 1 && (
                <Polyline positions={trajectoryPoints} pathOptions={{ color: '#38BDF8', weight: 4, opacity: 0.9, dashArray: '6, 6' }} />
              )}

              {responders.map(responder => renderResponder(responder))}

              {subjects.map(subject => {
                if (!Number.isFinite(subject.lat) || !Number.isFinite(subject.lon)) return null;
                const isSelected = selectedSubject?.subjectId === subject.subjectId;
                const markerIcon = iconByStatus[subject.staleStatus] || iconByStatus[subject.status] || iconByStatus.ACTIVE;
                return (
                  <React.Fragment key={subject.subjectId}>
                    {(subject.status === 'SOS' || subject.staleStatus === 'EMERGENCY_STALE') && (
                      <Circle
                        center={[subject.lat, subject.lon]}
                        radius={1500}
                        pathOptions={{ color: '#EF4444', fillColor: '#EF4444', fillOpacity: 0.22 }}
                      />
                    )}
                    {isSelected && (
                      <Circle
                        center={[subject.lat, subject.lon]}
                        radius={Math.max(subject.accuracyMeters || 50, 50)}
                        pathOptions={{ color: '#38BDF8', fillColor: '#38BDF8', fillOpacity: 0.16 }}
                      />
                    )}
                    <Marker
                      position={[subject.lat, subject.lon]}
                      icon={markerIcon}
                      eventHandlers={{ click: () => handleSelectSubject(subject.subjectId) }}
                    >
                      <Popup>
                        <strong>{subject.status === 'SOS' ? 'SOS Incident' : 'Active Trip'}</strong><br />
                        Tourist: {subject.touristId}<br />
                        Trip: {subject.tripId || 'Not linked'}<br />
                        Last known: {formatDateTime(subject.lastSeenAt)}<br />
                        Accuracy: {formatMeters(subject.accuracyMeters)}<br />
                        Status: {subject.staleStatus}
                      </Popup>
                    </Marker>
                  </React.Fragment>
                );
              })}
            </MapContainer>

            <div style={styles.legend}>
              <LegendDot color="#10B981" label="Live trip" />
              <LegendDot color="#F59E0B" label="Recent/caution" />
              <LegendDot color="#64748B" label="Stale" />
              <LegendDot color="#EF4444" label="SOS" />
              <LegendLine color="#38BDF8" label="Selected trail" />
              <LegendDot color="#3B82F6" label="Responder" />
            </div>
          </div>
        </section>

        <aside style={styles.rightColumn}>
          <section className="glass-panel" style={styles.panel}>
            <div style={styles.panelHeader}>
              <h2 style={styles.panelTitle}><Activity size={20} /> Active Subjects</h2>
              <button className="btn-primary" style={styles.compactButton} onClick={hydrateDashboard}>{loading ? 'Refreshing' : 'Refresh'}</button>
            </div>

            <div style={styles.subjectList}>
              {subjects.map(subject => (
                <button
                  key={subject.subjectId}
                  type="button"
                  onClick={() => handleSelectSubject(subject.subjectId)}
                  style={{
                    ...styles.subjectButton,
                    borderColor: selectedSubject?.subjectId === subject.subjectId ? 'rgba(56, 189, 248, 0.65)' : 'rgba(255,255,255,0.06)'
                  }}
                >
                  <span style={styles.subjectTopline}>
                    <strong>{subject.touristId}</strong>
                    <span className={badgeClassForSubject(subject)}>{subject.status}</span>
                  </span>
                  <span style={styles.subjectMeta}>{subject.tripId || subject.incidentId} - {subject.staleStatus}</span>
                  <span style={styles.subjectMeta}>{formatDateTime(subject.lastSeenAt)} - {formatMeters(subject.accuracyMeters)}</span>
                </button>
              ))}
              {subjects.length === 0 && (
                <p style={styles.emptyText}>No active trip monitoring data is available yet.</p>
              )}
            </div>
          </section>

          <section className="glass-panel" style={styles.panel}>
            <div style={styles.panelHeader}>
              <h2 style={styles.panelTitle}><AlertCircle size={20} /> Subject Detail</h2>
              <span className={selectedSubject ? badgeClassForSubject(selectedSubject) : 'badge badge-purple'}>
                {selectedSubject?.staleStatus || 'NO SELECTION'}
              </span>
            </div>

            {selectedSubject ? (
              <div style={styles.detailStack}>
                <DetailRow label="Tourist ID" value={selectedSubject.touristId} highlight />
                <DetailRow label="ID Hash" value={previewHash(selectedSubject.idHash)} />
                <DetailRow label="Trip ID" value={selectedSubject.tripId || 'Not linked'} />
                <DetailRow label="Last Known Safety Location" value={formatPosition(selectedSubject)} />
                <DetailRow label="Accuracy" value={formatMeters(selectedSubject.accuracyMeters)} />
                <DetailRow label="Battery" value={selectedSubject.batteryPercent == null ? 'Unknown' : `${selectedSubject.batteryPercent}%`} />
                <DetailRow label="Last Seen" value={formatDateTime(selectedSubject.lastSeenAt)} />
                <DetailRow label="Source" value={selectedSubject.source || 'Unknown'} />
                <DetailRow label="Route / Zone" value={`${selectedSubject.plannedRouteId || 'No route'} / ${selectedSubject.currentZoneId || 'No zone'}`} />
                <DetailRow label="Risk Score" value={selectedSubject.riskScore == null ? 'Unknown' : `${selectedSubject.riskScore}/100`} />

                {selectedSubject.incidentId && (
                  <div style={styles.stateMachine}>
                    <span style={styles.sectionLabel}>Incident State Machine</span>
                    <div style={styles.stateButtons}>
                      {INCIDENT_STATES.map(status => (
                        <button
                          key={status}
                          type="button"
                          onClick={() => handleUpdateIncidentState(status)}
                          style={{
                            ...styles.stateButton,
                            background: selectedSubject.incidentStatus === status ? 'var(--primary-gradient)' : 'rgba(255,255,255,0.08)',
                            color: selectedSubject.incidentStatus === status ? '#FFFFFF' : 'var(--text-muted)'
                          }}
                        >
                          {status}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                <button className={selectedSubject.status === 'SOS' ? 'btn-danger' : 'btn-primary'} style={styles.fullButton} onClick={handleMatchResponders}>
                  <Navigation size={18} /> Evaluate Rescueability
                </button>
              </div>
            ) : (
              <p style={styles.emptyText}>Select an active trip or SOS incident to inspect last-known safety data.</p>
            )}
          </section>

          {rescueEvaluation && (
            <section className="glass-panel" style={styles.panel}>
              <h2 style={styles.panelTitle}><Compass size={20} /> Rescueability</h2>
              <ResponderEvaluation evaluation={rescueEvaluation} />
            </section>
          )}

          <section className="glass-panel" style={styles.panel}>
            <h2 style={styles.panelTitle}><CheckCircle2 size={20} /> Voucher Verification</h2>
            <div style={styles.verifyRow}>
              <input
                value={verifyInput}
                onChange={event => setVerifyInput(event.target.value)}
                placeholder="Paste idHash"
                style={styles.input}
              />
              <button className="btn-primary" style={styles.compactButton} onClick={handleVerifyContract}>Verify</button>
            </div>
            {verifyResult && (
              <p style={styles.verifyResult}>
                {verifyResult.valid || verifyResult.isValid ? 'Voucher active' : `Voucher not active: ${verifyResult.reason || 'unknown'}`}
              </p>
            )}
          </section>
        </aside>
      </main>
    </div>
  );
}

function MetricCard({ label, value, detail, tone, icon }) {
  const color = tone === 'danger'
    ? 'var(--accent-rose)'
    : tone === 'safe'
      ? 'var(--accent-emerald)'
      : tone === 'cyan'
        ? 'var(--primary-cyan)'
        : 'var(--text-main)';

  return (
    <div className="glass-panel" style={styles.metricCard}>
      <span style={styles.metricLabel}>{icon} {label}</span>
      <h2 style={{ ...styles.metricValue, color }}>{value}</h2>
      <span style={styles.metricDetail}>{detail}</span>
    </div>
  );
}

function DetailRow({ label, value, highlight }) {
  return (
    <div style={styles.detailRow}>
      <span style={styles.detailLabel}>{label}</span>
      <strong style={{ ...styles.detailValue, color: highlight ? 'var(--primary-cyan)' : 'var(--text-main)' }}>{value || 'Unknown'}</strong>
    </div>
  );
}

function ResponderEvaluation({ evaluation }) {
  return (
    <div style={styles.detailStack}>
      {evaluation.geographicallyNearest && (
        <div className="glass-card" style={{ borderLeft: '3px solid var(--accent-amber)' }}>
          <span style={styles.sectionLabel}>Geographically Nearest</span>
          <strong>{evaluation.geographicallyNearest.name}</strong>
          <p style={styles.cardText}>{evaluation.geographicallyNearest.geoDistanceKm} km from last-known location.</p>
          {evaluation.geographicallyNearest.isBlocked && (
            <p style={styles.warningText}><AlertOctagon size={14} /> Impassable: {evaluation.geographicallyNearest.blockageReason}</p>
          )}
        </div>
      )}
      {evaluation.operationallyRecommended && (
        <div className="glass-card" style={{ borderLeft: '3px solid var(--accent-emerald)' }}>
          <span style={styles.sectionLabel}>Operationally Recommended</span>
          <strong>{evaluation.operationallyRecommended.name}</strong>
          <p style={styles.cardText}>ETA {evaluation.operationallyRecommended.feasibleETAMins} mins - {evaluation.operationallyRecommended.geoDistanceKm} km.</p>
          <button className="btn-primary" style={styles.compactButton}><PhoneCall size={14} /> Dispatch Unit</button>
        </div>
      )}
      {evaluation.divergenceExplanation && (
        <div className="glass-card" style={{ background: 'rgba(245, 158, 11, 0.1)', borderColor: 'rgba(245, 158, 11, 0.3)' }}>
          <span style={styles.sectionLabel}>Operational Divergence</span>
          <p style={styles.cardText}>{evaluation.divergenceExplanation}</p>
        </div>
      )}
    </div>
  );
}

function renderGeofence(gf) {
  const coords = gf.coordinates_json || gf.coordinates || gf.coords;
  if (!coords) return null;
  const color = gf.color || (gf.riskLevel === 'HIGH_RISK' ? '#EF4444' : '#F59E0B');
  return (
    <Polygon key={gf.id} positions={coords} pathOptions={{ color, fillColor: color, fillOpacity: 0.18, weight: 2 }}>
      <Popup>
        <strong>{gf.name}</strong><br />
        Risk level: {gf.riskLevel || 'Unknown'}
      </Popup>
    </Polygon>
  );
}

function renderHazard(hazard) {
  const lat = Number(hazard.lat);
  const lon = Number(hazard.lon);
  if (!Number.isFinite(lat) || !Number.isFinite(lon)) return null;
  return (
    <Marker key={hazard.id} position={[lat, lon]} icon={iconHazard}>
      <Popup>
        <strong>{hazard.hazardType || hazard.type || 'Hazard'}</strong><br />
        {hazard.description || hazard.status || 'Reported hazard'}
      </Popup>
    </Marker>
  );
}

function renderResponder(responder) {
  const lat = Number(responder.lat);
  const lon = Number(responder.lon);
  if (!Number.isFinite(lat) || !Number.isFinite(lon)) return null;
  return (
    <Marker key={responder.id} position={[lat, lon]} icon={iconResponder}>
      <Popup>
        <strong>{responder.name}</strong><br />
        Status: {responder.status || 'AVAILABLE'}<br />
        Vehicle: {responder.vehicle || responder.type || 'Unit'}
      </Popup>
    </Marker>
  );
}

function renderSearchSectors(searchProbability) {
  return searchProbability?.topSearchSectors?.map(sector => (
    <Polygon
      key={sector.sectorId}
      positions={sector.bounds}
      pathOptions={{ color: '#8B5CF6', fillColor: '#8B5CF6', fillOpacity: 0.3, weight: 2, dashArray: '4, 4' }}
    >
      <Popup>
        <strong>{sector.name} estimate ({sector.probabilityPercent}%)</strong><br />
        {sector.explanation}
      </Popup>
    </Polygon>
  ));
}

function LegendDot({ color, label }) {
  return (
    <span style={styles.legendItem}>
      <span style={{ ...styles.legendDot, background: color }} /> {label}
    </span>
  );
}

function LegendLine({ color, label }) {
  return (
    <span style={styles.legendItem}>
      <span style={{ ...styles.legendLine, background: color }} /> {label}
    </span>
  );
}

function badgeClassForSubject(subject) {
  if (subject.status === 'SOS' || subject.staleStatus === 'EMERGENCY_STALE') return 'badge badge-danger';
  if (subject.staleStatus === 'STALE' || subject.staleStatus === 'RECENT') return 'badge badge-caution';
  if (subject.status === 'RESOLVED') return 'badge badge-purple';
  return 'badge badge-safe';
}

function formatPosition(subject) {
  if (!Number.isFinite(subject.lat) || !Number.isFinite(subject.lon)) return 'Unknown';
  return `${subject.lat.toFixed(4)}, ${subject.lon.toFixed(4)}`;
}

function formatMeters(value) {
  return value == null ? 'Unknown accuracy' : `${Math.round(value)} m`;
}

function formatDateTime(value) {
  if (!value) return 'Unknown';
  return new Intl.DateTimeFormat(undefined, {
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value));
}

function previewHash(hash) {
  if (!hash) return 'Not available';
  return hash.length > 18 ? `${hash.slice(0, 10)}...${hash.slice(-6)}` : hash;
}

const styles = {
  shell: {
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
    backgroundColor: 'var(--bg-dark)'
  },
  header: {
    margin: '1rem',
    padding: '1rem 1.5rem',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: '1rem',
    flexWrap: 'wrap'
  },
  brandGroup: {
    display: 'flex',
    alignItems: 'center',
    gap: '1rem'
  },
  brandMark: {
    background: 'var(--primary-gradient)',
    padding: '0.6rem',
    borderRadius: '12px',
    display: 'flex'
  },
  title: {
    fontSize: '1.4rem',
    fontWeight: 800,
    color: 'var(--text-main)'
  },
  subtitle: {
    fontSize: '0.82rem',
    color: 'var(--text-muted)'
  },
  headerBadges: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    flexWrap: 'wrap'
  },
  mainGrid: {
    flex: 1,
    display: 'grid',
    gridTemplateColumns: 'minmax(0, 1fr) minmax(360px, 430px)',
    gap: '1rem',
    padding: '0 1rem 1rem'
  },
  leftColumn: {
    display: 'flex',
    flexDirection: 'column',
    gap: '1rem',
    minWidth: 0
  },
  rightColumn: {
    display: 'flex',
    flexDirection: 'column',
    gap: '1rem',
    minWidth: 0
  },
  statsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
    gap: '1rem'
  },
  metricCard: {
    padding: '1rem',
    minHeight: 116
  },
  metricLabel: {
    fontSize: '0.75rem',
    color: 'var(--text-muted)',
    display: 'flex',
    alignItems: 'center',
    gap: '0.4rem',
    textTransform: 'uppercase'
  },
  metricValue: {
    fontSize: '1.55rem',
    marginTop: '0.25rem'
  },
  metricDetail: {
    fontSize: '0.72rem',
    color: 'var(--text-muted)'
  },
  mapPanel: {
    flex: 1,
    minHeight: 560,
    overflow: 'hidden',
    position: 'relative'
  },
  map: {
    height: '100%',
    width: '100%',
    minHeight: 560
  },
  legend: {
    position: 'absolute',
    bottom: 20,
    left: 20,
    zIndex: 1000,
    background: 'rgba(9, 13, 22, 0.86)',
    backdropFilter: 'blur(8px)',
    padding: '0.75rem 1rem',
    borderRadius: 10,
    border: '1px solid var(--border-color)',
    display: 'flex',
    gap: '0.8rem',
    flexWrap: 'wrap',
    fontSize: '0.78rem'
  },
  legendItem: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.38rem',
    color: 'var(--text-main)'
  },
  legendDot: {
    width: 10,
    height: 10,
    borderRadius: '50%',
    display: 'inline-block'
  },
  legendLine: {
    width: 16,
    height: 3,
    display: 'inline-block'
  },
  panel: {
    padding: '1.1rem',
    display: 'flex',
    flexDirection: 'column',
    gap: '1rem'
  },
  panelHeader: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: '0.75rem'
  },
  panelTitle: {
    fontSize: '1rem',
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem'
  },
  compactButton: {
    padding: '0.42rem 0.72rem',
    fontSize: '0.75rem'
  },
  fullButton: {
    width: '100%',
    justifyContent: 'center',
    marginTop: '0.25rem'
  },
  subjectList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.65rem',
    maxHeight: 315,
    overflowY: 'auto'
  },
  subjectButton: {
    width: '100%',
    textAlign: 'left',
    background: 'rgba(30, 41, 59, 0.55)',
    border: '1px solid rgba(255,255,255,0.06)',
    borderRadius: 10,
    padding: '0.75rem',
    color: 'var(--text-main)',
    cursor: 'pointer',
    display: 'flex',
    flexDirection: 'column',
    gap: '0.35rem'
  },
  subjectTopline: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '0.5rem'
  },
  subjectMeta: {
    fontSize: '0.76rem',
    color: 'var(--text-muted)'
  },
  detailStack: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.72rem'
  },
  detailRow: {
    display: 'flex',
    justifyContent: 'space-between',
    gap: '1rem',
    borderBottom: '1px solid rgba(255,255,255,0.06)',
    paddingBottom: '0.45rem'
  },
  detailLabel: {
    color: 'var(--text-muted)',
    fontSize: '0.82rem'
  },
  detailValue: {
    fontSize: '0.84rem',
    textAlign: 'right',
    overflowWrap: 'anywhere'
  },
  stateMachine: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.45rem',
    background: 'rgba(15, 23, 42, 0.6)',
    padding: '0.75rem',
    borderRadius: 10
  },
  sectionLabel: {
    fontSize: '0.72rem',
    color: 'var(--text-muted)',
    fontWeight: 700,
    textTransform: 'uppercase'
  },
  stateButtons: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: '0.4rem'
  },
  stateButton: {
    padding: '0.32rem 0.48rem',
    fontSize: '0.68rem',
    borderRadius: 6,
    border: 'none',
    cursor: 'pointer',
    fontWeight: 700
  },
  verifyRow: {
    display: 'grid',
    gridTemplateColumns: '1fr auto',
    gap: '0.5rem'
  },
  input: {
    minWidth: 0,
    background: 'rgba(15, 23, 42, 0.78)',
    border: '1px solid var(--border-color)',
    borderRadius: 8,
    padding: '0.55rem 0.7rem',
    color: 'var(--text-main)'
  },
  verifyResult: {
    color: 'var(--text-muted)',
    fontSize: '0.82rem'
  },
  emptyText: {
    color: 'var(--text-muted)',
    fontSize: '0.88rem'
  },
  cardText: {
    color: 'var(--text-muted)',
    fontSize: '0.78rem',
    marginTop: '0.28rem'
  },
  warningText: {
    color: 'var(--accent-rose)',
    fontSize: '0.78rem',
    marginTop: '0.35rem',
    display: 'flex',
    alignItems: 'center',
    gap: '0.3rem'
  }
};
