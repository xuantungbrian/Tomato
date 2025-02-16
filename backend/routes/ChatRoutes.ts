import { ChatController } from "../controllers/ChatController";

const chatController = new ChatController();

export const ChatRoutes = [
    {
        method: "get",
        route: "/:usr_id/chats",            
        action: chatController.getChats,
        validation: []
    },
    {
        method: "get",
        route: "/:usr_id/chats/:id",    
        action: chatController.getChatMessages,
        validation: []
    },
    {
        method: "post",
        route: "/:usr_id/chats",             
        action: chatController.createChat,
        validation: []
    },
    {
        method: "put",
        route: "/:usr_id/chats/",          
        action: chatController.addMessage,
        validation: []
    },
    {
        method: "delete",
        route: "/:usr_id/chats/:id",          
        action: chatController.deleteChat,
        validation: []
    },
    {
        method: "delete",
        route: "/:usr_id/chats/:id/:message_id",          
        action: chatController.deleteMessage,
        validation: []
    }
   
]