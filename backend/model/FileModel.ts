import mongoose from "mongoose"

const Schema = mongoose.Schema;

const FileModelSchema = new Schema({
  _id: String,
  postId: String,
});

export const FileModel = mongoose.model("File", FileModelSchema);
