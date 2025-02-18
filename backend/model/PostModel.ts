import mongoose from "mongoose"

const Schema = mongoose.Schema;

const PostModelSchema = new Schema({
  latitude: Number,
  longtitude: Number,
<<<<<<< HEAD
  fileData: Buffer, 
  fileType: String, // Normally, the better approach is move fileData and FileType in another table
  userId: String,
  date: Date,
  note: String,
});

export const PostModel = mongoose.model("Post", PostModelSchema); // Something about adding interfaces right here, might need to structure interfaces somehow
=======
  userId: String,
  note: String,
});

export const PostModel = mongoose.model("Post", PostModelSchema);
>>>>>>> f5c574e6c63e3c52412b93402d87edf23c435f89
