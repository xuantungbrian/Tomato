import mongoose from "mongoose";
import { PostModel } from "../model/PostModel";

interface Post {
    latitude: number,
    longtitude: number,
    fileData: Buffer, 
    fileType: string, 
    userId: string,
    date: Date,
    note: string,
}

export class PostService {
    async getPostById(postId: string) {
        return PostModel.findOne({ _id: new mongoose.Types.ObjectId(postId) })
    }

    async createPost(post: Post) {
        try {
            const newPost = new PostModel(post);
            await newPost.save();
            return newPost;
        } catch (error) {
            console.error("Error creating post:", error);
            throw new Error("Failed to create post");
        }
    }
    

    async updatePost(postId: string, post: Post)  {
        try {
            await PostModel.findByIdAndUpdate(new mongoose.Types.ObjectId(postId), post)
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