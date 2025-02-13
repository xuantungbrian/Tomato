const mongoose = require("mongoose")

const Schema = mongoose.Schema;

const UserModelSchema = new Schema({
  _id: String,
  name: String,
});

export const UserModel = mongoose.model("User", UserModelSchema);
