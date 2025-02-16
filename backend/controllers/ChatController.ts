import { ChatService } from "../service/ChatService";
import { NextFunction } from "express"

export class ChatController {
    private chatService: ChatService;

    constructor() {
        this.chatService = new ChatService();
    }

    async createChat(req: Request, res: Response, next: NextFunction) {
        let chat = (req as any).body
        return this.chatService.createChat(chat._id, chat.member_1, chat.member_2);
    }

    async getChats(req: Request, res: Response, next: NextFunction) {
        const userId = (req as any).param.usr_id
        return this.chatService.getChats(userId);
    }

    async getChatMessages(req: Request, res: Response, next: NextFunction) {
        const chatId = (req as any).param.id
        return this.chatService.getChatMessages(chatId);
    }

    async addMessage(req: Request, res: Response, next: NextFunction) {
        let message = (req as any).body
        return this.chatService.addMessage(message._id, message.chatroom_id, message.sender, message.message);
    }

    async deleteMessage(req: Request, res: Response, next: NextFunction) {
        const messageId = (req as any).param.id
        return this.chatService.deleteMessage(messageId);
    }

    async deleteChat(req: Request, res: Response, next: NextFunction) {
        const chatId = (req as any).param.message_id
        return this.chatService.deleteChat(chatId);
    }
}