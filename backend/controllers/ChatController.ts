import { ChatService } from "../service/ChatService";
import { Request, Response, NextFunction } from "express"

export class ChatController {
    private chatService: ChatService;

    constructor() {
        this.chatService = new ChatService();
    }

    createChat = async (req: Request, res: Response, next: NextFunction) => {
        let chat = (req as any).body
        res.json(await this.chatService.createChat(chat.member_1, chat.member_2))
    }

    getChats = async (req: Request, res: Response, next: NextFunction) => {
        const userId = (req as any).user.id
        res.json(await this.chatService.getChats(userId))
    }

    getChatMessages = async (req: Request, res: Response, next: NextFunction) => {
        const chatId = (req as any).params.id
        res.json(await this.chatService.getChatMessages(chatId));
    }

    addMessage = async (req: Request, res: Response, next: NextFunction) => {
        let message = (req as any).body
        let chatroom_id = (req as any).params.id
        res.json(await this.chatService.addMessage(chatroom_id, message.sender, message.message))
    }

    deleteMessage = async (req: Request, res: Response, next: NextFunction) => {
        const messageId = (req as any).params.message_id
        res.json(await this.chatService.deleteMessage(messageId))
    }

    deleteChat = async (req: Request, res: Response, next: NextFunction) => {
        const chatId = (req as any).params.id
        res.json(await this.chatService.deleteChat(chatId))
    }
}