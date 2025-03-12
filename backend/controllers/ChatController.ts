import { ChatService } from "../service/ChatService";
import { Request, Response } from "express"
import { AuthenticatedRequest } from "../index";


export class ChatController {
    private chatService: ChatService;

    constructor() {
        this.chatService = new ChatService();
    }

    createChat = async (req: Request<{ id: string }, {}, { member_1: string; member_2: string }>, res: Response) => {
        const { member_1, member_2 } = req.body;
        res.json(await this.chatService.createChat(member_1, member_2));
    }

    getChats = async (req: AuthenticatedRequest, res: Response) => {
        const userId = req.user.id; 
        res.json(await this.chatService.getChats(userId));
    }

    getChatMessages = async (req: Request<{ id: string }>, res: Response) => {
        const chatId = req.params.id
        res.json(await this.chatService.getChatMessages(chatId));
    }

    addMessage = async (req: Request<{ id: string }, {}, { sender: string; message: string }>, res: Response) => {
        let message = req.body
        let chatroom_id = req.params.id
        res.json(await this.chatService.addMessage(chatroom_id, message.sender, message.message))
    }

    deleteMessage = async (req: Request<{ message_id: string }>, res: Response) => {
        const messageId = req.params.message_id;
        res.json(await this.chatService.deleteMessage(messageId));
    }

    deleteChat = async (req: Request<{ id: string }>, res: Response) => {
        const chatId = req.params.id;
        res.json(await this.chatService.deleteChat(chatId));
    }
}