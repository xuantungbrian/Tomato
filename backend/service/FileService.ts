import { GridFSBucket } from "mongodb";
import { FileModel } from "../model/FileModel"
import mongoose from "mongoose";

export class FileService {
    private bucket: any = null
    // TODO: Init this bucket better

    async createFile(id: string, postId: string) {
        try {
            if (!this.bucket) {
                this.bucket = new mongoose.mongo.GridFSBucket(mongoose.connection.db as any, {
                    bucketName: process.env.MONGODB_UPLOAD_BUCKET
                });
            }
            const newFile = new FileModel({ _id: id, postId })
            return newFile.save()
        } catch(error) {
            console.error("Error creating file:", error);
            return null
        }
    }

    async getFile(id: string) {
        try {
            if (!this.bucket) {
                this.bucket = new mongoose.mongo.GridFSBucket(mongoose.connection.db as any, {
                    bucketName: process.env.MONGODB_UPLOAD_BUCKET
                });
            }
            return this.bucket.openDownloadStream(new mongoose.Types.ObjectId(id));
        } catch(error) {
            console.error("Error getting file:", error);
            return null
        }
    }

    async getFileInPost(postId: string) {
        try {
            if (!this.bucket) {
                this.bucket = new mongoose.mongo.GridFSBucket(mongoose.connection.db as any, {
                    bucketName: process.env.MONGODB_UPLOAD_BUCKET
                });
            }
            const postObjectId = new mongoose.Types.ObjectId(postId)
            return FileModel.find({ postId: postObjectId })
        } catch(error) {
            console.error("Error getting file ids in post:", error);
            return null
        }
    }
}