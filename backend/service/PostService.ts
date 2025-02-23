import mongoose from "mongoose";
import { PostModel } from "../model/PostModel";

interface ImageData{
    fileData: Buffer,
    fileType: string,
}

interface Post {
    latitude: number,
    longitude: number,
    images: ImageData[], 
    userId: string,
    date: Date,
    note: string,
    private: boolean,
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

    async getPosts(
        userId: string, 
        start_lat?: number, 
        end_lat?: number, 
        start_long?: number, 
        end_long?: number, 
        isPrivate?: boolean
    ) {
        try {
            // Initialize the query with userId
            const query: any = {};
    
            // Check for start_lat and end_lat separately
            if (start_lat !== undefined) {
                query.latitude = { $gte: start_lat };  // Latitude greater than or equal to start_lat
            }
            if (end_lat !== undefined) {
                query.latitude = { ...query.latitude, $lte: end_lat };  // Latitude less than or equal to end_lat
            }
    
            // Check for start_long and end_long separately
            if (start_long !== undefined) {
                query.longitude = { $gte: start_long };  // Longitude greater than or equal to start_long
            }
            if (end_long !== undefined) {
                query.longitude = { ...query.longitude, $lte: end_long };  // Longitude less than or equal to end_long
            }
    
            // If `isPrivate` is provided, add it to the query
            if (isPrivate) {
                query.userId = userId; // Assuming 'private' is the field in your PostModel
            }
    
            // Return the posts based on the constructed 
            return PostModel.find(query);
        } catch (error) {
            console.log("Error getting posts", error);
            return null;
        }
    }    
}