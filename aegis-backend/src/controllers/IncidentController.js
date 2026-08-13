const incidentService = require('../services/IncidentService');

module.exports = {
  async getIncidents(req, res, next) {
    try {
      const incidents = await incidentService.listAllIncidents();
      res.json(incidents);
    } catch (err) {
      next(err);
    }
  },

  async updateStatus(req, res, next) {
    try {
      const { id } = req.params;
      const { status } = req.body;
      const updated = await incidentService.updateStatus(id, status);

      if (!updated) {
        return res.status(404).json({ error: 'NOT_FOUND: Incident not found' });
      }

      if (req.app.get('wsBroadcaster')) {
        req.app.get('wsBroadcaster')('INCIDENT_STATUS_CHANGED', updated);
      }

      res.json(updated);
    } catch (err) {
      next(err);
    }
  }
};
