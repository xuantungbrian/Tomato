import mongoose from "mongoose"

const Schema = mongoose.Schema;


const PostModelSchema = new Schema({
  latitude: Number,
  longitude: Number,
  userId: String,
  images: [{
    fileData: Buffer,
    fileType: String
  }],
  date: Date,
  note: String,
  isPrivate: Boolean
});

export const PostModel = mongoose.model("Post", PostModelSchema);
