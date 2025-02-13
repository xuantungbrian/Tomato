import mongoose from 'mongoose';

const connectDB = async (): Promise<void> => {
    try {
        const dbURI = process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/' //Might need to specify the db required
        await mongoose.connect(dbURI)
        console.log("MongoDB Connected Successfully");
    } catch (error) {
        console.error("MongoDB connection error:", error);
        process.exit(1);
    }
};

export default connectDB;