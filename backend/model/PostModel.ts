import mongoose from "mongoose"

const Schema = mongoose.Schema;

// TODO: Normally, the better approach is craeting one more table for files
const PostModelSchema = new Schema({
  latitude: Number,
  longtitude: Number,
  userId: String,
  note: String,
});

export const PostModel = mongoose.model("Post", PostModelSchema);