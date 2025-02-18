import mongoose from "mongoose"

const Schema = mongoose.Schema;

const MessageModelSchema = new Schema({
    chatroom_id: String,
    sender: String,
    message: String
});

export const MessageModel = mongoose.model("Message", MessageModelSchema);