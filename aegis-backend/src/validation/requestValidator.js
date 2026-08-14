module.exports = {
  validateSosRequest(req, res, next) {
    const { touristId, lat, lon } = req.body;
    if (lat !== undefined && (isNaN(parseFloat(lat)) || parseFloat(lat) < -90 || parseFloat(lat) > 90)) {
      return res.status(400).json({ error: 'INVALID_INPUT: Latitude must be a valid number between -90 and 90' });
    }
    if (lon !== undefined && (isNaN(parseFloat(lon)) || parseFloat(lon) < -180 || parseFloat(lon) > 180)) {
      return res.status(400).json({ error: 'INVALID_INPUT: Longitude must be a valid number between -180 and 180' });
    }
    next();
  },

  validateIdentityRegister(req, res, next) {
    const { touristId, salt } = req.body;
    if (!touristId || !salt) {
      return res.status(400).json({ error: 'INVALID_INPUT: touristId and salt are required parameters' });
    }
    next();
  },

  validateTripRequest(req, res, next) {
    const { touristId } = req.body;
    if (!touristId) {
      return res.status(400).json({ error: 'INVALID_INPUT: touristId is a required parameter' });
    }
    next();
  },

  validateBreadcrumbRequest(req, res, next) {
    const { tripId, lat, lon } = req.body;
    if (!tripId) {
      return res.status(400).json({ error: 'INVALID_INPUT: tripId is a required parameter' });
    }
    if (lat === undefined || isNaN(parseFloat(lat)) || parseFloat(lat) < -90 || parseFloat(lat) > 90) {
      return res.status(400).json({ error: 'INVALID_INPUT: Latitude must be a valid number between -90 and 90' });
    }
    if (lon === undefined || isNaN(parseFloat(lon)) || parseFloat(lon) < -180 || parseFloat(lon) > 180) {
      return res.status(400).json({ error: 'INVALID_INPUT: Longitude must be a valid number between -180 and 180' });
    }
    next();
  }
};
