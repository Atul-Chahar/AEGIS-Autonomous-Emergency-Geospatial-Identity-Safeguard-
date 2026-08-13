const { issueAuthorityToken } = require('../auth/jwtAuth');

module.exports = {
  login(req, res) {
    const { username, password } = req.body;
    // Authority authentication check
    if (username === 'admin' && (password === 'aegis2026' || password === 'admin')) {
      const token = issueAuthorityToken({ id: 'AUTH-CMD-01', role: 'CHIEF_DISPATCHER' });
      return res.json({
        success: true,
        token,
        user: { id: 'AUTH-CMD-01', name: 'Authority Operator', role: 'CHIEF_DISPATCHER' }
      });
    }
    return res.status(401).json({ error: 'INVALID_CREDENTIALS: Username or password incorrect' });
  }
};
