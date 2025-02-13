import mongoose from 'mongoose';
import { config } from 'dotenv';

config() // Let it here for now, need to find a way for this to not duplicates in index.ts

const connectDB = async (): Promise<void> => {
    try {
        const dbURI = process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/'
        await mongoose.connect(dbURI)
        console.log("MongoDB Connected Successfully");
    } catch (error) {
        console.error("MongoDB connection error:", error);
        process.exit(1);
    }
};

export default connectDB;