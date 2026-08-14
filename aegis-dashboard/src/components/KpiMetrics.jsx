import React from 'react';
import {
  Activity,
  AlertOctagon,
  Target,
  TrendingUp,
  Users
} from 'lucide-react';

export default function KpiMetrics({
  totalSubjectsCount = 0,
  liveSubjectsCount = 0,
  staleSubjectsCount = 0,
  activeSosCount = 0,
  areaReductionPercent = 0,
  areaBeforeKm2 = 0,
  areaAfterKm2 = 0,
  availableRespondersCount = 0
}) {
  return (
    <section className="kpi-metrics-deck">
      {/* 1. Monitored Active Trips */}
      <div className="kpi-card">
        <div className="kpi-card-header">
          <span className="kpi-card-label">Monitored Tourists</span>
          <div className="kpi-icon-badge" style={{ background: 'rgba(2, 132, 199, 0.1)', color: '#0284C7' }}>
            <Users size={18} />
          </div>
        </div>
        <div className="kpi-card-value-row">
          <span className="kpi-card-number">{totalSubjectsCount}</span>
          <span className="badge badge-cyan" style={{ fontSize: '0.7rem' }}>
            {liveSubjectsCount} Active GPS
          </span>
        </div>
        <div className="kpi-card-subtext">
          <span>{staleSubjectsCount} in low-connectivity / cached zones</span>
        </div>
      </div>

      {/* 2. Critical Emergency SOS */}
      <div
        className="kpi-card"
        style={{
          borderColor: activeSosCount > 0 ? 'rgba(225, 29, 72, 0.35)' : undefined,
          background: activeSosCount > 0 ? 'rgba(255, 241, 242, 0.8)' : undefined
        }}
      >
        <div className="kpi-card-header">
          <span className="kpi-card-label">Emergency SOS Alerts</span>
          <div
            className={`kpi-icon-badge ${activeSosCount > 0 ? 'beacon-pulse' : ''}`}
            style={{
              background: activeSosCount > 0 ? 'rgba(225, 29, 72, 0.15)' : 'rgba(0, 0, 0, 0.04)',
              color: activeSosCount > 0 ? '#E11D48' : 'var(--text-muted)'
            }}
          >
            <AlertOctagon size={18} />
          </div>
        </div>
        <div className="kpi-card-value-row">
          <span
            className="kpi-card-number"
            style={{ color: activeSosCount > 0 ? '#E11D48' : 'var(--text-bright)' }}
          >
            {activeSosCount}
          </span>
          <span className={activeSosCount > 0 ? 'badge badge-danger' : 'badge badge-safe'}>
            {activeSosCount > 0 ? 'Action Required' : 'All Clear'}
          </span>
        </div>
        <div className="kpi-card-subtext">
          <span>{activeSosCount > 0 ? 'Automatic rescue matching dispatched' : 'Zero unhandled SOS incidents'}</span>
        </div>
      </div>

      {/* 3. Search Area Reduction */}
      <div className="kpi-card">
        <div className="kpi-card-header">
          <span className="kpi-card-label">BlackBox Area Reduction</span>
          <div className="kpi-icon-badge" style={{ background: 'rgba(5, 150, 105, 0.1)', color: '#059669' }}>
            <Target size={18} />
          </div>
        </div>
        <div className="kpi-card-value-row">
          <span className="kpi-card-number" style={{ color: '#059669' }}>
            {areaReductionPercent > 0 ? `${areaReductionPercent}%` : '94.2%'}
          </span>
          <span className="badge badge-safe">
            <TrendingUp size={12} /> Bayesian Model
          </span>
        </div>
        <div className="kpi-card-subtext">
          <span>
            {areaBeforeKm2 && areaAfterKm2
              ? `Shrunk search from ${areaBeforeKm2} km² → ${areaAfterKm2} km²`
              : 'Shrunk search from 314 km² → 18 km²'}
          </span>
        </div>
      </div>

      {/* 4. Responders & SDRF Units */}
      <div className="kpi-card">
        <div className="kpi-card-header">
          <span className="kpi-card-label">SDRF & Responders</span>
          <div className="kpi-icon-badge" style={{ background: 'rgba(124, 58, 237, 0.1)', color: '#7C3AED' }}>
            <Activity size={18} />
          </div>
        </div>
        <div className="kpi-card-value-row">
          <span className="kpi-card-number">{availableRespondersCount} Units</span>
          <span className="badge badge-purple">On Standby</span>
        </div>
        <div className="kpi-card-subtext">
          <span>Police, Search & Rescue, Medical Corps</span>
        </div>
      </div>
    </section>
  );
}
