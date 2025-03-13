import WebSocket, { Server as WebSocketServer } from 'ws';
import { IncomingMessage } from 'http';

interface CustomWebSocket extends WebSocket {
  isAlive: boolean;
}

const wss = new WebSocketServer({ noServer: true });

wss.on('connection', (ws: CustomWebSocket, req: IncomingMessage) => {
  ws.isAlive = true;

  ws.on('pong', () => {
    ws.isAlive = true;
  });

  ws.on('message', (message: string) => {
    // Handle incoming message
  });

  ws.on('close', () => {
    // Handle connection close
  });
});

const interval = setInterval(() => {
  wss.clients.forEach((ws: WebSocket) => {
    const customWs = ws as CustomWebSocket;
    if (!customWs.isAlive) return customWs.terminate();

    customWs.isAlive = false;
    customWs.ping();
  });
}, 30000);

wss.on('close', () => {
  clearInterval(interval);
});

export { wss };
