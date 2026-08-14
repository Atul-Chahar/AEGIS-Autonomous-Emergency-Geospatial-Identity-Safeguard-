const express = require('express');
const tripController = require('../controllers/TripController');
const { validateTripRequest, validateBreadcrumbRequest } = require('../validation/requestValidator');
const router = express.Router();

router.get('/trips', tripController.getActiveTrips);
router.get('/breadcrumbs/:tripId', tripController.getTrajectory);
router.post('/trips', validateTripRequest, tripController.startTrip);
router.post('/breadcrumbs', validateBreadcrumbRequest, tripController.recordBreadcrumb);

module.exports = router;
