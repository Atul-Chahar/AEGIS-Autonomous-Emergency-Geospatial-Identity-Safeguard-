const incidentService = require('../services/IncidentService');

module.exports = {
  async getIncidents(req, res, next) {
    try {
      const incidents = await incidentService.listAllIncidents();
      res.json(incidents);
    } catch (err) {
      next(err);
    }
  }
};
