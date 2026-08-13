const express = require('express');
const healthRoutes = require('./healthRoutes');
const identityRoutes = require('./identityRoutes');
const sosRoutes = require('./sosRoutes');
const geofenceRoutes = require('./geofenceRoutes');
const incidentRoutes = require('./incidentRoutes');
const hazardRoutes = require('./hazardRoutes');
const authRoutes = require('./authRoutes');

const router = express.Router();

router.use(healthRoutes);
router.use(identityRoutes);
router.use(sosRoutes);
router.use(geofenceRoutes);
router.use(incidentRoutes);
router.use(hazardRoutes);
router.use(authRoutes);

module.exports = router;
