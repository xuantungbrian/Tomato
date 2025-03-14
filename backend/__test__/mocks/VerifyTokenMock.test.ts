import express, { NextFunction, Request, Response } from 'express';
import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import morgan from 'morgan';
import { PostController } from '../../controllers/PostController';
import request from 'supertest';
import { PostModel } from '../../model/PostModel';
import { config } from 'dotenv';
import { PostService } from '../../service/PostService';
import { AuthenticatedRequest } from '../..';

const {verifyToken} = require('../../middleware/verifyToken')
config();
jest.mock('jsonwebtoken', () => ({
  ...jest.requireActual('jsonwebtoken'), // import and retain the original functionalities
  verify: jest.fn().mockImplementation((token, secret, algorithm) =>
    {
      if (token == "90909090") {
        return {id: "user123"}
      }
      else {
        throw new Error("Verify token error")
      }
    }), // overwrite verify
  sign: jest.fn().mockReturnValue("token")
  }));
// Setup MongoDB in-memory server
let mongoServer = new MongoMemoryServer();
// Create the Express app
const app = express();
app.use(express.json());  // Middleware to parse JSON bodies
app.use(morgan('tiny')); // Logger

// Define your routes
const postController = new PostController();

// createPost routes for testing 
app.post('/posts', verifyToken, async(req: Request, res: Response, next: NextFunction): Promise<void> => {
  try{
    await postController.createPost(req as AuthenticatedRequest, res);
  } catch(err) {
    next(err);
  }}); 

// Setup for in-memory MongoDB testing
beforeAll(async () => {
  mongoServer = await MongoMemoryServer.create();
  const uri = mongoServer.getUri();
  await mongoose.connect(uri);
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

beforeEach(async () => {
  // Clean up any existing posts in the database before each test
  await PostModel.deleteMany({});
});

// Unit Tests using Supertest
/**
 * @jest-environment jsdom
 */
describe('Mocked Posts API: Erroneus Behaviour', () => {
  beforeEach(() => {
    jest.restoreAllMocks();
    jest.clearAllMocks();
  });

  it('should fail to verify token if there is no token provided', async () => {
    const newPost = {
      userId: 'user123',
      latitude: 40.7128,
      longitude: -74.0060,
      images: ["string", "image/jpeg"],
      date: new Date(),
      note: 'Test post',
      isPrivate: false,
    };
  
    const response = await request(app)
      .post('/posts') // Testing the protected /posts route
      .send(newPost) // Send the post body directly
      .expect(401);

    expect(response.body.message).toBe('No token provided')
  });

  it('should fail to verify token if jwt secret is not provided', async () => {
    const newPost = {
      userId: 'user123',
      latitude: 40.7128,
      longitude: -74.0060,
      images: ["string", "image/jpeg"],
      date: new Date(),
      note: 'Test post',
      isPrivate: false,
    };

    const old_process_env = process.env
    process.env = {}
    const response = await request(app)
      .post('/posts') // Testing the protected /posts route
      .send(newPost) // Send the post body directly
      .set('Authorization', 'Bearer 90909090')
      .expect(500);
  
    expect(response.body.message).toBe("Internal Server Error")
    process.env = old_process_env
  });

  it('should fail to verify token if an error occurs', async () => {
    const newPost = {
      userId: 'user123',
      latitude: 40.7128,
      longitude: -74.0060,
      images: ["string", "image/jpeg"],
      date: new Date(),
      note: 'Test post',
      isPrivate: false,
    };

    const response = await request(app)
      .post('/posts') // Testing the protected /posts route
      .send(newPost) // Send the post body directly
      .set('Authorization', 'Bearer 90909790')
      .expect(400);
  
    expect(response.body.message).toBe('Invalid token.')
  });
});
