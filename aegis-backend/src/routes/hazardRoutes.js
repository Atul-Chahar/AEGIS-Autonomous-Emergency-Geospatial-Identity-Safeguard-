const express = require('express');
const hazardController = require('../controllers/HazardController');
const router = express.Router();

router.post('/hazards', hazardController.submitHazardReport);
router.get('/hazards', hazardController.getHazards);

module.exports = router;
