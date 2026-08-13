const searchEngine = require('../geospatial/SearchProbabilityEngine');

module.exports = {
  calculateSearchProbability(req, res, next) {
    try {
      const result = searchEngine.calculateSearchProbability(req.body || {});
      res.json(result);
    } catch (err) {
      next(err);
    }
  }
};
