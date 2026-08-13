const express = require('express');
const geofenceController = require('../controllers/GeofenceController');
const router = express.Router();

router.get('/geofences', geofenceController.getGeofences);

module.exports = router;
