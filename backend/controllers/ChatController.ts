import { ChatModel } from "../model/ChatModel";
import { MessageModel } from "../model/MessageModel";
import { ChatService } from "../service/ChatService";
import { Request, Response } from "express"
import { isAuthenticatedRequest } from "../types/AuthenticatedRequest";

export class ChatController {
    private chatService: ChatService;

    constructor() {
        this.chatService = new ChatService();
    }
    
    /**
     * Given two user ids in the request body, create a new chat.
     */
    createChat = async (req: Request, res: Response) => {
        if (!isAuthenticatedRequest(req)) {
            res.status(401).json({ message: "Unauthorized" });
            return;
        } 
        let chat = req.body
        let user = req.user.id
        if (!chat.member_1 || !chat.member_2) {
            res.status(400).send({message: "Bad request"});
            return;
        }
        if (chat.member_1 != user && chat.member_2 != user) {
            res.status(401).send({message: "Unauthorized"});
            return;
        }
        res.json(await this.chatService.createChat(chat.member_1 as string, chat.member_2 as string))
    }

    /**
     * Get a list of chats involving an authenticated user.
     */
    getChats = async (req: Request, res: Response): Promise<void> => {
        if (!isAuthenticatedRequest(req)) {
            res.status(401).json({ message: "Unauthorized" });
            return;
        }

        const userId = req.user.id; 
        res.json(await this.chatService.getChats(userId));
    }

    /**
     * Get a list of messages in a chat.
     */
    getChatMessages = async (req: Request, res: Response): Promise<void> => {
        if (!isAuthenticatedRequest(req)) {res.status(401).json({ message: "Unauthorized" });
            return;
        }

        if(!req.params.id || req.params.id.trim() === ""){
            res.status(400).json({ error: 'ID is required' });
            return
        }
        
        const chatId = req.params.id
        let user = req.user.id
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

    /**
     * Add a message to a chat whose id is provided in the request params.
     */
    addMessage = async (req: Request, res: Response) => {
        if (!isAuthenticatedRequest(req)) {
            res.status(401).json({ message: "Unauthorized" });
            return;
        }
        let message = req.body
        let user = req.user.id
        if(!req.params.id || req.params.id.trim() === ""){
            res.status(400).json({ error: 'ID is required' });
            return
        }
        if (!message.sender || !message.message) {
            res.status(400).send({message: "Bad Request"})
            return;
        }
        if (!user || message.sender !== user) {
            res.status(401).send({message: "Unauthorized"})
            return;
        }

        let chatroom_id = req.params.id
        res.json(await this.chatService.addMessage(chatroom_id, message.sender as string, message.message as string))
    }

    
    /**
     * Delete a message from a chat whose id is provided in the request params.
     */
    deleteMessage = async (req: Request, res: Response) => {
        if (!isAuthenticatedRequest(req)) {
            res.status(401).json({ message: "Unauthorized" });
            return
        }
        if(!req.params.message_id || req.params.id.trim() === ""){
            res.status(400).json({ error: 'ID is required' });
            return
        }
        const messageId = req.params.message_id
        let user = req.user.id
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

    /**
     * Delete a chatroom whose id is provided in the request params.
     */
    deleteChat = async (req: Request, res: Response) => {
        if (!isAuthenticatedRequest(req)) {
            res.status(401).json({ message: "Unauthorized" });
            return
        }
        if(!req.params.id || req.params.id.trim() === ""){
            res.status(400).json({ error: 'ID is required' });
            return
        }
        const chatId = req.params.id
        let user = req.user.id
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