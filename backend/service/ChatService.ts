import { ChatModel } from "../model/ChatModel"
import { MessageModel } from "../model/MessageModel"

export class ChatService {
    async createChat( member_1: string, member_2: string ) {
        try {
            const existChat = await ChatModel.findOne().or([{ member_1, member_2 }, { member_1: member_2, member_2: member_1 }])
              
            if (existChat) {
                console.log("CHAT EXIST")
                return existChat
            }
            const newChat = new ChatModel({ member_1, member_2})
            await newChat.save()
            return newChat
        } catch(err) {
            console.error("Error creating chat: " + err)
            return null
        }
    }

    async getChatMessages(id: string) {
        try {
            const messages = await MessageModel.find({ chatroom_id: id })
            return messages
        } catch(err) {
            console.error("Error getting chat messages: " + err)
            return null
        }
    }

    // Get chat with chatId
    async getChat(id: string) {
        try {
            const chat = await ChatModel.findOne({ _id: id})
            return chat
        } catch(err) {
            console.error("Error getting chat: " + err)
            return null
        }
    }

    // Get chat with userId
    async getChats(id: string) {
        try {
            const chats = await ChatModel.find().or([{ member_1 : id}, {member_2: id}])
            return chats
        } catch(err) {
            console.error("Error getting chats: " + err)
            return null
        }
    }

    async deleteChat(id: string) {
        try {
            await MessageModel.deleteMany({ chatroom_id: id })
            const chat = await ChatModel.findById(id)
            await chat?.deleteOne()
            return chat
        } catch(err) {
            console.error("Error deleting chat: " + err)
            return null
        }
    }

    async addMessage(chatroom_id: string, sender: string, message: string) {
        try {    
            const newMessage = new MessageModel({ chatroom_id: chatroom_id, sender: sender, message: message })
            await newMessage.save()
            return newMessage
        } catch(err) {
            console.error("Error adding message: " + err)
            return null
        }
    }

    async deleteMessage(id: string) {
        try {
            const message = await MessageModel.findById(id)
            await message?.deleteOne()
            return message
        } catch(err) {
            console.error("Error deleting message: " + err)
            return null
        }
    }
}