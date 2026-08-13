const http = require('http');
const app = require('./app');
const env = require('./config/env');
const setupWebSocketServer = require('./websocket/wsServer');

const server = http.createServer(app);

// Setup WebSocket gateway
const { broadcast } = setupWebSocketServer(server);
app.set('wsBroadcaster', broadcast);

server.listen(env.port, () => {
  console.log(`🛡️ AEGIS API Gateway running on port ${env.port} (${env.nodeEnv})`);
});

module.exports = server;
