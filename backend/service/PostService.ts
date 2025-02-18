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
    async getPostById(id: string) {
        try {
            return PostModel.findById(id)
        } catch(error) {
            console.log("Error to get post from ID: ", error)
            return null
        }
    }

    async createPost(post: Post) {
        try {
            const newPost = new PostModel(post);
            await newPost.save();
            return newPost;
        } catch (error) {
            console.error("Error creating post:", error);
            return null
        }
    }
    

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
        }
    }

    async getPosts(userId: string) {
        try {
            return PostModel.find({ userId: new mongoose.Types.ObjectId(userId) })
        } catch(error) {
            console.log("Error getting all posts of this user", error)
            return null
        }
    }
}