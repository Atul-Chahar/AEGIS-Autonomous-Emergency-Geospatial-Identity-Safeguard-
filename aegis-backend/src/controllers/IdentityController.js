const identityService = require('../services/IdentityService');

module.exports = {
  async registerIdentity(req, res, next) {
    try {
      const result = await identityService.registerIdentity(req.body);
      res.status(201).json(result);
    } catch (err) {
      next(err);
    }
  },

  async verifyVoucher(req, res, next) {
    try {
      const { idHash } = req.params;
      const result = await identityService.verifyVoucher(idHash);
      res.json(result);
    } catch (err) {
      next(err);
    }
  }
};
