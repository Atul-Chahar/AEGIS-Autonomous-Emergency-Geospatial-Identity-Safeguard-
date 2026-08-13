const express = require('express');
const sosController = require('../controllers/SosController');
const { validateSosRequest } = require('../validation/requestValidator');
const router = express.Router();

router.post('/sos', validateSosRequest, sosController.handleSosDispatch);

module.exports = router;
