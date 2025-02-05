import {MongoClient} from 'mongodb';

const MONGO_URI = process.env.DB_URI as string ?? 'mongodb://127.0.0.1:27017'
export const client = new MongoClient(MONGO_URI);