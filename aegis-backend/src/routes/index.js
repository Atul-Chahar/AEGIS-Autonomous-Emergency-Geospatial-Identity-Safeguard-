const express = require('express');
const healthRoutes = require('./healthRoutes');
const identityRoutes = require('./identityRoutes');
const sosRoutes = require('./sosRoutes');
const geofenceRoutes = require('./geofenceRoutes');
const incidentRoutes = require('./incidentRoutes');
const hazardRoutes = require('./hazardRoutes');
const authRoutes = require('./authRoutes');
const responderRoutes = require('./responderRoutes');
const tripRoutes = require('./tripRoutes');

const router = express.Router();

router.use(healthRoutes);
router.use(identityRoutes);
router.use(sosRoutes);
router.use(geofenceRoutes);
router.use(incidentRoutes);
router.use(hazardRoutes);
router.use(authRoutes);
router.use(responderRoutes);
router.use(tripRoutes);

module.exports = router;
