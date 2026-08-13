const express = require('express');
const incidentController = require('../controllers/IncidentController');
const router = express.Router();

router.get('/incidents', incidentController.getIncidents);
router.patch('/incidents/:id/status', incidentController.updateStatus);

module.exports = router;
