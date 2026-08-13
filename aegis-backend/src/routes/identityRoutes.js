const express = require('express');
const identityController = require('../controllers/IdentityController');
const { validateIdentityRegister } = require('../validation/requestValidator');
const router = express.Router();

router.post('/identity/register', validateIdentityRegister, identityController.registerIdentity);
router.get('/identity/verify/:idHash', identityController.verifyVoucher);

module.exports = router;
