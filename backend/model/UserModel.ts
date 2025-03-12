import { Document, model, Schema } from 'mongoose';

/**
 * Interface for User object
 */
export interface IUser extends Document {
  _id: string;
  username: string;
  firebaseToken: string[]; // Array of strings (matches schema)
}

const UserModelSchema = new Schema<IUser>({
  _id: String,
  username: String,
  firebaseToken: String,
});
 
 
export const UserModel = model<IUser>("User", UserModelSchema);
