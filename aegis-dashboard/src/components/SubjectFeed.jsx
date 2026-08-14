import React from 'react';
import {
  AlertTriangle,
  Battery,
  MapPin,
  Users
} from 'lucide-react';

export default function SubjectFeed({
  subjects = [],
  selectedSubject = null,
  onSelectSubject,
  searchQuery = '',
  activeFilter = 'all'
}) {
  // Filter subjects based on search query and active tab filter
  const filteredSubjects = subjects.filter(subject => {
    // Search query filter
    const q = searchQuery.toLowerCase().trim();
    if (q) {
      const matchTourist = (subject.touristId || '').toLowerCase().includes(q);
      const matchTrip = (subject.tripId || '').toLowerCase().includes(q);
      const matchZone = (subject.currentZoneId || '').toLowerCase().includes(q);
      const matchRoute = (subject.plannedRouteId || '').toLowerCase().includes(q);
      if (!matchTourist && !matchTrip && !matchZone && !matchRoute) return false;
    }

    // Active filter pill
    if (activeFilter === 'sos') {
      return subject.status === 'SOS' || subject.staleStatus === 'EMERGENCY_STALE';
    }
    if (activeFilter === 'live') {
      return subject.staleStatus === 'LIVE';
    }
    if (activeFilter === 'stale') {
      return subject.isStale || subject.staleStatus === 'STALE' || subject.staleStatus === 'RECENT';
    }
    return true;
  });

  return (
    <div className="glass-panel" style={{ padding: '1.15rem', display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Users size={18} color="var(--primary-cyan)" />
          <h2 style={{ fontSize: '0.98rem', fontWeight: 700, color: 'var(--text-bright)' }}>
            Active Subjects ({filteredSubjects.length})
          </h2>
        </div>
        <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)', fontWeight: 600 }}>
          Real-Time GPS Feed
        </span>
      </div>

      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '0.55rem',
          maxHeight: selectedSubject ? '175px' : '420px',
          overflowY: 'auto',
          paddingRight: '2px',
          transition: 'max-height 0.25s ease'
        }}
      >
        {filteredSubjects.map(subject => {
          const isSelected = selectedSubject?.subjectId === subject.subjectId;
          const isSos = subject.status === 'SOS' || subject.staleStatus === 'EMERGENCY_STALE';
          
          let badgeClass = 'badge badge-safe';
          if (isSos) badgeClass = 'badge badge-danger';
          else if (subject.staleStatus === 'STALE' || subject.staleStatus === 'RECENT') badgeClass = 'badge badge-caution';
          else if (subject.status === 'RESOLVED') badgeClass = 'badge badge-purple';

          return (
            <button
              key={subject.subjectId}
              type="button"
              className={`subject-item-card ${isSelected ? 'selected' : ''}`}
              onClick={() => onSelectSubject(subject.subjectId)}
            >
              <div className="subject-header-row">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <div
                    style={{
                      width: 28,
                      height: 28,
                      borderRadius: 8,
                      background: isSos ? 'rgba(225, 29, 72, 0.12)' : 'rgba(2, 132, 199, 0.1)',
                      color: isSos ? '#E11D48' : '#0284C7',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '0.75rem',
                      fontWeight: 800
                    }}
                  >
                    {isSos ? <AlertTriangle size={14} /> : subject.touristId?.slice(-2) || 'T'}
                  </div>
                  <strong style={{ fontSize: '0.88rem', color: 'var(--text-bright)' }}>
                    {subject.touristId}
                  </strong>
                </div>
                <span className={badgeClass}>{subject.status}</span>
              </div>

              <div className="subject-meta-row">
                <span>{subject.tripId || subject.incidentId || 'Direct Track'} • {subject.staleStatus}</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                  {subject.batteryPercent != null && (
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
                      <Battery size={12} color={subject.batteryPercent < 20 ? '#E11D48' : 'var(--text-muted)'} />
                      {subject.batteryPercent}%
                    </span>
                  )}
                  {subject.accuracyMeters != null && (
                    <span>±{Math.round(subject.accuracyMeters)}m</span>
                  )}
                </div>
              </div>

              {subject.riskScore != null && subject.riskScore > 0 && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.1rem' }}>
                  <div style={{ flex: 1, height: 4, background: 'rgba(0, 0, 0, 0.06)', borderRadius: 2, overflow: 'hidden' }}>
                    <div
                      style={{
                        width: `${Math.min(subject.riskScore, 100)}%`,
                        height: '100%',
                        backgroundColor: subject.riskScore > 60 ? '#E11D48' : subject.riskScore > 30 ? '#D97706' : '#059669'
                      }}
                    />
                  </div>
                  <span style={{ fontSize: '0.68rem', color: 'var(--text-dim)', fontWeight: 600 }}>Risk: {subject.riskScore}</span>
                </div>
              )}
            </button>
          );
        })}

        {filteredSubjects.length === 0 && (
          <div style={{ padding: '2rem 1rem', textAlign: 'center', color: 'var(--text-dim)', fontSize: '0.82rem' }}>
            No subjects match the selected search or filter criteria.
          </div>
        )}
      </div>
    </div>
  );
}
