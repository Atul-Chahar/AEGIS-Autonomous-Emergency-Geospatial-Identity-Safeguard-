const hazardRepository = require('../repositories/HazardRepository');

module.exports = {
  async submitHazardReport(req, res, next) {
    try {
      const { hazardType, lat, lon, description, reporterId } = req.body;
      if (!hazardType || lat === undefined || lon === undefined) {
        return res.status(400).json({ error: 'INVALID_INPUT: hazardType, lat, and lon are required' });
      }

      const result = await hazardRepository.createHazardReport({
        hazardType,
        lat,
        lon,
        description,
        reporterId
      });

      if (req.app.get('wsBroadcaster')) {
        req.app.get('wsBroadcaster')('HAZARD_REPORTED', result);
      }

      res.status(201).json(result);
    } catch (err) {
      next(err);
    }
  },

  async getHazards(req, res, next) {
    try {
      const list = await hazardRepository.getAllHazards();
      res.json(list);
    } catch (err) {
      next(err);
    }
  }
};
