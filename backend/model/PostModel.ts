const mongoose = require("mongoose")

const Schema = mongoose.Schema;

const PostModelSchema = new Schema({
  location: String,
  fileData: Buffer, 
  fileType: String, // Normally, the better approach is move fileData and FileType in another table
  userId: String
});

const PostModel = mongoose.model("PostModel", PostModelSchema);
