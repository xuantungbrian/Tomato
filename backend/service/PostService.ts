import mongoose from "mongoose";
import { PostModel } from "../model/PostModel";

export class PostService {
    async getPostById(postId: string) {
        return PostModel.findOne({ _id: new mongoose.Types.ObjectId(postId) })
    }

    async createPost(latitude: number, longitude: number, fileData: Buffer, fileType: string, userId: string, note?: string) {
        try {
            const newPost = new PostModel({
                latitude,
                longitude,
                fileData,
                fileType,
                userId,
                date: new Date(),
                note: note || "",
            });
    
            await newPost.save();
            return newPost;
        } catch (error) {
            console.error("Error creating post:", error);
            throw new Error("Failed to create post");
        }
    }
    

    async updatePost(latitude: number, longitude: number, fileData: Buffer, fileType: string, userId: string, postId: string, note?: string)  {
        try {
            const updatePost = new PostModel({
                latitude,
                longitude,
                fileData,
                fileType,
                userId: new mongoose.Types.ObjectId(userId),
                date: new Date(),
                note: note || "",
            });
    
            await PostModel.findByIdAndUpdate(new mongoose.Types.ObjectId(postId), updatePost)
        } catch (error) {
            console.error("Error updating post:", error);
            throw new Error("Failed to update post");
        }
    }

    async deletePost(postId: string) {
        try {  
            await PostModel.deleteOne({ _id: new mongoose.Types.ObjectId(postId) })
        } catch (error) {
            console.error("Error deleting post:", error);
            throw new Error("Failed to delete post");
        }
    }

    async getPosts(userId: string) {
        return PostModel.find({ userId: new mongoose.Types.ObjectId(userId) })
    }
}