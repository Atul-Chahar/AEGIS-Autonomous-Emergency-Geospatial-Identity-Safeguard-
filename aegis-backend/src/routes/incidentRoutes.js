const express = require('express');
const incidentController = require('../controllers/IncidentController');
const router = express.Router();

router.get('/incidents', incidentController.getIncidents);

module.exports = router;
