<<<<<<< HEAD
const mongoose = require("mongoose")
=======
import mongoose from "mongoose"
>>>>>>> f5c574e6c63e3c52412b93402d87edf23c435f89

const Schema = mongoose.Schema;

const UserModelSchema = new Schema({
  _id: String,
<<<<<<< HEAD
  name: String,
=======
  postId: String,
>>>>>>> f5c574e6c63e3c52412b93402d87edf23c435f89
});

export const UserModel = mongoose.model("User", UserModelSchema);
