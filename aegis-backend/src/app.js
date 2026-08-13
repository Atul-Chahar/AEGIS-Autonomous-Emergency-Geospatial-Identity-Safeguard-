const express = require('express');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const env = require('./config/env');
const apiRoutes = require('./routes');

const app = express();

// 1. Security & CORS Middleware
app.use(cors());
app.use(express.json({ limit: '2mb' }));

// 2. Express Rate Limiting
const limiter = rateLimit({
  windowMs: env.rateLimitWindowMs,
  max: env.rateLimitMax,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'TOO_MANY_REQUESTS: Rate limit exceeded. Please retry later.' }
});
app.use('/api/', limiter);

// 3. API Routes Aggregator
app.use('/api', apiRoutes);

// 4. Centralized Structured Error Handling
app.use((err, req, res, next) => {
  const status = err.status || 500;
  const message = err.message || 'INTERNAL_SERVER_ERROR';
  res.status(status).json({
    error: message,
    timestamp: new Date().toISOString(),
    path: req.originalUrl
  });
});

module.exports = app;
