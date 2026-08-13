const express = require('express');
const searchController = require('../controllers/SearchProbabilityController');
const router = express.Router();

router.post('/search-probability', searchController.calculateSearchProbability);
router.get('/search-probability', searchController.calculateSearchProbability);

module.exports = router;
