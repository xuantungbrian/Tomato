import mongoose from "mongoose"

const Schema = mongoose.Schema;

const UserModelSchema = new Schema({
  _id: String,
  username: String,
  firebaseToken: String, //TODO: this might be an array for one user on multiple device
});

export const UserModel = mongoose.model("User", UserModelSchema);
