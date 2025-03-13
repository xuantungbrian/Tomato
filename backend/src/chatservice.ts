import { wss } from './wss';
import { Server as HttpServer } from 'http';

interface ChatMessage {
  user: string;
  message: string;
}

const chatService = (server: HttpServer) => {
  server.on('upgrade', (request, socket, head) => {
    wss.handleUpgrade(request, socket, head, (ws) => {
      wss.emit('connection', ws, request);
    });
  });

  wss.on('connection', (ws) => {
    ws.on('message', (data: string) => {
      const message: ChatMessage = JSON.parse(data);
      // Broadcast the message to all clients
      wss.clients.forEach((client) => {
        if (client.readyState === WebSocket.OPEN) {
          client.send(JSON.stringify(message));
        }
      });
    });
  });
};

export { chatService };
