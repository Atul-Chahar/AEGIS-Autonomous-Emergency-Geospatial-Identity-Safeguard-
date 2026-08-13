const incidentService = require('../services/IncidentService');

module.exports = {
  async handleSosDispatch(req, res, next) {
    try {
      const result = await incidentService.processSosDispatch(req.body);
      
      // Broadcast SOS event to connected authority WebSocket clients
      if (req.app.get('wsBroadcaster')) {
        req.app.get('wsBroadcaster')('EMERGENCY_SOS', result);
      }

      res.status(200).json(result);
    } catch (err) {
      next(err);
    }
  }
};
