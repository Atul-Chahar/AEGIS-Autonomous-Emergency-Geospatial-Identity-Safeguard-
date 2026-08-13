const responderRepository = require('../repositories/ResponderRepository');

module.exports = {
  async getResponders(req, res, next) {
    try {
      const list = await responderRepository.getAllResponders();
      res.json(list);
    } catch (err) {
      next(err);
    }
  },

  async matchNearest(req, res, next) {
    try {
      const { lat, lon, requiredCapabilities } = req.body;
      if (lat === undefined || lon === undefined) {
        return res.status(400).json({ error: 'INVALID_INPUT: lat and lon required' });
      }

      const evaluation = await responderRepository.matchResponders(
        parseFloat(lat),
        parseFloat(lon),
        requiredCapabilities || ['MEDICAL', 'ROPE']
      );

      res.json({
        geographicallyNearest: evaluation.geographicallyNearest,
        operationallyRecommended: evaluation.operationallyRecommended,
        divergenceExplanation: evaluation.divergenceExplanation,
        nearestResponders: evaluation.sortedResponders
      });
    } catch (err) {
      next(err);
    }
  }
};
