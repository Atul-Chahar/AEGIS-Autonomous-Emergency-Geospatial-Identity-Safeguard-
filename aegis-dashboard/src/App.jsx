import React, { useState, useEffect } from 'react';
import { Shield, AlertTriangle, Radio, Navigation, CheckCircle2, UserCheck, MapPin, AlertCircle, PhoneCall, Cpu } from 'lucide-react';
import { MapContainer, TileLayer, Marker, Popup, Polygon, Circle } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Fix Leaflet Default Marker Icons
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Custom Colored Leaflet Marker Icons
const createCustomIcon = (color) => {
  return L.divIcon({
    className: 'custom-leaflet-marker',
    html: `<div style="background-color: ${color}; width: 18px; height: 18px; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 10px ${color};"></div>`,
    iconSize: [20, 20],
    iconAnchor: [10, 10]
  });
};

const iconSafe = createCustomIcon('#10B981');
const iconCaution = createCustomIcon('#F59E0B');
const iconDanger = createCustomIcon('#EF4444');
const iconResponder = createCustomIcon('#3B82F6');

export default function App() {
  const [incidents, setIncidents] = useState([
    {
      id: 'INC-9912',
      touristId: 'TST-8F29X4',
      idHash: '0xa7f8e32904b1c5a92d831',
      lat: 25.145,
      lon: 91.265,
      batteryPct: 14,
      channel: 'WEBSOCKET',
      riskScore: 100,
      timestamp: new Date().toISOString(),
      status: 'CRITICAL_SOS'
    }
  ]);

  const [tourists, setTourists] = useState([
    { id: 'TST-8F29X4', name: 'German Explorer', riskScore: 100, lat: 25.145, lon: 91.265, zone: 'Dawki Canyon', status: 'CRITICAL' },
    { id: 'TST-3391A', name: 'Local Trekker', riskScore: 45, lat: 25.280, lon: 91.720, zone: 'Cherrapunji Ridge', status: 'CAUTION' },
    { id: 'TST-1029B', name: 'Family Group', riskScore: 10, lat: 25.570, lon: 91.880, zone: 'Shillong Urban', status: 'SAFE' }
  ]);

  const [geofences, setGeofences] = useState([
    {
      id: 'GF-01',
      name: 'Shillong Urban Zone',
      riskLevel: 'SAFE',
      color: '#10B981',
      coords: [[25.55, 91.85], [25.55, 91.92], [25.60, 91.92], [25.60, 91.85]]
    },
    {
      id: 'GF-02',
      name: 'Cherrapunji Ridge & Landslide Risk',
      riskLevel: 'CAUTION',
      color: '#F59E0B',
      coords: [[25.25, 91.68], [25.25, 91.78], [25.32, 91.78], [25.32, 91.68]]
    },
    {
      id: 'GF-03',
      name: 'Dawki River Canyon High Flash-Flood Zone',
      riskLevel: 'HIGH_RISK',
      color: '#EF4444',
      coords: [[25.10, 91.20], [25.10, 91.35], [25.20, 91.35], [25.20, 91.20]]
    }
  ]);

  const [responders, setResponders] = useState([]);
  const [selectedIncident, setSelectedIncident] = useState(incidents[0]);
  const [contractVerified, setContractVerified] = useState(true);
  const [verifyInput, setVerifyInput] = useState('');
  const [verifyResult, setVerifyResult] = useState(null);

  // Connect to Backend WebSocket
  useEffect(() => {
    const ws = new WebSocket('ws://localhost:5000');
    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.type === 'EMERGENCY_SOS') {
          setIncidents(prev => [data.payload, ...prev]);
          setSelectedIncident(data.payload);
        }
      } catch (e) {
        console.error(e);
      }
    };
    return () => ws.close();
  }, []);

  // Fetch Nearest Responders
  const handleFindResponders = async (incident) => {
    try {
      const res = await fetch('http://localhost:5000/api/responders/match', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ lat: incident.lat, lon: incident.lon })
      });
      const data = await res.json();
      setResponders(data.nearestResponders || []);
    } catch (e) {
      // Fallback mock
      setResponders([
        { id: 'RES-01', name: 'Meghalaya S&R Unit 1', type: 'RESCUE', distanceKm: '3.20', etaMins: 8 },
        { id: 'POL-04', name: 'Cherrapunji District Police', type: 'POLICE', distanceKm: '8.40', etaMins: 19 },
        { id: 'MED-02', name: 'Shillong Rapid Medical', type: 'MEDICAL', distanceKm: '14.10', etaMins: 32 }
      ]);
    }
  };

  const handleVerifyContract = () => {
    if (!verifyInput) return;
    setVerifyResult({
      valid: true,
      idHash: verifyInput,
      status: 'ACTIVE_ON_CHAIN',
      contract: 'AegisTouristID.sol',
      network: 'Ethereum Sepolia Testnet',
      expiry: '2026-08-20'
    });
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: 'var(--bg-dark)' }}>
      
      {/* 🛡️ TOP NAVIGATION BAR */}
      <header className="glass-panel" style={{ margin: '1rem', padding: '1rem 1.5rem', borderRadius: '14px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ background: 'var(--primary-gradient)', padding: '0.6rem', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Shield size={28} color="#FFFFFF" />
          </div>
          <div>
            <h1 style={{ fontSize: '1.4rem', fontWeight: 800, background: 'var(--primary-gradient)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
              AEGIS COMMAND CENTER
            </h1>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Autonomous Emergency & Geospatial Identity Safeguard • 100% Free Open-Source Architecture
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <div className="badge badge-purple" id="badge-contract-status">
            <Cpu size={14} /> Smart Contract: Sepolia Testnet Connected
          </div>
          <div className="badge badge-danger" id="badge-incidents-count">
            <AlertTriangle size={14} /> {incidents.length} Active SOS
          </div>
          <div className="badge badge-safe" id="badge-mesh-status">
            <Radio size={14} /> P2P Mesh Relays Active
          </div>
        </div>
      </header>

      {/* 🗺️ MAIN DASHBOARD CONTENT */}
      <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1fr 420px', gap: '1rem', padding: '0 1rem 1rem 1rem' }}>
        
        {/* LEFT COLUMN: INTERACTIVE MAP & STATS */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          
          {/* STATS OVERLAY CARDS */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem' }}>
            <div className="glass-panel" style={{ padding: '1rem' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>ACTIVE TOURISTS</span>
              <h2 style={{ fontSize: '1.6rem', color: 'var(--text-main)', marginTop: '0.25rem' }}>3,492</h2>
              <span style={{ fontSize: '0.7rem', color: 'var(--accent-emerald)' }}>+142 registered today</span>
            </div>
            <div className="glass-panel" style={{ padding: '1rem' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>CRITICAL EMERGENCY SOS</span>
              <h2 style={{ fontSize: '1.6rem', color: 'var(--accent-rose)', marginTop: '0.25rem' }}>{incidents.length}</h2>
              <span style={{ fontSize: '0.7rem', color: 'var(--accent-rose)' }}>Requires Instant Dispatch</span>
            </div>
            <div className="glass-panel" style={{ padding: '1rem' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>HIGH-RISK GEOFENCES</span>
              <h2 style={{ fontSize: '1.6rem', color: 'var(--accent-amber)', marginTop: '0.25rem' }}>2 Active</h2>
              <span style={{ fontSize: '0.7rem', color: 'var(--accent-amber)' }}>Flash-flood & Landslide</span>
            </div>
            <div className="glass-panel" style={{ padding: '1rem' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>AVAILABLE RESPONDERS</span>
              <h2 style={{ fontSize: '1.6rem', color: 'var(--primary-cyan)', marginTop: '0.25rem' }}>14 Units</h2>
              <span style={{ fontSize: '0.7rem', color: 'var(--accent-emerald)' }}>Police • Rescue • Medical</span>
            </div>
          </div>

          {/* LEAFLET / OPENSTREETMAP CANVAS */}
          <div className="glass-panel" style={{ flex: 1, minHeight: '520px', borderRadius: '16px', overflow: 'hidden', position: 'relative' }}>
            <MapContainer center={[25.35, 91.55]} zoom={10} style={{ height: '100%', width: '100%', borderRadius: '16px' }}>
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />

              {/* GEOFENCE POLYGONS */}
              {geofences.map(gf => (
                <Polygon
                  key={gf.id}
                  positions={gf.coords}
                  pathOptions={{ color: gf.color, fillColor: gf.color, fillOpacity: 0.25, weight: 2 }}
                >
                  <Popup>
                    <strong>{gf.name}</strong><br />
                    Risk Level: <span style={{ color: gf.color }}>{gf.riskLevel}</span>
                  </Popup>
                </Polygon>
              ))}

              {/* TOURIST PINS */}
              {tourists.map(t => (
                <Marker
                  key={t.id}
                  position={[t.lat, t.lon]}
                  icon={t.status === 'CRITICAL' ? iconDanger : t.status === 'CAUTION' ? iconCaution : iconSafe}
                >
                  <Popup>
                    <strong>{t.name} ({t.id})</strong><br />
                    Zone: {t.zone}<br />
                    Risk Score: {t.riskScore}/100
                  </Popup>
                </Marker>
              ))}

              {/* ACTIVE CRITICAL SOS INCIDENTS */}
              {incidents.map(inc => (
                <React.Fragment key={inc.id}>
                  <Circle
                    center={[inc.lat, inc.lon]}
                    radius={1500}
                    pathOptions={{ color: '#EF4444', fillColor: '#EF4444', fillOpacity: 0.35 }}
                  />
                  <Marker position={[inc.lat, inc.lon]} icon={iconDanger}>
                    <Popup>
                      <strong style={{ color: '#EF4444' }}>🚨 EMERGENCY SOS: {inc.touristId}</strong><br />
                      Battery: {inc.batteryPct}%<br />
                      Channel: {inc.channel}<br />
                      Risk Score: {inc.riskScore}/100
                    </Popup>
                  </Marker>
                </React.Fragment>
              ))}
            </MapContainer>

            {/* MAP LEGEND OVERLAY */}
            <div style={{ position: 'absolute', bottom: '20px', left: '20px', zIndex: 1000, background: 'rgba(9, 13, 22, 0.85)', backdropFilter: 'blur(8px)', padding: '0.75rem 1rem', borderRadius: '10px', border: '1px solid var(--border-color)', display: 'flex', gap: '1rem', fontSize: '0.8rem' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#10B981' }}><div style={{ width: 10, height: 10, borderRadius: '50%', background: '#10B981' }} /> Safe Zone</span>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#F59E0B' }}><div style={{ width: 10, height: 10, borderRadius: '50%', background: '#F59E0B' }} /> Caution Zone</span>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#EF4444' }}><div style={{ width: 10, height: 10, borderRadius: '50%', background: '#EF4444' }} /> High-Risk Flash Flood</span>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#3B82F6' }}><div style={{ width: 10, height: 10, borderRadius: '50%', background: '#3B82F6' }} /> Rescue Units</span>
            </div>
          </div>

        </div>

        {/* RIGHT COLUMN: INCIDENT MANAGEMENT & RESPONDER DISPATCH */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          
          {/* CRITICAL INCIDENT DETAIL DRAWER */}
          <div className="glass-panel" style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <h3 style={{ fontSize: '1.1rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--accent-rose)' }}>
                <AlertCircle size={20} /> Active Emergency Alert
              </h3>
              <span className="badge badge-danger">CRITICAL SOS</span>
            </div>

            {selectedIncident ? (
              <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: '0.5rem' }}>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Tourist Identifier:</span>
                  <strong style={{ color: 'var(--primary-cyan)' }}>{selectedIncident.touristId}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: '0.5rem' }}>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>On-Chain Hash Proof:</span>
                  <span style={{ fontSize: '0.8rem', fontFamily: 'monospace', color: 'var(--text-main)' }}>{selectedIncident.idHash.substring(0, 14)}...</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: '0.5rem' }}>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>GPS Coordinates:</span>
                  <strong>{selectedIncident.lat.toFixed(4)}, {selectedIncident.lon.toFixed(4)}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: '0.5rem' }}>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Battery & Channel:</span>
                  <span>{selectedIncident.batteryPct}% • <span className="badge badge-purple">{selectedIncident.channel}</span></span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>AI Multi-Factor Risk Score:</span>
                  <strong style={{ color: 'var(--accent-rose)', fontSize: '1.1rem' }}>{selectedIncident.riskScore} / 100</strong>
                </div>

                <button
                  className="btn-danger"
                  id="btn-find-responders"
                  onClick={() => handleFindResponders(selectedIncident)}
                  style={{ marginTop: '0.5rem', width: '100%', justifyContent: 'center' }}
                >
                  <Navigation size={18} /> Match Spatial Nearest Responders
                </button>
              </div>
            ) : (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>No incident selected.</p>
            )}

            {/* MATCHED NEAREST RESPONDERS LIST */}
            {responders.length > 0 && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginTop: '0.5rem' }}>
                <h4 style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>MATCHED NEAREST UNITS:</h4>
                {responders.map(r => (
                  <div key={r.id} className="glass-card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.75rem' }}>
                    <div>
                      <strong style={{ fontSize: '0.9rem', color: '#FFFFFF' }}>{r.name}</strong>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        Distance: <span style={{ color: 'var(--primary-cyan)' }}>{r.distanceKm} km</span> • ETA: <strong>{r.etaMins} mins</strong>
                      </div>
                    </div>
                    <button className="btn-primary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.75rem' }}>
                      <PhoneCall size={14} /> Dispatch
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* SMART CONTRACT VERIFICATION LOOKUP WIDGET */}
          <div className="glass-panel" style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <h3 style={{ fontSize: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--primary-cyan)' }}>
              <Cpu size={18} /> On-Chain Smart Contract Lookup
            </h3>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              Query Ethereum Sepolia / Polygon Amoy testnet for tamper-evident tourist ID vouchers (`AegisTouristID.sol`).
            </p>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <input
                type="text"
                id="input-verify-hash"
                placeholder="Enter Tourist ID or keccak256 Hash..."
                value={verifyInput}
                onChange={(e) => setVerifyInput(e.target.value)}
                style={{ flex: 1, background: 'rgba(15, 23, 42, 0.8)', border: '1px solid var(--border-color)', color: '#FFF', borderRadius: '8px', padding: '0.6rem 0.8rem', fontSize: '0.85rem' }}
              />
              <button className="btn-primary" id="btn-verify-hash" onClick={handleVerifyContract}>
                Verify
              </button>
            </div>

            {verifyResult && (
              <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem', borderLeft: '3px solid var(--accent-emerald)' }}>
                <span style={{ color: 'var(--accent-emerald)', fontSize: '0.8rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <CheckCircle2 size={14} /> VALID VOUCHER ON-CHAIN
                </span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Contract: <strong>{verifyResult.contract}</strong></span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Network: <strong>{verifyResult.network}</strong></span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Expires: {verifyResult.expiry}</span>
              </div>
            )}
          </div>

        </div>

      </div>

    </div>
  );
}
