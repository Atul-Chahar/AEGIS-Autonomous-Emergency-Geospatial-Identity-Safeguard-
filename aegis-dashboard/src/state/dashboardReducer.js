const PII_KEYS = new Set([
  'aadhaar',
  'aadhaar_number',
  'aadhaarNumber',
  'contact',
  'passport',
  'passport_number',
  'passportNumber',
  'passportOrAadhaar',
  'passport_or_aadhaar',
  'phone',
  'phone_number',
  'phoneNumber',
  'emergencyContact',
  'emergency_contact',
  'emergencyContacts',
  'emergency_contacts',
  'identity',
  'identityDocument',
  'identity_document',
  'name',
  'fullName'
]);

const EMPTY_COLLECTIONS = {
  incidents: [],
  trips: [],
  geofences: [],
  hazards: [],
  responders: []
};

export function createInitialDashboardState(overrides = {}) {
  return {
    incidentsById: {},
    tripsById: {},
    breadcrumbsByTripId: {},
    geofencesById: {},
    hazardsById: {},
    respondersById: {},
    selectedSubjectId: null,
    connectionStatus: 'OFFLINE',
    loading: false,
    error: null,
    now: new Date().toISOString(),
    ...overrides
  };
}

export function hydrateDashboardState(input = {}) {
  const base = createInitialDashboardState({
    selectedSubjectId: input.selectedSubjectId || null,
    now: input.now || new Date().toISOString(),
    loading: false,
    error: input.error || null
  });

  const tripsById = indexById(input.trips || EMPTY_COLLECTIONS.trips);
  const incidentsById = indexById(input.incidents || EMPTY_COLLECTIONS.incidents);
  const breadcrumbsByTripId = normalizeBreadcrumbMap(input.breadcrumbsByTripId || {});

  const next = {
    ...base,
    tripsById,
    incidentsById,
    breadcrumbsByTripId,
    geofencesById: indexById(input.geofences || EMPTY_COLLECTIONS.geofences),
    hazardsById: indexById(input.hazards || EMPTY_COLLECTIONS.hazards),
    respondersById: indexById(input.responders || EMPTY_COLLECTIONS.responders)
  };

  if (!next.selectedSubjectId) {
    next.selectedSubjectId = selectDashboardSubjects(next)[0]?.subjectId || null;
  }

  return next;
}

export function dashboardReducer(state, action) {
  switch (action.type) {
    case 'HYDRATE_SUCCESS':
      return hydrateDashboardState({
        ...action.payload,
        selectedSubjectId: state.selectedSubjectId,
        now: action.receivedAt || action.payload?.now || state.now
      });
    case 'SELECT_SUBJECT':
      return { ...state, selectedSubjectId: action.subjectId };
    case 'WS_CONNECTED':
      return { ...state, connectionStatus: 'LIVE' };
    case 'WS_RECONNECTING':
      return { ...state, connectionStatus: 'RECONNECTING' };
    case 'WS_OFFLINE':
      return { ...state, connectionStatus: 'OFFLINE' };
    case 'EMERGENCY_SOS':
      return upsertIncident(state, action.payload, action.receivedAt);
    case 'INCIDENT_STATUS_CHANGED':
      return upsertIncident(state, action.payload, action.receivedAt);
    case 'TRIP_UPDATED':
    case 'TRIP_STARTED':
      return upsertTrip(state, action.payload, action.receivedAt);
    case 'BREADCRUMB_RECORDED':
      return appendBreadcrumb(state, action.payload, action.receivedAt);
    case 'HAZARD_EVALUATED':
      return upsertHazard(state, action.payload?.hazard || action.payload);
    case 'RESPONDER_UPDATED':
      return upsertResponder(state, action.payload);
    default:
      return state;
  }
}

export function selectDashboardSubjects(state) {
  const trips = Object.values(state.tripsById).map(trip => buildTripSubject(state, trip));
  const incidentSubjects = Object.values(state.incidentsById)
    .filter(incident => incident.status !== 'RESOLVED')
    .map(incident => buildIncidentSubject(state, incident));
  const incidentTripIds = new Set(
    incidentSubjects
      .filter(subject => subject.incidentStatus !== 'RESOLVED')
      .map(subject => subject.tripId)
      .filter(Boolean)
  );
  const tripSubjects = trips.filter(subject => !incidentTripIds.has(subject.tripId));

  return [...incidentSubjects, ...tripSubjects]
    .filter(Boolean)
    .sort((a, b) => compareTimestamps(b.lastSeenAt, a.lastSeenAt));
}

export function selectSelectedSubject(state) {
  const subjects = selectDashboardSubjects(state);
  return subjects.find(subject => subject.subjectId === state.selectedSubjectId) || subjects[0] || null;
}

export function selectCollections(state) {
  return {
    incidents: Object.values(state.incidentsById),
    trips: Object.values(state.tripsById),
    geofences: Object.values(state.geofencesById),
    hazards: Object.values(state.hazardsById),
    responders: Object.values(state.respondersById)
  };
}

function upsertIncident(state, incident, receivedAt) {
  if (!incident) return state;
  const id = incident.id || incident.incidentId;
  if (!id) return state;
  const cleanIncident = stripPii({ ...incident, id });
  const existing = state.incidentsById[id];
  if (isOlder(cleanIncident.timestamp || receivedAt, existing?.timestamp)) return state;
  return {
    ...state,
    now: receivedAt || state.now,
    incidentsById: { ...state.incidentsById, [id]: cleanIncident },
    selectedSubjectId: state.selectedSubjectId || `incident:${id}`
  };
}

function upsertTrip(state, trip, receivedAt) {
  if (!trip) return state;
  const id = trip.id || trip.tripId;
  if (!id) return state;
  const cleanTrip = stripPii({ ...trip, id });
  return {
    ...state,
    now: receivedAt || state.now,
    tripsById: { ...state.tripsById, [id]: cleanTrip },
    selectedSubjectId: state.selectedSubjectId || `trip:${id}`
  };
}

function appendBreadcrumb(state, breadcrumb, receivedAt) {
  if (!breadcrumb?.tripId) return state;
  const cleanBreadcrumb = stripPii(breadcrumb);
  const currentTrail = state.breadcrumbsByTripId[breadcrumb.tripId] || [];
  const exists = currentTrail.some(point => getRecordId(point) === getRecordId(cleanBreadcrumb));
  const nextTrail = exists
    ? currentTrail.map(point => getRecordId(point) === getRecordId(cleanBreadcrumb) ? cleanBreadcrumb : point)
    : [...currentTrail, cleanBreadcrumb];

  return {
    ...state,
    now: receivedAt || state.now,
    breadcrumbsByTripId: {
      ...state.breadcrumbsByTripId,
      [breadcrumb.tripId]: sortBreadcrumbs(nextTrail)
    }
  };
}

function upsertHazard(state, hazard) {
  if (!hazard) return state;
  const id = hazard.id || hazard.hazardId;
  if (!id) return state;
  return {
    ...state,
    hazardsById: { ...state.hazardsById, [id]: stripPii({ ...hazard, id }) }
  };
}

function upsertResponder(state, responder) {
  if (!responder) return state;
  const id = responder.id || responder.responderId;
  if (!id) return state;
  return {
    ...state,
    respondersById: { ...state.respondersById, [id]: stripPii({ ...responder, id }) }
  };
}

function buildTripSubject(state, trip) {
  const trail = state.breadcrumbsByTripId[trip.id] || [];
  const latest = trail[trail.length - 1] || {};
  return stripPii({
    subjectId: `trip:${trip.id}`,
    tripId: trip.id,
    touristId: trip.touristId || trip.tourist_id || 'UNKNOWN',
    idHash: trip.idHash || trip.id_hash,
    status: normalizeTripStatus(trip, latest, state.now),
    lat: numberOrNull(latest.lat ?? trip.lat),
    lon: numberOrNull(latest.lon ?? trip.lon),
    accuracyMeters: numberOrNull(latest.accuracyMeters ?? latest.accuracy_meters ?? latest.horizontal_accuracy ?? trip.accuracyMeters ?? trip.accuracy_meters ?? trip.horizontal_accuracy),
    batteryPercent: numberOrNull(latest.batteryPercent ?? latest.battery_pct ?? trip.batteryPercent ?? trip.battery_pct),
    lastSeenAt: latest.timestamp || latest.createdAt || latest.created_at || trip.updatedAt || trip.updated_at || trip.startedAt || trip.started_at,
    plannedRouteId: trip.plannedRouteId || trip.planned_route_id,
    currentZoneId: latest.currentZoneId || trip.currentZoneId,
    riskScore: numberOrNull(latest.riskScore ?? latest.risk_score ?? trip.riskScore ?? trip.risk_score),
    source: latest.source || trip.source || inferFixtureSource(trip),
    staleStatus: computeStaleStatus(latest.timestamp || latest.createdAt || latest.created_at || trip.updatedAt || trip.updated_at || trip.startedAt || trip.started_at, state.now, false),
    isStale: isStale(latest.timestamp || latest.createdAt || latest.created_at || trip.updatedAt || trip.updated_at || trip.startedAt || trip.started_at, state.now),
    trajectory: trail
  });
}

function buildIncidentSubject(state, incident) {
  const tripId = incident.tripId || incident.trip_id;
  const trip = tripId ? state.tripsById[tripId] : null;
  const trail = tripId ? state.breadcrumbsByTripId[tripId] || [] : [];
  const latest = trail[trail.length - 1] || {};
  const lastSeenAt = incident.timestamp || incident.updatedAt || incident.updated_at || latest.timestamp || latest.createdAt || latest.created_at || trip?.updatedAt || trip?.updated_at || trip?.startedAt || trip?.started_at;
  return stripPii({
    ...incident,
    subjectId: `incident:${incident.id}`,
    incidentId: incident.id,
    tripId,
    touristId: incident.touristId || incident.tourist_id || trip?.touristId || trip?.tourist_id || 'UNKNOWN',
    idHash: incident.idHash || incident.id_hash || trip?.idHash || trip?.id_hash,
    status: incident.status === 'RESOLVED' ? 'RESOLVED' : 'SOS',
    incidentStatus: incident.status || 'OPEN',
    lat: numberOrNull(incident.lat ?? latest.lat ?? trip?.lat),
    lon: numberOrNull(incident.lon ?? latest.lon ?? trip?.lon),
    accuracyMeters: numberOrNull(incident.accuracyMeters ?? incident.accuracy_meters ?? incident.horizontal_accuracy ?? latest.accuracyMeters ?? latest.accuracy_meters ?? latest.horizontal_accuracy),
    batteryPercent: numberOrNull(incident.batteryPct ?? incident.battery_pct ?? incident.batteryPercent ?? latest.batteryPercent ?? latest.battery_pct),
    lastSeenAt,
    plannedRouteId: incident.plannedRouteId || trip?.plannedRouteId || trip?.planned_route_id,
    currentZoneId: incident.currentZoneId || latest.currentZoneId || trip?.currentZoneId,
    riskScore: numberOrNull(incident.riskScore ?? latest.riskScore),
    source: incident.channel || latest.source || trip?.source || inferFixtureSource(incident),
    staleStatus: computeStaleStatus(lastSeenAt, state.now, true),
    isStale: isStale(lastSeenAt, state.now),
    trajectory: trail
  });
}

function normalizeTripStatus(trip, latest, now) {
  const stale = computeStaleStatus(latest.timestamp || latest.createdAt || latest.created_at || trip.updatedAt || trip.updated_at || trip.startedAt || trip.started_at, now, false);
  if (stale === 'STALE') return 'STALE';
  return trip.status || 'ACTIVE';
}

function computeStaleStatus(timestamp, now, isEmergency) {
  if (!timestamp) return isEmergency ? 'EMERGENCY_STALE' : 'STALE';
  const ageMs = new Date(now).getTime() - new Date(timestamp).getTime();
  if (!Number.isFinite(ageMs)) return isEmergency ? 'EMERGENCY_STALE' : 'STALE';
  if (ageMs <= 2 * 60 * 1000) return 'LIVE';
  if (ageMs <= 10 * 60 * 1000) return 'RECENT';
  return isEmergency ? 'EMERGENCY_STALE' : 'STALE';
}

function isStale(timestamp, now) {
  const status = computeStaleStatus(timestamp, now, false);
  return status === 'STALE';
}

function normalizeBreadcrumbMap(map) {
  return Object.fromEntries(
    Object.entries(map).map(([tripId, trail]) => [tripId, sortBreadcrumbs((trail || []).map(stripPii))])
  );
}

function indexById(items) {
  return Object.fromEntries(
    (items || [])
      .map(item => stripPii({ ...item, id: item.id || item.tripId || item.incidentId || item.hazardId || item.responderId }))
      .filter(item => item.id)
      .map(item => [item.id, item])
  );
}

function sortBreadcrumbs(trail) {
  return [...trail].sort((a, b) => compareTimestamps(a.timestamp || a.createdAt, b.timestamp || b.createdAt));
}

function compareTimestamps(a, b) {
  return new Date(a || 0).getTime() - new Date(b || 0).getTime();
}

function isOlder(candidateTimestamp, existingTimestamp) {
  if (!candidateTimestamp || !existingTimestamp) return false;
  return compareTimestamps(candidateTimestamp, existingTimestamp) < 0;
}

function stripPii(record = {}) {
  if (!record || typeof record !== 'object' || Array.isArray(record)) return record;
  return Object.fromEntries(
    Object.entries(record)
      .filter(([key]) => !PII_KEYS.has(key) && !isSensitiveKey(key))
      .map(([key, value]) => [key, stripPiiValue(value)])
  );
}

function stripPiiValue(value) {
  if (Array.isArray(value)) return value.map(stripPiiValue);
  if (value && typeof value === 'object') return stripPii(value);
  return value;
}

function isSensitiveKey(key) {
  const normalized = key.toLowerCase().replace(/[_-]/g, '');
  return normalized.includes('passport')
    || normalized.includes('aadhaar')
    || normalized.includes('phone')
    || normalized.includes('emergencycontact')
    || normalized === 'identity'
    || normalized.includes('identitydocument');
}

function numberOrNull(value) {
  if (value === null || value === undefined || value === '') return null;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

function getRecordId(record) {
  return record.id || record.breadcrumbId || `${record.tripId}:${record.timestamp}`;
}

function inferFixtureSource(record) {
  return record.source || 'GPS';
}
