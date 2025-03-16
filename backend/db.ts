import mongoose from 'mongoose';

const connectDB = async (): Promise<void> => {
    try {
        const dbURI = process.env.DB_URI ?? 'mongodb://127.0.0.1:27017/'
        await mongoose.connect(dbURI)
        console.log("MongoDB Connected Successfully");
    } catch (error) {
        console.error("MongoDB connection error:", error);
    }
};

export default connectDB;