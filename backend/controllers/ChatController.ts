import { ChatService } from "../service/ChatService";
import { Request, Response, NextFunction } from "express"

export class ChatController {
    private chatService: ChatService;

    constructor() {
        this.chatService = new ChatService();
    }

    async createChat(req: Request, res: Response, next: NextFunction) {
        let chat = (req as any).body
        res.json(await this.chatService.createChat(chat.member_1, chat.member_2))
    }

    async getChats(req: Request, res: Response, next: NextFunction) {
        const userId = (req as any).user.id
        res.json(await this.chatService.getChats(userId))
    }

    async getChatMessages(req: Request, res: Response, next: NextFunction) {
        const chatId = (req as any).param.id
        res.json(await this.chatService.getChatMessages(chatId));
    }

    async addMessage(req: Request, res: Response, next: NextFunction) {
        let message = (req as any).body
        let chatroom_id = (req as any).param.id
        res.json(await this.chatService.addMessage(chatroom_id, message.sender, message.message))
    }

    async deleteMessage(req: Request, res: Response, next: NextFunction) {
        const messageId = (req as any).param.message_id
        res.json(await this.chatService.deleteMessage(messageId))
    }

    async deleteChat(req: Request, res: Response, next: NextFunction) {
        const chatId = (req as any).param.id
        res.json(await this.chatService.deleteChat(chatId))
    }
}