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
      const { lat, lon } = req.body;
      if (lat === undefined || lon === undefined) {
        return res.status(400).json({ error: 'INVALID_INPUT: lat and lon required' });
      }
      const nearestResponders = await responderRepository.findNearestResponders(parseFloat(lat), parseFloat(lon));
      res.json({ nearestResponders });
    } catch (err) {
      next(err);
    }
  }
};
