const hazardService = require('../services/HazardService');

module.exports = {
  async submitHazardReport(req, res, next) {
    try {
      const { hazardType, lat, lon, description, reporterId, reporterRole, evidenceRef } = req.body;
      if (!hazardType || lat === undefined || lon === undefined) {
        return res.status(400).json({ error: 'INVALID_INPUT: hazardType, lat, and lon are required' });
      }

      const result = await hazardService.processHazardReport({
        hazardType,
        lat,
        lon,
        description,
        reporterId,
        reporterRole,
        evidenceRef
      });

      if (req.app.get('wsBroadcaster')) {
        req.app.get('wsBroadcaster')('HAZARD_EVALUATED', result);
      }

      res.status(201).json(result);
    } catch (err) {
      next(err);
    }
  },

  async getHazards(req, res, next) {
    try {
      const list = await hazardService.listHazards();
      res.json(list);
    } catch (err) {
      next(err);
    }
  }
};
