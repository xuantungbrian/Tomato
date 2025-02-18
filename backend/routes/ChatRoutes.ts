import { ChatController } from "../controllers/ChatController";

const chatController = new ChatController();

export const ChatRoutes = [
    {
        method: "get",
        route: "/chats/:usr_id",            
        action: chatController.getChats,
        validation: []
    },
    {
        method: "get",
        route: "/chats/:usr_id/messages/:id",    
        action: chatController.getChatMessages,
        validation: []
    },
    {
        method: "post",
        route: "/chats/:usr_id",             
        action: chatController.createChat,
        validation: []
    },
    {
        method: "put",
        route: "/chats/:usr_id",          
        action: chatController.addMessage,
        validation: []
    },
    {
        method: "delete",
        route: "/chats/:usr_id/messages/:id",          
        action: chatController.deleteChat,
        validation: []
    },
    {
        method: "delete",
        route: "/chats/:usr_id/messages/:id/:message_id",   
        action: chatController.deleteMessage,
        validation: []
    }
   
]