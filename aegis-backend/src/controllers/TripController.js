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
  }
};
