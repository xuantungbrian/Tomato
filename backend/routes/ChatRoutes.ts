import { ChatController } from "../controllers/ChatController";

const chatController = new ChatController();

export const ChatRoutes = [
    {
        method: "get",
        route: "/chats",            
        action: chatController.getChats,
        validation: []
    },
    {
        method: "get",
        route: "/chats/:id",    
        action: chatController.getChatMessages,
        validation: []
    },
    {
        method: "post",
        route: "/chats",             
        action: chatController.createChat,
        validation: []
    },
    {
        method: "post",
        route: "/chat/:id",          
        action: chatController.addMessage,
        validation: []
    },
    {
        method: "delete",
        route: "/chats/:id",          
        action: chatController.deleteChat,
        validation: []
    },
    {
        method: "delete",
        route: "/chat/:id/messages/:message_id",   
        action: chatController.deleteMessage,
        validation: []
    }
]