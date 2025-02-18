import mongoose from "mongoose"

const Schema = mongoose.Schema;

const PostModelSchema = new Schema({
  latitude: Number,
  longtitude: Number,
  userId: String,
  note: String,
});

export const PostModel = mongoose.model("Post", PostModelSchema);
