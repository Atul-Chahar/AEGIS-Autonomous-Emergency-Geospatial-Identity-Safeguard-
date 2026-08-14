import React, { useState } from 'react';
import {
  Activity,
  AlertTriangle,
  Compass,
  FileCheck2,
  Layers,
  Users
} from 'lucide-react';

export default function Sidebar({
  activeNav = 'map',
  onSelectNav,
  activeSosCount = 0,
  connectionStatus = 'LIVE'
}) {
  const [isHovered, setIsHovered] = useState(false);

  const navItems = [
    { id: 'map', label: 'Geospatial Radar', icon: Compass },
    { id: 'sos', label: 'Critical SOS Alerts', icon: AlertTriangle, count: activeSosCount },
    { id: 'trips', label: 'Active Tourists & Trips', icon: Users },
    { id: 'hazards', label: 'Hazard Geofences', icon: Layers },
    { id: 'responders', label: 'Rescue Units & SDRF', icon: Activity },
    { id: 'verify', label: 'Voucher Verifier', icon: FileCheck2 }
  ];

  return (
    <aside
      className={`dashboard-sidebar ${!isHovered ? 'collapsed' : 'expanded'}`}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div>
        <div className="sidebar-header-section" style={{ justifyContent: isHovered ? 'flex-start' : 'center' }}>
          {isHovered ? (
            <span className="sidebar-title-text">AEGIS Safeguard</span>
          ) : (
            <span style={{ fontSize: '0.85rem', fontWeight: 800, color: 'var(--primary-cyan)' }}>A</span>
          )}
        </div>

        <nav className="sidebar-nav-menu">
          {navItems.map(item => {
            const Icon = item.icon;
            const isActive = activeNav === item.id;
            return (
              <button
                key={item.id}
                type="button"
                className={`sidebar-nav-item ${isActive ? 'active' : ''}`}
                onClick={() => onSelectNav && onSelectNav(item.id)}
                title={!isHovered ? item.label : undefined}
              >
                <Icon size={18} style={{ flexShrink: 0 }} />
                {isHovered && <span>{item.label}</span>}
                {isHovered && item.count > 0 && (
                  <span className="sidebar-nav-counter">{item.count}</span>
                )}
              </button>
            );
          })}
        </nav>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
        <div className="sidebar-footer-card" style={{ justifyContent: !isHovered ? 'center' : 'flex-start' }}>
          <div
            style={{
              width: 8,
              height: 8,
              borderRadius: '50%',
              backgroundColor: connectionStatus === 'LIVE' ? '#059669' : '#E11D48',
              boxShadow: connectionStatus === 'LIVE' ? '0 0 8px #059669' : '0 0 8px #E11D48',
              flexShrink: 0
            }}
            className={connectionStatus === 'LIVE' ? 'live-dot' : ''}
          />
          {isHovered && (
            <div style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
              <span style={{ fontSize: '0.74rem', fontWeight: 700, color: 'var(--text-bright)' }}>
                Gateway {connectionStatus}
              </span>
              <span style={{ fontSize: '0.66rem', color: 'var(--text-dim)' }}>
                WebSocket Sepolia Live
              </span>
            </div>
          )}
        </div>
      </div>
    </aside>
  );
}
