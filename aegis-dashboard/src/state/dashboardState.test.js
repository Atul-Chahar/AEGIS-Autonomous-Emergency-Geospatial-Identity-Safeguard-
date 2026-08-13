import test from 'node:test';
import assert from 'node:assert/strict';

import {
  createInitialDashboardState,
  dashboardReducer,
  hydrateDashboardState,
  selectDashboardSubjects,
  selectSelectedSubject
} from './dashboardReducer.js';

const now = '2026-08-14T10:00:00.000Z';

test('hydrates active trips as trackable subjects when there are no incidents', () => {
  const state = hydrateDashboardState({
    trips: [
      {
        id: 'TRIP-1',
        touristId: 'TST-100',
        plannedRouteId: 'living-roots',
        status: 'ACTIVE',
        passportOrAadhaar: 'SECRET'
      }
    ],
    breadcrumbsByTripId: {
      'TRIP-1': [
        {
          id: 'B1',
          tripId: 'TRIP-1',
          lat: 25.145,
          lon: 91.265,
          accuracyMeters: 6,
          batteryPercent: 84,
          timestamp: '2026-08-14T09:59:00.000Z'
        }
      ]
    },
    now
  });

  const subjects = selectDashboardSubjects(state);

  assert.equal(subjects.length, 1);
  assert.equal(subjects[0].subjectId, 'trip:TRIP-1');
  assert.equal(subjects[0].status, 'ACTIVE');
  assert.equal(subjects[0].staleStatus, 'LIVE');
  assert.equal(subjects[0].batteryPercent, 84);
  assert.equal(subjects[0].passportOrAadhaar, undefined);
});

test('joins an incident to its active trip and exposes the breadcrumb trail', () => {
  const state = hydrateDashboardState({
    trips: [{ id: 'TRIP-1', touristId: 'TST-100', status: 'ACTIVE' }],
    incidents: [
      {
        id: 'INC-1',
        tripId: 'TRIP-1',
        touristId: 'TST-100',
        lat: 25.15,
        lon: 91.27,
        status: 'OPEN',
        riskScore: 100,
        phoneNumber: '+911234567890'
      }
    ],
    breadcrumbsByTripId: {
      'TRIP-1': [
        { id: 'B1', tripId: 'TRIP-1', lat: 25.14, lon: 91.26, timestamp: '2026-08-14T09:50:00.000Z' },
        { id: 'B2', tripId: 'TRIP-1', lat: 25.15, lon: 91.27, timestamp: '2026-08-14T09:58:00.000Z' }
      ]
    },
    selectedSubjectId: 'incident:INC-1',
    now
  });

  const selected = selectSelectedSubject(state);

  assert.equal(selected.subjectId, 'incident:INC-1');
  assert.equal(selected.tripId, 'TRIP-1');
  assert.equal(selected.status, 'SOS');
  assert.equal(selected.trajectory.length, 2);
  assert.equal(selected.phoneNumber, undefined);
});

test('records a websocket breadcrumb without losing selected subject state', () => {
  const initial = hydrateDashboardState({
    trips: [{ id: 'TRIP-1', touristId: 'TST-100', status: 'ACTIVE' }],
    breadcrumbsByTripId: {
      'TRIP-1': [
        { id: 'B1', tripId: 'TRIP-1', lat: 25.14, lon: 91.26, timestamp: '2026-08-14T09:55:00.000Z' }
      ]
    },
    selectedSubjectId: 'trip:TRIP-1',
    now
  });

  const next = dashboardReducer(initial, {
    type: 'BREADCRUMB_RECORDED',
    payload: {
      id: 'B2',
      tripId: 'TRIP-1',
      lat: 25.151,
      lon: 91.271,
      accuracyMeters: 4,
      batteryPercent: 79,
      timestamp: '2026-08-14T10:00:00.000Z'
    },
    receivedAt: now
  });

  const selected = selectSelectedSubject(next);

  assert.equal(next.selectedSubjectId, 'trip:TRIP-1');
  assert.equal(selected.lat, 25.151);
  assert.equal(selected.lon, 91.271);
  assert.equal(selected.trajectory.length, 2);
  assert.equal(selected.staleStatus, 'LIVE');
});

test('marks old locations stale for the operator', () => {
  const state = hydrateDashboardState({
    trips: [{ id: 'TRIP-1', touristId: 'TST-100', status: 'ACTIVE' }],
    breadcrumbsByTripId: {
      'TRIP-1': [
        { id: 'B1', tripId: 'TRIP-1', lat: 25.14, lon: 91.26, timestamp: '2026-08-14T09:40:00.000Z' }
      ]
    },
    now
  });

  assert.equal(selectDashboardSubjects(state)[0].staleStatus, 'STALE');
});

test('normalizes Postgres snake_case rows into dashboard subjects', () => {
  const state = hydrateDashboardState({
    trips: [
      {
        id: 'TRIP-DB',
        tourist_id: 'TST-DB',
        id_hash: '0xabc123',
        planned_route_id: 'db-route',
        started_at: '2026-08-14T09:00:00.000Z',
        status: 'ACTIVE'
      }
    ],
    breadcrumbsByTripId: {
      'TRIP-DB': [
        {
          id: 'B1',
          trip_id: 'TRIP-DB',
          lat: '25.200',
          lon: '91.300',
          horizontal_accuracy: '7',
          battery_pct: '67',
          risk_score: '41',
          created_at: '2026-08-14T09:59:00.000Z'
        }
      ]
    },
    now
  });

  const subject = selectDashboardSubjects(state)[0];

  assert.equal(subject.touristId, 'TST-DB');
  assert.equal(subject.idHash, '0xabc123');
  assert.equal(subject.plannedRouteId, 'db-route');
  assert.equal(subject.batteryPercent, 67);
  assert.equal(subject.accuracyMeters, 7);
  assert.equal(subject.riskScore, 41);
});

test('strips snake_case and nested PII from records', () => {
  const state = hydrateDashboardState({
    trips: [
      {
        id: 'TRIP-PII',
        tourist_id: 'TST-PII',
        phone_number: '+911234567890',
        identity: { passport_number: 'P123', display: 'raw' },
        emergency_contact: { phone: '+910000000000' },
        status: 'ACTIVE'
      }
    ],
    breadcrumbsByTripId: {
      'TRIP-PII': [
        {
          id: 'B1',
          trip_id: 'TRIP-PII',
          lat: 25.14,
          lon: 91.26,
          timestamp: '2026-08-14T09:59:00.000Z'
        }
      ]
    },
    now
  });

  const subject = selectDashboardSubjects(state)[0];

  assert.equal(subject.phone_number, undefined);
  assert.equal(subject.identity, undefined);
  assert.equal(subject.emergency_contact, undefined);
});

test('resolved incidents do not hide the active trip subject', () => {
  const state = hydrateDashboardState({
    trips: [{ id: 'TRIP-1', touristId: 'TST-100', status: 'ACTIVE' }],
    incidents: [{ id: 'INC-1', tripId: 'TRIP-1', touristId: 'TST-100', status: 'RESOLVED' }],
    breadcrumbsByTripId: {
      'TRIP-1': [
        { id: 'B1', tripId: 'TRIP-1', lat: 25.14, lon: 91.26, timestamp: '2026-08-14T09:59:00.000Z' }
      ]
    },
    now
  });

  const subjects = selectDashboardSubjects(state);

  assert.equal(subjects.some(subject => subject.subjectId === 'trip:TRIP-1'), true);
  assert.equal(subjects.some(subject => subject.subjectId === 'incident:INC-1'), false);
});
