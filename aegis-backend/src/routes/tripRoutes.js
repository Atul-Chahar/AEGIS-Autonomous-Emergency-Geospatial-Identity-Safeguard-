const express = require('express');
const tripController = require('../controllers/TripController');
const router = express.Router();

router.get('/trips', tripController.getActiveTrips);
router.get('/breadcrumbs/:tripId', tripController.getTrajectory);

module.exports = router;
