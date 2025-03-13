import { ChatModel } from "../model/ChatModel";
import { MessageModel } from "../model/MessageModel";
import { ChatService } from "../service/ChatService";
import { Request, Response } from "express"
import { AuthenticatedRequest } from "../index";


export class ChatController {
    private chatService: ChatService;

    constructor() {
        this.chatService = new ChatService();
    }

    createChat = async (req: Request, res: Response, next: NextFunction) => {
        let chat = req.body
        let user = req.user?.id
        if (!user) {
            res.status(401).send({message: "Unauthorized"});
            return;
        }
        if (!chat.member_1 || !chat.member_2) {
            res.status(400).send({message: "Bad request"});
            return;
        }
        if (chat.member_1 != user && chat.member_2 != user) {
            res.status(401).send({message: "Unauthorized"});
            return;
        }
        res.json(await this.chatService.createChat(chat.member_1, chat.member_2))
    }

    getChats = async (req: Request, res: Response, next: NextFunction) => {
        const userId = (req as any).user?.id
        if (!userId) {
            res.status(401).send({ message: "Unauthorized" });
            return;
        }
        res.json(await this.chatService.getChats(userId))
    }

    getChatMessages = async (req: Request, res: Response, next: NextFunction) => {
        const chatId = (req as any).params.id
        let user = (req as any).user?.id
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

    addMessage = async (req: Request, res: Response, next: NextFunction) => {
        let message = (req as any).body
        let user = (req as any).user?.id
        if (!user) {
            res.status(401).send({ message: "Unauthorized" });
            return;
        }
        if (!message.sender || !message.message) {
            res.status(400).send({message: "Bad Request"})
            return;
        }
        let chatroom_id = (req as ).params.id
        if (message.sender !== user) {
            res.status(401).send({message: "Unauthorized"})
        } else {
            res.json(await this.chatService.addMessage(chatroom_id, message.sender, message.message))
        }
    }

    deleteMessage = async (req: Request, res: Response, next: NextFunction) => {
        const messageId = (req as any).params.message_id
        let user = (req as any).user?.id
        if (!user) {
            res.status(401).send({ message: "Unauthorized" });
            return;
        }
        const message = await MessageModel.findById(messageId)
        if (!message) {
            res.status(404).send({message: "Message not found"});
            return;
        }
        if (message.sender != user) {
            res.status(401).send({message: "Unauthorized"})
            return;
        }
        res.json(await this.chatService.deleteMessage(messageId))
    }

    deleteChat = async (req: Request, res: Response, next: NextFunction) => {
        const chatId = (req as any).params.id
        let user = (req as any).user?.id
        if (!user) {
            res.status(401).send({ message: "Unauthorized" });
            return;
        }
        const chat = await ChatModel.findById(chatId)
        if (!chat) {
            res.status(404).send({message: "Chat not found"});
            return;
        }
        if (chat.member_1 != user && chat.member_2 != user) {
            res.status(401).send({message: "Unauthorized"})
            return;
        }
        res.json(await this.chatService.deleteChat(chatId))
    }
}