const express = require('express');
const responderController = require('../controllers/ResponderController');
const router = express.Router();

router.get('/responders', responderController.getResponders);
router.post('/responders/match', responderController.matchNearest);

module.exports = router;
