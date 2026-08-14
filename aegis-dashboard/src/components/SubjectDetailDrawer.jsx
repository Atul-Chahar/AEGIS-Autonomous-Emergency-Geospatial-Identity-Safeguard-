import React, { useState } from 'react';
import {
  Activity,
  AlertCircle,
  AlertOctagon,
  AlertTriangle,
  ArrowRight,
  Battery,
  BatteryCharging,
  BatteryWarning,
  CheckCircle2,
  Clock,
  Compass,
  Copy,
  Footprints,
  Layers,
  MapPin,
  Mountain,
  Navigation,
  PhoneCall,
  Radio,
  Route,
  Shield,
  ShieldAlert,
  ShieldCheck,
  Zap
} from 'lucide-react';

const INCIDENT_STATES = ['OPEN', 'ACKNOWLEDGED', 'TEAM_DISPATCHED', 'SEARCHING', 'LOCATED', 'RESOLVED'];

// Route metadata lookup for Cherrapunji trekking trails
const ROUTE_METADATA = {
  'Nongriat Double Decker Living Root Trail': {
    distance: '4.8 km',
    duration: '3.5 hrs',
    difficulty: 'Moderate - High',
    elevation: '+520m (3500 stone steps)',
    trailType: 'Rainforest River Valley Corridor',
    guidePost: 'Tyrna Checkpost #01'
  },
  'Nongriat Living Root Bridge Trail': {
    distance: '4.8 km',
    duration: '3.5 hrs',
    difficulty: 'Moderate - High',
    elevation: '+520m (3500 stone steps)',
    trailType: 'Rainforest River Valley Corridor',
    guidePost: 'Tyrna Checkpost #01'
  },
  'Mawsmai Ridge Trek': {
    distance: '3.2 km',
    duration: '2.0 hrs',
    difficulty: 'Easy - Moderate',
    elevation: '+140m',
    trailType: 'Limestone Plateau & Fog Escarpment',
    guidePost: 'Sohra South Ranger Station'
  },
  'Cherrapunji Canyon Ridge': {
    distance: '5.6 km',
    duration: '3.0 hrs',
    difficulty: 'Moderate',
    elevation: '+210m',
    trailType: 'Highland Ridge Escarpment',
    guidePost: 'Cherrapunji Eco Outpost'
  },
  'Nohkalikai Canyon Descent': {
    distance: '2.8 km',
    duration: '2.5 hrs',
    difficulty: 'High Risk / Severe',
    elevation: '-340m (Vertical Canyon Drop)',
    trailType: 'Steep Gorge & Flash-Flood Basin',
    guidePost: 'Nohkalikai Emergency Dispatch Base'
  },
  'Dawki River Border Pass': {
    distance: '6.4 km',
    duration: '3.0 hrs',
    difficulty: 'High Risk',
    elevation: '-180m',
    trailType: 'River Basin Gorge',
    guidePost: 'Dawki Border Checkpost'
  },
  'Cherrapunji Town & Eco Park Hub': {
    distance: '2.0 km',
    duration: '1.0 hr',
    difficulty: 'Easy',
    elevation: '+40m',
    trailType: 'Paved Urban Safe Passage',
    guidePost: 'Sohra Central Tourist Hub'
  }
};

export default function SubjectDetailDrawer({
  selectedSubject,
  onUpdateIncidentState,
  onEvaluateRescue,
  rescueEvaluation,
  verifyInput,
  onVerifyInputChange,
  onVerifyContract,
  verifyResult
}) {
  const [copied, setCopied] = useState(false);
  const [activeTab, setActiveTab] = useState('overview'); // 'overview' | 'route' | 'history'

  const handleCopyHash = () => {
    if (selectedSubject?.idHash) {
      navigator.clipboard.writeText(selectedSubject.idHash);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  if (!selectedSubject) {
    return (
      <div className="glass-panel" style={{ padding: '3rem 2rem', textAlign: 'center', color: 'var(--text-muted)', height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <Navigation size={36} color="var(--primary-cyan)" style={{ marginBottom: '1rem', opacity: 0.7 }} />
        <h3 style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--text-bright)', marginBottom: '0.4rem' }}>
          Select a Tourist to Inspect
        </h3>
        <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', maxWidth: '340px' }}>
          Click any active tourist in the <strong>Active Tourists & Trips</strong> tab to center the map and view real-time trip itinerary, battery health, and telemetry.
        </p>
      </div>
    );
  }

  const isSos = selectedSubject.status === 'SOS' || selectedSubject.staleStatus === 'EMERGENCY_STALE';
  const routeName = selectedSubject.plannedRouteId || 'Not specified';
  // Static route metadata is only meaningful for a REAL chosen route;
  // without one we must not fabricate distance/duration figures.
  const routeInfo = routeName !== 'Not specified' ? (ROUTE_METADATA[routeName] || null) : null;

  const batteryPct = selectedSubject.batteryPercent != null ? selectedSubject.batteryPercent : null;
  const isBatteryLow = batteryPct != null && batteryPct <= 20;
  const isBatteryMedium = batteryPct != null && batteryPct > 20 && batteryPct <= 50;

  const trajectory = selectedSubject.trajectory || [];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', height: '100%' }}>
      {/* 1. Header Profile Glass Card */}
      <div className="glass-panel" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1.15rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
            <div
              style={{
                width: 48,
                height: 48,
                borderRadius: 14,
                background: isSos ? 'rgba(225, 29, 72, 0.15)' : 'rgba(2, 132, 199, 0.12)',
                color: isSos ? '#E11D48' : '#0284C7',
                border: isSos ? '1px solid rgba(225, 29, 72, 0.3)' : '1px solid rgba(2, 132, 199, 0.3)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: 800,
                boxShadow: 'var(--glass-shadow-sm)'
              }}
            >
              {isSos ? <AlertTriangle size={24} /> : <MapPin size={24} />}
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                <h2 style={{ fontSize: '1.35rem', fontWeight: 800, color: 'var(--text-bright)' }}>
                  {selectedSubject.touristId}
                </h2>
                <span className={isSos ? 'badge badge-danger' : 'badge badge-safe'} style={{ fontSize: '0.78rem', padding: '0.3rem 0.75rem' }}>
                  {selectedSubject.status}
                </span>
              </div>
              <span style={{ fontSize: '0.82rem', color: 'var(--text-dim)', fontWeight: 600 }}>
                {selectedSubject.tripId
                  ? `Trip ID: ${selectedSubject.tripId}`
                  : selectedSubject.incidentId
                    ? `Incident ID: ${selectedSubject.incidentId}`
                    : 'Direct Active Track'}
              </span>
            </div>
          </div>

          <div style={{ textAlign: 'right' }}>
            <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)', fontWeight: 700, letterSpacing: '0.04em', display: 'block' }}>
              TELEMETRY STATUS
            </span>
            <span className="badge badge-cyan" style={{ fontSize: '0.75rem', marginTop: '0.2rem', padding: '0.25rem 0.65rem' }}>
              {selectedSubject.staleStatus || 'LIVE'}
            </span>
          </div>
        </div>

        {/* Sub-tab Navigation */}
        <div style={{ display: 'flex', gap: '0.45rem', background: 'rgba(255, 255, 255, 0.45)', padding: '0.35rem', borderRadius: 14, border: '1px solid rgba(255, 255, 255, 0.65)' }}>
          <button
            type="button"
            className={`glass-pill ${activeTab === 'overview' ? 'active' : ''}`}
            style={{ flex: 1, justifyContent: 'center', padding: '0.5rem 0.75rem', fontSize: '0.85rem' }}
            onClick={() => setActiveTab('overview')}
          >
            <Shield size={15} />
            <span>Overview</span>
          </button>
          <button
            type="button"
            className={`glass-pill ${activeTab === 'route' ? 'active' : ''}`}
            style={{ flex: 1, justifyContent: 'center', padding: '0.5rem 0.75rem', fontSize: '0.85rem' }}
            onClick={() => setActiveTab('route')}
          >
            <Route size={15} />
            <span>Chosen Route</span>
          </button>
          <button
            type="button"
            className={`glass-pill ${activeTab === 'history' ? 'active' : ''}`}
            style={{ flex: 1, justifyContent: 'center', padding: '0.5rem 0.75rem', fontSize: '0.85rem' }}
            onClick={() => setActiveTab('history')}
          >
            <Clock size={15} />
            <span>Trail History ({trajectory.length})</span>
          </button>
        </div>

        {/* TAB 1: OVERVIEW */}
        {activeTab === 'overview' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {/* Battery & Health Gauge Card */}
            <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem', padding: '1.15rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  {isBatteryLow ? (
                    <BatteryWarning size={18} color="#E11D48" />
                  ) : (
                    <Battery size={18} color={isBatteryMedium ? '#D97706' : '#059669'} />
                  )}
                  <span style={{ fontSize: '0.86rem', fontWeight: 700, color: 'var(--text-bright)' }}>
                    Device Battery Health & Runtime
                  </span>
                </div>
                <strong style={{ fontSize: '1.05rem', color: isBatteryLow ? '#E11D48' : isBatteryMedium ? '#D97706' : '#059669' }}>
                  {batteryPct != null ? `${batteryPct}%` : '--'}
                </strong>
              </div>

              {/* Progress bar (only when real battery telemetry exists) */}
              <div style={{ height: 8, width: '100%', background: 'rgba(0, 0, 0, 0.06)', borderRadius: 5, overflow: 'hidden' }}>
                <div
                  style={{
                    height: '100%',
                    width: `${batteryPct != null ? batteryPct : 0}%`,
                    background: isBatteryLow ? 'linear-gradient(90deg, #E11D48, #BE123C)' : isBatteryMedium ? 'linear-gradient(90deg, #D97706, #F59E0B)' : 'linear-gradient(90deg, #059669, #10B981)',
                    borderRadius: 5
                  }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.78rem', color: 'var(--text-dim)' }}>
                <span>{batteryPct == null ? 'No battery telemetry yet' : isBatteryLow ? 'Critical Low (BLE beacon power-save mode)' : isBatteryMedium ? 'Normal discharge rate' : 'Optimal power level'}</span>
                <span style={{ fontWeight: 600, color: 'var(--text-muted)' }}>{batteryPct == null ? '--' : isBatteryLow ? '~1.5 hrs remaining' : '~14 hrs remaining'}</span>
              </div>
            </div>

            {/* Live Attributes Grid */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.7rem', fontSize: '0.86rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid rgba(255, 255, 255, 0.5)', paddingBottom: '0.45rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Live GPS Position</span>
                <strong className="font-mono" style={{ color: 'var(--text-bright)' }}>
                  {formatPosition(selectedSubject)}
                </strong>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid rgba(255, 255, 255, 0.5)', paddingBottom: '0.45rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Horizontal Accuracy Radius</span>
                <span style={{ color: 'var(--text-bright)', fontWeight: 600 }}>{formatMeters(selectedSubject.accuracyMeters)}</span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid rgba(255, 255, 255, 0.5)', paddingBottom: '0.45rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Last Telemetry Timestamp</span>
                <span style={{ color: 'var(--text-bright)', fontWeight: 600 }}>{formatDateTime(selectedSubject.lastSeenAt)}</span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid rgba(255, 255, 255, 0.5)', paddingBottom: '0.45rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Transmission Channel</span>
                <span className="font-mono" style={{ fontSize: '0.82rem', color: 'var(--primary-cyan)', fontWeight: 700 }}>
                  {selectedSubject.source || 'BLE_MESH_RELAY'}
                </span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid rgba(255, 255, 255, 0.5)', paddingBottom: '0.45rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Current Geofence Zone</span>
                <span style={{ color: 'var(--text-bright)', fontWeight: 700 }}>
                  {selectedSubject.currentZoneId || 'Not determined'}
                </span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Cryptographic ID Hash</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                  <span className="font-mono" style={{ fontSize: '0.78rem', color: 'var(--primary-cyan)', fontWeight: 600 }}>
                    {previewHash(selectedSubject.idHash)}
                  </span>
                  {selectedSubject.idHash && (
                    <button
                      type="button"
                      onClick={handleCopyHash}
                      style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-dim)' }}
                      title="Copy Hash"
                    >
                      <Copy size={14} color={copied ? '#059669' : 'var(--text-muted)'} />
                    </button>
                  )}
                </div>
              </div>
            </div>

            {/* Risk Score Progress */}
            {selectedSubject.riskScore != null && (
              <div className="glass-card" style={{ padding: '0.9rem 1.1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
                  <span style={{ fontSize: '0.78rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                    Safety Risk Index
                  </span>
                  <strong style={{ fontSize: '0.95rem', color: selectedSubject.riskScore > 60 ? '#E11D48' : selectedSubject.riskScore > 30 ? '#D97706' : '#059669' }}>
                    {selectedSubject.riskScore} / 100
                  </strong>
                </div>
                <div style={{ height: 8, width: '100%', background: 'rgba(0, 0, 0, 0.06)', borderRadius: 4, overflow: 'hidden' }}>
                  <div
                    style={{
                      height: '100%',
                      width: `${Math.min(selectedSubject.riskScore, 100)}%`,
                      background: selectedSubject.riskScore > 60 ? '#E11D48' : selectedSubject.riskScore > 30 ? '#D97706' : '#059669'
                    }}
                  />
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 2: CHOSEN ROUTE */}
        {activeTab === 'route' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div className="glass-card" style={{ borderLeft: '4px solid var(--primary-cyan)', padding: '1.25rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem' }}>
                <Route size={18} color="var(--primary-cyan)" />
                <h3 style={{ fontSize: '1.05rem', fontWeight: 800, color: 'var(--text-bright)' }}>
                  {routeName}
                </h3>
              </div>
              <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                {routeInfo.trailType}
              </p>

              {routeInfo ? (
                <>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', fontSize: '0.82rem' }}>
                    <div style={{ background: 'rgba(255, 255, 255, 0.55)', padding: '0.65rem 0.75rem', borderRadius: 12 }}>
                      <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', display: 'block', fontWeight: 700 }}>TOTAL DISTANCE</span>
                      <strong style={{ color: 'var(--text-bright)', fontSize: '0.92rem' }}>{routeInfo.distance}</strong>
                    </div>
                    <div style={{ background: 'rgba(255, 255, 255, 0.55)', padding: '0.65rem 0.75rem', borderRadius: 12 }}>
                      <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', display: 'block', fontWeight: 700 }}>EST. DURATION</span>
                      <strong style={{ color: 'var(--text-bright)', fontSize: '0.92rem' }}>{routeInfo.duration}</strong>
                    </div>
                    <div style={{ background: 'rgba(255, 255, 255, 0.55)', padding: '0.65rem 0.75rem', borderRadius: 12 }}>
                      <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', display: 'block', fontWeight: 700 }}>TRAIL DIFFICULTY</span>
                      <strong style={{ color: routeInfo.difficulty.includes('High') ? '#E11D48' : '#059669', fontSize: '0.92rem' }}>
                        {routeInfo.difficulty}
                      </strong>
                    </div>
                    <div style={{ background: 'rgba(255, 255, 255, 0.55)', padding: '0.65rem 0.75rem', borderRadius: 12 }}>
                      <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', display: 'block', fontWeight: 700 }}>ELEVATION CHANGE</span>
                      <strong style={{ color: 'var(--text-bright)', fontSize: '0.92rem' }}>{routeInfo.elevation}</strong>
                    </div>
                  </div>

                  <div style={{ marginTop: '1rem', padding: '0.65rem 0.85rem', background: 'rgba(2, 132, 199, 0.08)', borderRadius: 12, fontSize: '0.8rem', color: 'var(--primary-cyan)', display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                    <ShieldCheck size={16} />
                    <span>Primary Ranger Outpost: <strong>{routeInfo.guidePost}</strong></span>
                  </div>
                </>
              ) : (
                <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                  No itinerary route is linked to this subject yet. Route metadata will appear once a
                  route is planned on the device.
                </p>
              )}
            </div>
          </div>
        )}

        {/* TAB 3: BREADCRUMB GPS TRAIL HISTORY */}
        {activeTab === 'history' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem', maxHeight: '340px', overflowY: 'auto' }}>
            <span style={{ fontSize: '0.76rem', color: 'var(--text-dim)', fontWeight: 700, textTransform: 'uppercase' }}>
              Chronological Breadcrumb Waypoints
            </span>
            {trajectory.map((crumb, idx) => (
              <div
                key={crumb.id || idx}
                style={{
                  background: 'rgba(255, 255, 255, 0.55)',
                  border: '1px solid rgba(255, 255, 255, 0.7)',
                  borderRadius: 14,
                  padding: '0.75rem 0.95rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  fontSize: '0.82rem'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                  <div
                    style={{
                      width: 26,
                      height: 26,
                      borderRadius: '50%',
                      background: idx === trajectory.length - 1 ? 'var(--primary-cyan)' : 'rgba(0, 0, 0, 0.08)',
                      color: idx === trajectory.length - 1 ? '#FFFFFF' : 'var(--text-dim)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '0.75rem',
                      fontWeight: 800
                    }}
                  >
                    {idx + 1}
                  </div>
                  <div>
                    <strong className="font-mono" style={{ color: 'var(--text-bright)', fontSize: '0.84rem' }}>
                      {Number(crumb.lat).toFixed(4)}°N, {Number(crumb.lon).toFixed(4)}°E
                    </strong>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)', display: 'block' }}>
                      {formatDateTime(crumb.timestamp || crumb.createdAt)}
                    </span>
                  </div>
                </div>

                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: '0.76rem', color: 'var(--text-main)', fontWeight: 600 }}>
                    ±{Math.round(crumb.accuracyMeters || 5)}m
                  </span>
                  {crumb.batteryPercent != null && (
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)', display: 'block' }}>
                      {crumb.batteryPercent}% batt
                    </span>
                  )}
                </div>
              </div>
            ))}

            {trajectory.length === 0 && (
              <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', textAlign: 'center', padding: '1.5rem' }}>
                No historical breadcrumb trail logged yet for this trip.
              </p>
            )}
          </div>
        )}

        {/* Incident Lifecycle State Machine (If SOS Alert) */}
        {selectedSubject.incidentId && (
          <div style={{ background: 'rgba(255, 255, 255, 0.65)', border: '1px solid rgba(255, 255, 255, 0.75)', borderRadius: 16, padding: '1rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Incident Lifecycle State Machine
            </span>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.45rem', marginTop: '0.6rem' }}>
              {INCIDENT_STATES.map(status => {
                const isCurrent = selectedSubject.incidentStatus === status;
                return (
                  <button
                    key={status}
                    type="button"
                    onClick={() => onUpdateIncidentState(status)}
                    style={{
                      padding: '0.5rem 0.25rem',
                      fontSize: '0.7rem',
                      fontWeight: 700,
                      borderRadius: 10,
                      border: '1px solid',
                      borderColor: isCurrent ? 'var(--primary-cyan)' : 'rgba(255, 255, 255, 0.7)',
                      background: isCurrent ? 'var(--primary-gradient)' : 'rgba(255, 255, 255, 0.6)',
                      color: isCurrent ? '#FFFFFF' : 'var(--text-muted)',
                      cursor: 'pointer',
                      transition: 'all 0.15s ease',
                      boxShadow: isCurrent ? '0 4px 14px rgba(2, 132, 199, 0.25)' : 'none'
                    }}
                  >
                    {status}
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {/* Evaluate Rescueability Action Button */}
        <button
          type="button"
          className={isSos ? 'btn-danger' : 'btn-primary'}
          style={{ width: '100%', justifyContent: 'center', padding: '0.75rem', fontSize: '0.92rem' }}
          onClick={onEvaluateRescue}
        >
          <Compass size={18} />
          <span>Evaluate Rescueability & AI Match</span>
        </button>
      </div>

      {/* 2. Rescueability AI Evaluation Card */}
      {rescueEvaluation && (
        <div className="glass-panel" style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Zap size={18} color="var(--primary-cyan)" />
            <h4 style={{ fontSize: '0.98rem', fontWeight: 700, color: 'var(--text-bright)' }}>
              Operational Rescue Matching
            </h4>
          </div>

          {rescueEvaluation.geographicallyNearest && (
            <div className="glass-card" style={{ borderLeft: '4px solid var(--accent-amber)', padding: '0.95rem' }}>
              <span style={{ fontSize: '0.72rem', color: 'var(--accent-amber)', fontWeight: 700, textTransform: 'uppercase' }}>
                Geographically Nearest
              </span>
              <div style={{ fontWeight: 700, fontSize: '0.92rem', marginTop: '0.2rem' }}>
                {rescueEvaluation.geographicallyNearest.name}
              </div>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                {rescueEvaluation.geographicallyNearest.geoDistanceKm} km straight-line distance.
              </p>
              {rescueEvaluation.geographicallyNearest.isBlocked && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--accent-rose)', fontSize: '0.78rem', marginTop: '0.4rem' }}>
                  <AlertOctagon size={15} />
                  <span>Impassable: {rescueEvaluation.geographicallyNearest.blockageReason}</span>
                </div>
              )}
            </div>
          )}

          {rescueEvaluation.operationallyRecommended && (
            <div className="glass-card" style={{ borderLeft: '4px solid var(--accent-emerald)', background: 'rgba(5, 150, 105, 0.08)', padding: '0.95rem' }}>
              <span style={{ fontSize: '0.72rem', color: 'var(--accent-emerald)', fontWeight: 700, textTransform: 'uppercase' }}>
                Operationally Recommended
              </span>
              <div style={{ fontWeight: 700, fontSize: '0.92rem', marginTop: '0.2rem' }}>
                {rescueEvaluation.operationallyRecommended.name}
              </div>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                ETA {rescueEvaluation.operationallyRecommended.feasibleETAMins} mins ({rescueEvaluation.operationallyRecommended.geoDistanceKm} km feasible road).
              </p>
              <button
                type="button"
                className="btn-primary"
                style={{ marginTop: '0.75rem', width: '100%', justifyContent: 'center', padding: '0.55rem', fontSize: '0.82rem' }}
              >
                <PhoneCall size={14} /> Dispatch Recommended SDRF Unit
              </button>
            </div>
          )}

          {rescueEvaluation.divergenceExplanation && (
            <div className="glass-card" style={{ background: 'rgba(217, 119, 6, 0.08)', borderColor: 'rgba(217, 119, 6, 0.25)', padding: '0.95rem' }}>
              <span style={{ fontSize: '0.72rem', color: 'var(--accent-amber)', fontWeight: 700, textTransform: 'uppercase' }}>
                Divergence Insight
              </span>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.3rem' }}>
                {rescueEvaluation.divergenceExplanation}
              </p>
            </div>
          )}
        </div>
      )}

      {/* 3. Blockchain Cryptographic Voucher Verification */}
      <div className="glass-panel" style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <ShieldCheck size={18} color="var(--primary-cyan)" />
          <h4 style={{ fontSize: '0.98rem', fontWeight: 700, color: 'var(--text-bright)' }}>
            Cryptographic Voucher Verifier
          </h4>
        </div>
        <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
          Verify tourist identity commitment on Sepolia/Amoy testnet without revealing raw PII.
        </p>

        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <input
            type="text"
            className="topbar-search-input"
            style={{
              background: 'rgba(255, 255, 255, 0.65)',
              border: '1px solid rgba(255, 255, 255, 0.75)',
              borderRadius: 12,
              padding: '0.6rem 0.85rem',
              fontSize: '0.82rem',
              flex: 1
            }}
            placeholder="Paste keccak256 idHash"
            value={verifyInput}
            onChange={e => onVerifyInputChange(e.target.value)}
          />
          <button
            type="button"
            className="btn-primary"
            style={{ padding: '0.6rem 1rem', fontSize: '0.82rem' }}
            onClick={onVerifyContract}
          >
            Verify
          </button>
        </div>

        {verifyResult && (
          <div
            style={{
              padding: '0.75rem 1rem',
              borderRadius: 14,
              background: verifyResult.valid || verifyResult.isValid ? 'rgba(5, 150, 105, 0.12)' : 'rgba(225, 29, 72, 0.12)',
              border: '1px solid',
              borderColor: verifyResult.valid || verifyResult.isValid ? 'rgba(5, 150, 105, 0.35)' : 'rgba(225, 29, 72, 0.35)',
              fontSize: '0.82rem',
              color: verifyResult.valid || verifyResult.isValid ? '#059669' : '#E11D48',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              fontWeight: 600
            }}
          >
            {verifyResult.valid || verifyResult.isValid ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
            <span>
              {verifyResult.valid || verifyResult.isValid
                ? 'Valid Cryptographic Voucher on Blockchain'
                : `Voucher Invalid: ${verifyResult.reason || 'Not Found'}`}
            </span>
          </div>
        )}
      </div>
    </div>
  );
}

function formatPosition(subject) {
  if (!Number.isFinite(subject.lat) || !Number.isFinite(subject.lon)) return 'Unknown';
  return `${subject.lat.toFixed(4)}°N, ${subject.lon.toFixed(4)}°E`;
}

function formatMeters(value) {
  return value == null ? 'Unknown' : `±${Math.round(value)}m Precision`;
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
  if (!hash) return 'No Hash Linked';
  return hash.length > 16 ? `${hash.slice(0, 10)}...${hash.slice(-8)}` : hash;
}
