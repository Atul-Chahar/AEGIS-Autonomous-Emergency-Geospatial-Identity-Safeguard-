import React, { useEffect, useState } from 'react';
import { Circle, MapContainer, Marker, Polygon, Polyline, Popup, TileLayer, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import { Activity, AlertOctagon, AlertTriangle, Ban, Flame, Layers, Navigation, Shield, ShieldAlert, Target } from 'lucide-react';

// Operational area spans Cherrapunji (91.60-91.82) AND Dawki (91.297) —
// SOS incidents/relays can originate anywhere in the sector, so the map
// must not be hard-locked to Cherrapunji or those markers are invisible.
const CHERRAPUNJI_CENTER = [25.270, 91.715];
const CHERRAPUNJI_BOUNDS = [
  [25.05, 91.15], // Southwest bound (covers Dawki 91.297)
  [25.45, 91.95]  // Northeast bound
];

const iconByStatus = {
  ACTIVE: createCustomIcon('#059669', false),
  CAUTION: createCustomIcon('#D97706', false),
  LIVE: createCustomIcon('#059669', false),
  RECENT: createCustomIcon('#D97706', false),
  STALE: createCustomIcon('#64748B', false),
  EMERGENCY_STALE: createCustomIcon('#E11D48', true),
  SOS: createCustomIcon('#E11D48', true),
  SEARCHING: createCustomIcon('#7C3AED', true),
  RESOLVED: createCustomIcon('#0284C7', false)
};

const iconResponder = L.divIcon({
  className: 'custom-responder-marker',
  html: `<div style="background: linear-gradient(135deg, #0284C7, #4F46E5); width:24px; height:24px; border-radius:50%; border:2px solid #FFFFFF; display:flex; align-items:center; justify-content:center; box-shadow:0 2px 10px rgba(2,132,199,0.5); color:white; font-size:11px; font-weight:800;">R</div>`,
  iconSize: [26, 26],
  iconAnchor: [13, 13]
});

const iconHazard = L.divIcon({
  className: 'custom-hazard-marker',
  html: `<div style="background: linear-gradient(135deg, #D97706, #E11D48); width:22px; height:22px; border-radius:6px; border:2px solid #FFFFFF; display:flex; align-items:center; justify-content:center; box-shadow:0 2px 8px rgba(217,119,6,0.5); color:white; font-size:11px; font-weight:800;">!</div>`,
  iconSize: [24, 24],
  iconAnchor: [12, 12]
});

function createCustomIcon(color, isPulsing) {
  return L.divIcon({
    className: 'custom-leaflet-marker',
    html: `
      <div style="position:relative; width:24px; height:24px; display:flex; align-items:center; justify-content:center;">
        ${isPulsing ? `<div style="position:absolute; width:38px; height:38px; border-radius:50%; background:${color}; opacity:0.4; animation:sosBeacon 1.6s infinite ease-in-out;"></div>` : ''}
        <div style="background-color:${color}; width:17px; height:17px; border-radius:50%; border:2.5px solid #FFFFFF; box-shadow:0 2px 10px ${color}; z-index:2;"></div>
      </div>
    `,
    iconSize: [30, 30],
    iconAnchor: [15, 15]
  });
}

// Auto-zoomer & recenter on tracked active tourist
function MapController({ selectedSubject, subjects }) {
  const map = useMap();

  useEffect(() => {
    if (selectedSubject?.lat && selectedSubject?.lon) {
      map.flyTo([selectedSubject.lat, selectedSubject.lon], 15.5, {
        animate: true,
        duration: 1.2
      });
      return;
    }

    // No subject selected yet — fit all live subjects (incl. SOS) in view.
    const points = (subjects || [])
      .filter(s => Number.isFinite(s.lat) && Number.isFinite(s.lon))
      .map(s => [s.lat, s.lon]);
    if (points.length > 0) {
      map.fitBounds(points, { padding: [40, 40], maxZoom: 13 });
    }
  }, [selectedSubject?.lat, selectedSubject?.lon, selectedSubject?.subjectId, subjects, map]);

  return null;
}

export default function GeospatialMap({
  subjects = [],
  selectedSubject = null,
  onSelectSubject,
  geofences = [],
  hazards = [],
  responders = [],
  searchProbability = null,
  trajectoryPoints = []
}) {
  const [layers, setLayers] = useState({
    geofences: true,
    hazards: true,
    responders: true,
    searchSectors: false,
    hotMap: true,
    trails: true
  });

  const toggleLayer = key => {
    setLayers(prev => ({ ...prev, [key]: !prev[key] }));
  };

  // Extract probability cells for hot map
  const heatmapFeatures = searchProbability?.geoJsonHeatmap?.features || [];

  return (
    <div className="map-canvas-wrapper">
      {/* Floating Top Layer Filter Bar - Cleaned */}
      <div className="map-floating-top-hud">
        <div className="map-layer-pill-group">
          <button
            type="button"
            className={`map-layer-btn ${layers.hotMap ? 'active' : ''}`}
            onClick={() => toggleLayer('hotMap')}
          >
            <Flame size={13} />
            <span>Risk Hot Map</span>
          </button>
          <button
            type="button"
            className={`map-layer-btn ${layers.hazards ? 'active' : ''}`}
            onClick={() => toggleLayer('hazards')}
          >
            <AlertTriangle size={13} />
            <span>Hazards ({hazards.length})</span>
          </button>
          <button
            type="button"
            className={`map-layer-btn ${layers.responders ? 'active' : ''}`}
            onClick={() => toggleLayer('responders')}
          >
            <Activity size={13} />
            <span>Responders ({responders.length})</span>
          </button>
        </div>

        {selectedSubject && (
          <div
            style={{
              pointerEvents: 'auto',
              background: 'rgba(255, 255, 255, 0.92)',
              backdropFilter: 'blur(14px)',
              border: '1px solid var(--border-glow-cyan)',
              borderRadius: 12,
              padding: '0.45rem 0.95rem',
              display: 'flex',
              alignItems: 'center',
              gap: '0.65rem',
              fontSize: '0.82rem',
              color: 'var(--text-bright)',
              boxShadow: '0 4px 16px rgba(0, 0, 0, 0.08)'
            }}
          >
            <Navigation size={15} color="var(--primary-cyan)" />
            <span>Tracking: <strong>{selectedSubject.touristId}</strong></span>
            <span style={{ color: 'var(--text-dim)' }}>•</span>
            <span className="font-mono" style={{ fontSize: '0.76rem', color: 'var(--primary-cyan)', fontWeight: 700 }}>
              {selectedSubject.lat?.toFixed(4)}°N, {selectedSubject.lon?.toFixed(4)}°E
            </span>
          </div>
        )}
      </div>

      {/* Leaflet Map Canvas - Locked to Cherrapunji Sector */}
      <MapContainer
        center={selectedSubject?.lat ? [selectedSubject.lat, selectedSubject.lon] : CHERRAPUNJI_CENTER}
        zoom={13}
        minZoom={12}
        maxZoom={18}
        maxBounds={CHERRAPUNJI_BOUNDS}
        maxBoundsViscosity={1.0}
        style={{ width: '100%', height: '100%' }}
      >
        <MapController selectedSubject={selectedSubject} subjects={subjects} />

        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {/* Geofence Polygons: Green (Safe), Yellow (Caution), Red (High Risk), Black (Restricted) */}
        {layers.geofences && geofences.map(gf => {
          const rawCoords = gf.coordinates_json || gf.coordinates || gf.coords;
          if (!rawCoords || !Array.isArray(rawCoords)) return null;

          // Normalize [lon, lat] vs [lat, lon] for Leaflet
          const coords = rawCoords.map(p => {
            if (Array.isArray(p) && p.length >= 2) {
              const a = Number(p[0]);
              const b = Number(p[1]);
              if (a > 50 && b < 40) return [b, a];
              return [a, b];
            }
            return p;
          });

          const isBlackZone = gf.riskLevel === 'RESTRICTED' || gf.riskLevel === 'BLACK_ZONE';
          const isRedZone = gf.riskLevel === 'HIGH_RISK';
          const isYellowZone = gf.riskLevel === 'CAUTION';

          let color = gf.color;
          if (!color) {
            if (isBlackZone) color = '#090D16';
            else if (isRedZone) color = '#E11D48';
            else if (isYellowZone) color = '#D97706';
            else color = '#059669';
          }

          let fillColor = color;
          let fillOpacity = 0.16;
          let dashArray = undefined;

          if (isBlackZone) {
            fillColor = '#000000';
            fillOpacity = 0.45;
            color = '#18181B';
            dashArray = '6, 6';
          } else if (isRedZone) {
            fillOpacity = 0.22;
          } else if (isYellowZone) {
            fillOpacity = 0.18;
          }

          return (
            <Polygon
              key={gf.id}
              positions={coords}
              pathOptions={{
                color,
                fillColor,
                fillOpacity,
                weight: isBlackZone ? 3 : 2,
                dashArray
              }}
            >
              <Popup>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', minWidth: '220px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '0.5rem' }}>
                    <strong style={{ color: '#0F172A', fontSize: '0.88rem' }}>{gf.name}</strong>
                  </div>
                  <div style={{ marginTop: '0.2rem' }}>
                    {isBlackZone && (
                      <span className="badge" style={{ background: '#18181B', color: '#FFFFFF', border: '1px solid #000000' }}>
                        <Ban size={12} /> Black Zone • Restricted
                      </span>
                    )}
                    {isRedZone && (
                      <span className="badge badge-danger">
                        <AlertOctagon size={12} /> Red Zone • High Risk
                      </span>
                    )}
                    {isYellowZone && (
                      <span className="badge badge-caution">
                        <AlertTriangle size={12} /> Yellow Zone • Caution
                      </span>
                    )}
                    {!isBlackZone && !isRedZone && !isYellowZone && (
                      <span className="badge badge-safe">
                        <Shield size={12} /> Safe Eco-Corridor
                      </span>
                    )}
                  </div>
                  <p style={{ fontSize: '0.75rem', color: '#64748B', marginTop: '0.35rem' }}>
                    {isBlackZone
                      ? 'Prohibited Zero-Entry Perimeter. Cliff instability and bio-reserve protection.'
                      : isRedZone
                        ? 'Critical Red Zone. Deep gorge flash-flood risk and steep vertical descents.'
                        : isYellowZone
                          ? 'Caution Escarpment. Dense fog and slippery rock trails.'
                          : 'Safe Monitored Corridor with guide posts and emergency beacons.'}
                  </p>
                </div>
              </Popup>
            </Polygon>
          );
        })}

        {/* Risk Prediction Hot Map / Probability Density Heatmap Grid */}
        {layers.hotMap && heatmapFeatures.map((feat, idx) => {
          const prob = feat.properties?.probabilityPercent || 0;
          if (prob < 0.3) return null;

          let heatColor = '#0284C7';
          let heatOpacity = 0.15;
          if (prob > 5.0) {
            heatColor = '#E11D48'; // Critical Hotspot (Red)
            heatOpacity = 0.45;
          } else if (prob > 2.0) {
            heatColor = '#D97706'; // Medium Hotspot (Yellow)
            heatOpacity = 0.35;
          } else if (prob > 1.0) {
            heatColor = '#059669'; // Moderate (Green)
            heatOpacity = 0.25;
          }

          const rawRing = feat.geometry?.coordinates?.[0] || [];
          const positions = rawRing.map(([lon, lat]) => [lat, lon]);

          return (
            <Polygon
              key={`heat-${idx}`}
              positions={positions}
              pathOptions={{
                color: heatColor,
                fillColor: heatColor,
                fillOpacity: heatOpacity,
                weight: 0.5,
                stroke: false
              }}
            >
              <Popup>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.2rem' }}>
                  <strong style={{ color: '#0F172A' }}>Bayesian Probability Hotspot</strong>
                  <span style={{ fontSize: '0.75rem', color: heatColor, fontWeight: 700 }}>
                    {prob}% density probability
                  </span>
                  <span style={{ fontSize: '0.72rem', color: '#64748B' }}>
                    {feat.properties?.distKm} km from last known safety fix
                  </span>
                </div>
              </Popup>
            </Polygon>
          );
        })}

        {/* Hazard Points */}
        {layers.hazards && hazards.map(hazard => {
          const lat = Number(hazard.lat);
          const lon = Number(hazard.lon);
          if (!Number.isFinite(lat) || !Number.isFinite(lon)) return null;
          return (
            <Marker key={hazard.id} position={[lat, lon]} icon={iconHazard}>
              <Popup>
                <strong>{hazard.hazardType || hazard.type || 'Hazard Zone'}</strong><br />
                {hazard.description || hazard.status || 'Reported hazard warning'}
              </Popup>
            </Marker>
          );
        })}

        {/* Search Probability Top Sectors */}
        {layers.searchSectors && searchProbability?.topSearchSectors?.map(sector => (
          <Polygon
            key={sector.sectorId}
            positions={sector.bounds}
            pathOptions={{ color: '#7C3AED', fillColor: '#7C3AED', fillOpacity: 0.2, weight: 2, dashArray: '6, 6' }}
          >
            <Popup>
              <strong>{sector.name} ({sector.probabilityPercent}%)</strong><br />
              {sector.explanation}
            </Popup>
          </Polygon>
        ))}

        {/* Selected Subject Breadcrumb Trail */}
        {layers.trails && trajectoryPoints.length > 1 && (
          <Polyline
            positions={trajectoryPoints}
            pathOptions={{ color: '#0284C7', weight: 4, opacity: 0.9, dashArray: '6, 6' }}
          />
        )}

        {/* Responder Units */}
        {layers.responders && responders.map(responder => {
          const lat = Number(responder.lat);
          const lon = Number(responder.lon);
          if (!Number.isFinite(lat) || !Number.isFinite(lon)) return null;
          return (
            <Marker key={responder.id} position={[lat, lon]} icon={iconResponder}>
              <Popup>
                <strong>{responder.name}</strong><br />
                Status: {responder.status || 'AVAILABLE'}<br />
                Type: {responder.vehicle || responder.type || 'SDRF Rapid Unit'}
              </Popup>
            </Marker>
          );
        })}

        {/* Tracked Subjects / Tourists / SOS Incidents */}
        {subjects.map(subject => {
          if (!Number.isFinite(subject.lat) || !Number.isFinite(subject.lon)) return null;
          const isSelected = selectedSubject?.subjectId === subject.subjectId;
          const markerIcon = iconByStatus[subject.staleStatus] || iconByStatus[subject.status] || iconByStatus.ACTIVE;

          return (
            <React.Fragment key={subject.subjectId}>
              {(subject.status === 'SOS' || subject.staleStatus === 'EMERGENCY_STALE') && (
                <Circle
                  center={[subject.lat, subject.lon]}
                  radius={800}
                  pathOptions={{ color: '#E11D48', fillColor: '#E11D48', fillOpacity: 0.18, weight: 2 }}
                />
              )}
              {isSelected && (
                <Circle
                  center={[subject.lat, subject.lon]}
                  radius={Math.max(subject.accuracyMeters || 60, 60)}
                  pathOptions={{ color: '#0284C7', fillColor: '#0284C7', fillOpacity: 0.14, weight: 1.5 }}
                />
              )}
              <Marker
                position={[subject.lat, subject.lon]}
                icon={markerIcon}
                eventHandlers={{ click: () => onSelectSubject(subject.subjectId) }}
              >
                <Popup>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '0.5rem' }}>
                      <strong style={{ color: '#0F172A' }}>{subject.touristId}</strong>
                      <span className={subject.status === 'SOS' ? 'badge badge-danger' : 'badge badge-safe'}>
                        {subject.status}
                      </span>
                    </div>
                    <span style={{ fontSize: '0.74rem', color: '#64748B' }}>
                      Trip: {subject.tripId || 'Not linked'} • {subject.staleStatus}
                    </span>
                    <span style={{ fontSize: '0.72rem', color: '#94A3B8' }}>
                      Accuracy: {Math.round(subject.accuracyMeters || 0)}m • Battery: {subject.batteryPercent ?? '--'}%
                    </span>
                  </div>
                </Popup>
              </Marker>
            </React.Fragment>
          );
        })}
      </MapContainer>

      {/* Floating Bottom Legend HUD */}
      <div className="map-floating-bottom-hud">
        <div className="map-legend-box">
          <span className="legend-chip">
            <span style={{ width: 10, height: 10, borderRadius: 2, backgroundColor: '#059669', display: 'inline-block' }} />
            <span>Safe Corridor</span>
          </span>
          <span className="legend-chip">
            <span style={{ width: 10, height: 10, borderRadius: 2, backgroundColor: '#D97706', display: 'inline-block' }} />
            <span>Yellow Zone (Caution)</span>
          </span>
          <span className="legend-chip">
            <span style={{ width: 10, height: 10, borderRadius: 2, backgroundColor: '#E11D48', display: 'inline-block' }} />
            <span>Red Zone (High Risk)</span>
          </span>
          <span className="legend-chip">
            <span style={{ width: 10, height: 10, borderRadius: 2, backgroundColor: '#18181B', border: '1px solid #000', display: 'inline-block' }} />
            <span>Black Zone (Restricted)</span>
          </span>
          <span className="legend-chip">
            <span style={{ width: 10, height: 10, borderRadius: 2, background: 'linear-gradient(135deg, #F59E0B, #E11D48)', display: 'inline-block' }} />
            <span>Risk Hot Map</span>
          </span>
          <span className="legend-chip">
            <span style={{ width: 9, height: 9, borderRadius: '50%', backgroundColor: '#E11D48', display: 'inline-block' }} />
            <span>Emergency SOS</span>
          </span>
        </div>
      </div>
    </div>
  );
}
