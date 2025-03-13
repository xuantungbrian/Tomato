import {Model, model, Schema} from "mongoose"

export interface Post {
  _id: string,
  latitude: number,
  longitude: number,
  images: ImageData[], 
  userId: string,
  date: Date,
  note: string,
  isPrivate: boolean,
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
  isPrivate: Boolean
});

export const PostModel: Model<Post> = model<Post>("Post", PostModelSchema);