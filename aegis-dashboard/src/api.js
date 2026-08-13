const API_BASE_URL = 'http://localhost:5000/api';

export async function fetchHealth() {
  const res = await fetch(`${API_BASE_URL}/health`);
  return res.json();
}

export async function fetchIncidents() {
  const res = await fetch(`${API_BASE_URL}/incidents`);
  return res.json();
}

export async function updateIncidentStatus(id, status) {
  const res = await fetch(`${API_BASE_URL}/incidents/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status })
  });
  return res.json();
}

export async function fetchGeofences() {
  const res = await fetch(`${API_BASE_URL}/geofences`);
  return res.json();
}

export async function fetchHazards() {
  const res = await fetch(`${API_BASE_URL}/hazards`);
  return res.json();
}

export async function fetchResponders() {
  const res = await fetch(`${API_BASE_URL}/responders`);
  return res.json();
}

export async function matchResponders(lat, lon) {
  const res = await fetch(`${API_BASE_URL}/responders/match`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ lat, lon })
  });
  return res.json();
}

export async function fetchActiveTrips() {
  const res = await fetch(`${API_BASE_URL}/trips`);
  return res.json();
}

export async function fetchTrajectory(tripId) {
  const res = await fetch(`${API_BASE_URL}/breadcrumbs/${tripId}`);
  return res.json();
}

export async function verifyVoucher(idHash) {
  const res = await fetch(`${API_BASE_URL}/identity/verify/${encodeURIComponent(idHash)}`);
  return res.json();
}

export async function fetchSearchProbability(params = {}) {
  const res = await fetch(`${API_BASE_URL}/search-probability`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params)
  });
  return res.json();
}
