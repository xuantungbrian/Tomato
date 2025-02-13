import mongoose from "mongoose"

const Schema = mongoose.Schema;

const PostModelSchema = new Schema({
  latitude: Number,
  longtitude: Number,
  fileData: Buffer, 
  fileType: String, // Normally, the better approach is move fileData and FileType in another table
  userId: String,
  date: Date,
  note: String,
});

export const PostModel = mongoose.model("PostModel", PostModelSchema);