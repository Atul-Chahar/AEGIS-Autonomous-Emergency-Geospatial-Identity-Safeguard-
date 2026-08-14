import React from 'react';
import {
  MapPin,
  RefreshCw,
  Search
} from 'lucide-react';

export default function TopBar({
  searchQuery,
  onSearchChange,
  onRefresh,
  loading
}) {
  return (
    <header className="dashboard-topbar">
      <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem', flex: 1 }}>
        <div className="topbar-search-container" style={{ width: '380px' }}>
          <Search size={16} color="var(--text-muted)" />
          <input
            type="text"
            className="topbar-search-input"
            placeholder="Search tourist ID, trip, zone, hazard..."
            value={searchQuery}
            onChange={e => onSearchChange(e.target.value)}
          />
          <span
            style={{
              fontSize: '0.68rem',
              background: 'rgba(0, 0, 0, 0.05)',
              padding: '0.15rem 0.4rem',
              borderRadius: 4,
              color: 'var(--text-dim)',
              fontWeight: 600
            }}
          >
            ⌘K
          </span>
        </div>
      </div>

      <div className="topbar-actions-group">
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.45rem',
            background: 'rgba(255, 255, 255, 0.8)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 12,
            padding: '0.45rem 0.85rem',
            fontSize: '0.8rem',
            color: 'var(--text-bright)',
            fontWeight: 700,
            boxShadow: 'var(--glass-shadow-sm)'
          }}
        >
          <MapPin size={15} color="var(--primary-cyan)" />
          <span>Cherrapunji (Sohra) Sector • 25.275°N 91.730°E</span>
        </div>

        <button
          type="button"
          className="btn-secondary"
          style={{ padding: '0.5rem 0.95rem', fontSize: '0.8rem' }}
          onClick={onRefresh}
          disabled={loading}
        >
          <RefreshCw size={14} className={loading ? 'live-dot' : ''} />
          <span>{loading ? 'Syncing...' : 'Sync Gateway'}</span>
        </button>
      </div>
    </header>
  );
}
