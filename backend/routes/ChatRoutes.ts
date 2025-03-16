import { ChatController } from "../controllers/ChatController";
import {Route} from "./RouteInterface";

const chatController = new ChatController();

export const ChatRoutes: Route[] = [
    {
        method: "get",
        route: "/chats",            
        action: chatController.getChats,
        validation: [],
        protected: true
    },
    {
        method: "get",
        route: "/chats/:id",    
        action: chatController.getChatMessages,
        validation: [],
        protected: true
    },
    {
        method: "post",
        route: "/chats",             
        action: chatController.createChat,
        validation: [],
        protected: true
    },
    {
        method: "post",
        route: "/chat/:id",          
        action: chatController.addMessage,
        validation: [],
        protected: true
    },
    {
        method: "delete",
        route: "/chats/:id",          
        action: chatController.deleteChat,
        validation: [],
        protected: true
    },
    {
        method: "delete",
        route: "/chat/:id/messages/:message_id",   
        action: chatController.deleteMessage,
        validation: [],
        protected: true
    }
]