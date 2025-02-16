import { ChatModel } from "../model/ChatModel"
import { MessageModel } from "../model/MessageModel"

export class ChatService {
    async createChat(_id: string, member_1: string, member_2: string) {
        const newChat = new ChatModel({_id, member_1, member_2})
        await newChat.save()
    }

    async getChatMessages(_id: string) {
        const messages = await MessageModel.find({ chatroom_id: _id })
        return messages
    }

    async getChats(_id: string) {
        const chats = await ChatModel.find().or([{ member_1 : _id}, {member_2: _id}])
        return chats
    }

    async deleteChat(id: string) {
        await MessageModel.deleteMany({ chatroom_id: id })
        const chat = await ChatModel.findById(id)
        await chat?.deleteOne()
    }

    async addMessage(id: string, chatroom_id: string, sender: string, message: string) {
        const newMessage = new MessageModel({_id: id, chatroom_id: chatroom_id, sender: sender, message: message})
        await newMessage.save()
    }

    async deleteMessage(id: string) {
        const message = await MessageModel.findById(id)
        await message?.deleteOne()
    }
}