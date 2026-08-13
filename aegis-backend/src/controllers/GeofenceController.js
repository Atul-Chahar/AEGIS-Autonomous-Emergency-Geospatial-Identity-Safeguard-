const safetyZoneRepository = require('../repositories/SafetyZoneRepository');

module.exports = {
  async getGeofences(req, res, next) {
    try {
      const zones = await safetyZoneRepository.getAllZones();
      res.json(zones);
    } catch (err) {
      next(err);
    }
  }
};
