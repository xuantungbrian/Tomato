import mongoose from "mongoose"

const Schema = mongoose.Schema;

interface ImageData {
  fileData: Buffer;
  fileType: string;
}

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
  private: Boolean
});

export const PostModel = mongoose.model("Post", PostModelSchema);