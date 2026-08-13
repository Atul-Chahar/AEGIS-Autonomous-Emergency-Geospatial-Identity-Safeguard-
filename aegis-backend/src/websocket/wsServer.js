const { WebSocketServer, WebSocket } = require('ws');
const jwt = require('jsonwebtoken');
const env = require('../config/env');

function setupWebSocketServer(httpServer) {
  const wss = new WebSocketServer({ server: httpServer });

  function broadcast(type, payload) {
    const data = JSON.stringify({ type, payload, timestamp: new Date().toISOString() });
    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(data);
      }
    });
  }

  wss.on('connection', (ws, req) => {
    // Check token authentication if provided in query string or headers
    const url = new URL(req.url, 'http://localhost');
    const token = url.searchParams.get('token');
    
    let isAuthenticated = false;
    if (token) {
      try {
        jwt.verify(token, env.jwtSecret);
        isAuthenticated = true;
      } catch (err) {
        // Token invalid, keep connection open in read-only mode or attach unauthenticated tag
      }
    }

    ws.send(JSON.stringify({
      type: 'CONNECTED',
      payload: { message: 'AEGIS WebSocket Gateway Active', authenticated: isAuthenticated }
    }));

    ws.on('message', (message) => {
      try {
        const parsed = JSON.parse(message.toString());
        if (parsed.type === 'PING') {
          ws.send(JSON.stringify({ type: 'PONG', timestamp: new Date().toISOString() }));
        }
      } catch (err) {}
    });
  });

  return { wss, broadcast };
}

module.exports = setupWebSocketServer;
