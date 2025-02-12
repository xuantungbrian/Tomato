const mongoose = require("mongoose")

const Schema = mongoose.Schema;

const PostModelSchema = new Schema({
  x_location: Number,
  y_location: Number,
  fileData: Buffer, 
  fileType: String, // Normally, the better approach is move fileData and FileType in another table
  userId: String,
  date: Date,
  note: String,
});

const PostModel = mongoose.model("PostModel", PostModelSchema);
