import { ChatService } from "../service/ChatService";
import { Request, Response } from "express"
import { isAuthenticatedRequest } from "../index";


export class ChatController {
    private chatService: ChatService;

    constructor() {
        this.chatService = new ChatService();
    }

    createChat = async (req: Request, res: Response) => {
        const { member_1, member_2 } = req.body;
        res.json(await this.chatService.createChat(member_1 as string, member_2 as string));
    }

    getChats = async (req: Request, res: Response): Promise<void> => {
        if (!isAuthenticatedRequest(req)) {
            res.status(401).json({ message: "Unauthorized" });
            return;
        }

        const userId = req.user.id; 
        res.json(await this.chatService.getChats(userId));
    }

    getChatMessages = async (req: Request, res: Response): Promise<void> => {
        if (!isAuthenticatedRequest(req)) {
            res.status(401).json({ message: "Unauthorized" });
            return;
        }
        const chatId = req.params.id
        res.json(await this.chatService.getChatMessages(chatId));
    }

    addMessage = async (req: Request, res: Response) => {
        let message = req.body
        let chatroom_id = req.params.id
        res.json(await this.chatService.addMessage(chatroom_id, message.sender as string, message.message as string))
    }

    deleteMessage = async (req: Request, res: Response) => {
        const messageId = req.params.message_id;
        res.json(await this.chatService.deleteMessage(messageId));
    }

    deleteChat = async (req: Request, res: Response) => {
        const chatId = req.params.id;
        res.json(await this.chatService.deleteChat(chatId));
    }
}