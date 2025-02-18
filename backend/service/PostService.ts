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
<<<<<<< HEAD
    async getPostById(postId: string) {
        return PostModel.findOne({ _id: new mongoose.Types.ObjectId(postId) })
=======
    async getPostById(id: string) {
        try {
            return PostModel.findById(id)
        } catch(error) {
            console.log("Error to get post from ID: ", error)
            return null
        }
>>>>>>> f5c574e6c63e3c52412b93402d87edf23c435f89
    }

    async createPost(post: Post) {
        try {
            const newPost = new PostModel(post);
            await newPost.save();
            return newPost;
        } catch (error) {
            console.error("Error creating post:", error);
<<<<<<< HEAD
            throw new Error("Failed to create post");
=======
            return null
>>>>>>> f5c574e6c63e3c52412b93402d87edf23c435f89
        }
    }
    

<<<<<<< HEAD
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
=======
    async updatePost(id: string, post: Post)  {
        try {
            await PostModel.findByIdAndUpdate(new mongoose.Types.ObjectId(id), post)
            return PostModel.findById(id)
        } catch (error) {
            console.error("Error updating post:", error);
            return null
        }
    }

    async deletePost(id: string) {
        try {  
            return PostModel.findOneAndDelete({ _id: new mongoose.Types.ObjectId(id) })
        } catch (error) {
            console.error("Error deleting post:", error);
            return null
>>>>>>> f5c574e6c63e3c52412b93402d87edf23c435f89
        }
    }

    async getPosts(userId: string) {
<<<<<<< HEAD
        return PostModel.find({ userId: new mongoose.Types.ObjectId(userId) })
=======
        try {
            return PostModel.find({ userId: new mongoose.Types.ObjectId(userId) })
        } catch(error) {
            console.log("Error getting all posts of this user", error)
            return null
        }
>>>>>>> f5c574e6c63e3c52412b93402d87edf23c435f89
    }
}