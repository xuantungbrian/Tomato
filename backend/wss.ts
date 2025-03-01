import WebSocket from 'ws';
import { ChatService } from './service/ChatService';

const startWSS = () => {
    const chatService = new ChatService();
    const wssPort = Number(process.env.WSS_PORT) || 3001;
    const wss = new WebSocket.Server({ host: '0.0.0.0', port: wssPort });
    const wsRoomMapping = new Map();

    wss.on('listening', () => {
        console.log(`WebSocket server is running on ws://localhost:${wssPort}`);
    });

    wss.on('connection', (ws, req) => {
        console.log("CONNECTION!")
        const urlParams = new URLSearchParams((req as any).url.slice(1)); 
        const chatId = urlParams.get('chatId');
        
        if (!chatId) {
            console.error('Chatroom ID not provided');
            ws.close();
            return;
        }

        wsRoomMapping.set(ws, chatId);

        ws.on('message', (data) => {
            try {
                const message = JSON.parse(data.toString()); 
                chatService.addMessage(chatId, message.sender, message.message);
                for (let client of wss.clients) {
                    if (client.readyState === WebSocket.OPEN && wsRoomMapping.get(client) === chatId) {
                        client.send(JSON.stringify(message));
                    }
                }
            } catch (error) {
                console.error(`Error processing message: ${error}`);
            }
        });
    });
};

export default startWSS;
