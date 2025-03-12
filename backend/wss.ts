import WebSocket from 'ws';
import { ChatService } from './service/ChatService';
import { UserService } from './service/UserService';
import { IncomingMessage } from 'http';
import admin from "firebase-admin";
const serviceAccount = require("./serviceAccountKey.json");
// import serviceAccount from "./serviceAccountKey.json"; // Load Firebase credentials

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const startWSS = () => {
    const chatService = new ChatService();
    const userService = new UserService()
    const wssPort = Number(process.env.WSS_PORT) || 3001;
    const wss = new WebSocket.Server({ host: '0.0.0.0', port: wssPort });
    const wsRoomMapping = new Map();

    wss.on('listening', () => {
        console.log('WebSocket server is running on ws://localhost:%d', wssPort);
    });

    wss.on('connection', (ws: WebSocket, req: IncomingMessage) => {
        console.log("CONNECTION!")
        if(!req.url) {
            console.error('No URL provided');
            ws.close();
            return;
        }
        const urlParams = new URLSearchParams(req.url.slice(1)); 
        const chatId = urlParams.get('chatId');
        
        if (!chatId) {
            console.error('Chatroom ID not provided');
            ws.close();
            return;
        }

        wsRoomMapping.set(ws, chatId);

        ws.on('message', async (data: WebSocket.Data) => {
            try {
                const message = JSON.parse(data.toString()); 
                chatService.addMessage(chatId, message.sender, message.message);
                const chatInfo = await chatService.getChat(chatId)
                let receiverId = ""
                if (chatInfo) {
                    receiverId = ((message.sender == chatInfo.member_1) ? chatInfo.member_2 : chatInfo.member_1) || ""
                }
                if (!receiverId) {
                    console.error("Cannot find this user")
                }
                const receiverInfo = await userService.getUser(receiverId)
                const receiverFirebaseToken = receiverInfo?.firebaseToken || [""]
                let recipientFound = false;
                for (let client of wss.clients) {
                    if (wsRoomMapping.get(client) === chatId) {
                        if (client.readyState === WebSocket.OPEN) {
                            client.send(JSON.stringify(message));
                            if (client !== ws) {
                                recipientFound = true;
                            }
                        }
                    }
                }
                if (!recipientFound) {
                    for (let token of receiverFirebaseToken) {
                        if (token) {
                            sendPushNotification(token, message.message, chatId)
                        }
                    }
                }
            } catch (error) {
                console.error(`Error processing message: ${error}`);
            }
        });
    });
};

// Function to send push notification
function sendPushNotification(token: string, message: string, chatId: string) {
    const payload = {
        notification: {
            title: "You have a new Message",
            body: message,
        },
        token: token,
        data: {
            chatId: chatId,
        },
        android: {
            priority: "high"
        },
        apns: {
            headers: {
                "apns-priority": "10",
            },
        },
        webpush: {
            headers: {
                urgency: "high",
            },
        },
    };

    admin.messaging().send(payload as admin.messaging.Message)
        .then((response: string) => {
            console.log("Successfully sent message:", response);
        })
        .catch((error: any) => {
            console.log("Error sending message:", error);
        });
}

export default startWSS;
