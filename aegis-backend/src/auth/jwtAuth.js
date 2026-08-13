const jwt = require('jsonwebtoken');
const env = require('../config/env');

function issueAuthorityToken(authorityUser) {
  return jwt.sign(
    { userId: authorityUser.id, role: authorityUser.role || 'AUTHORITY_OPERATOR' },
    env.jwtSecret,
    { expiresIn: '12h' }
  );
}

function verifyTokenMiddleware(req, res, next) {
  const authHeader = req.headers['authorization'];
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'UNAUTHORIZED: Missing or invalid Bearer token' });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, env.jwtSecret);
    req.user = decoded;
    next();
  } catch (err) {
    return res.status(403).json({ error: 'FORBIDDEN: Token expired or invalid signature' });
  }
}

module.exports = {
  issueAuthorityToken,
  verifyTokenMiddleware
};
