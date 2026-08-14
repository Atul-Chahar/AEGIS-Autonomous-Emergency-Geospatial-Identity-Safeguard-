const tripRepository = require('../repositories/TripRepository');

module.exports = {
  async getActiveTrips(req, res, next) {
    try {
      const trips = await tripRepository.getActiveTrips();
      res.json(trips);
    } catch (err) {
      next(err);
    }
  },

  async getTrajectory(req, res, next) {
    try {
      const { tripId } = req.params;
      const breadcrumbs = await tripRepository.getTripBreadcrumbs(tripId);
      res.json(breadcrumbs);
    } catch (err) {
      next(err);
    }
  },

  /** Ingests a trip started by the Android BlackBox and broadcasts it live. */
  async startTrip(req, res, next) {
    try {
      const trip = await tripRepository.saveTrip(req.body);
      if (req.app.get('wsBroadcaster')) {
        req.app.get('wsBroadcaster')('TRIP_STARTED', trip);
      }
      res.status(201).json(trip);
    } catch (err) {
      next(err);
    }
  },

  /** Ingests a breadcrumb from the Android BlackBox and broadcasts it live. */
  async recordBreadcrumb(req, res, next) {
    try {
      const breadcrumb = await tripRepository.saveBreadcrumb(req.body);
      if (req.app.get('wsBroadcaster')) {
        req.app.get('wsBroadcaster')('BREADCRUMB_RECORDED', breadcrumb);
      }
      res.status(201).json(breadcrumb);
    } catch (err) {
      next(err);
    }
  }
};
