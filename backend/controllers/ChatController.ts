import { ChatModel } from "../model/ChatModel";
import { MessageModel } from "../model/MessageModel";
import { ChatService } from "../service/ChatService";
import { Request, Response } from "express"
import { isAuthenticatedRequest } from "../index";


export class ChatController {
    private chatService: ChatService;

    constructor() {
        this.chatService = new ChatService();
    }

    createChat = async (req: Request, res: Response) => {
        const { member_1, member_2 } = req.body as { member_1: string, member_2: string };
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
        let user = req.user?.id
        if (!user) {
            res.status(401).send({ message: "Unauthorized" });
            return;
        }
        const chat = await ChatModel.findById(chatId);
        if (!chat) {
            res.status(404).send({ message: "Chat not found" });
            return;
        }
        if (chat.member_1 != user && chat.member_2 != user) {
            res.status(401).send({message: "Unauthorized"});
            return;
        }
        res.json(await this.chatService.getChatMessages(chatId));
    }

    addMessage = async (req: Request, res: Response) => {
        let message = req.body
        let user = req.user?.id
        if (!user) {
            res.status(401).send({ message: "Unauthorized" });
            return;
        }
        if (!message.sender || !message.message) {
            res.status(400).send({message: "Bad Request"})
            return;
        }
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