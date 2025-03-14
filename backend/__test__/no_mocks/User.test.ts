import express, { Request, Response, NextFunction } from 'express';
import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import morgan from 'morgan';
import request from 'supertest';
import { UserController } from '../../controllers/UserController';
import { UserModel } from '../../model/UserModel';
import { UserService } from '../../service/UserService';

// Setup MongoDB in-memory server
let mongoServer = new MongoMemoryServer();
// Create the Express app
const app = express();
app.use(express.json());  // Middleware to parse JSON bodies
app.use(morgan('tiny')); // Logger

// Define your routes
const userController = new UserController();
const userService = new UserService();
app.post('/user/auth', async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
        await userController.handleGoogleSignIn(req, res);
    } catch (error) {
        next(error);
    }});  // Route for creating a post
app.get('/user/:id', async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
      await userController.getUser(req, res);
  } catch (error) {
      next(error);
  }}); // Route for getting a post by ID

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
  await UserModel.deleteMany({});
});

describe('Unmocked User APIs: Expected Behaviour', () => {
  it('should get a user from id', async () => {
    const newUser = {
      _id: "1234",
      username: "user123",
      firebaseToken: "user12345"
    };

    const user = await userService.createUser(newUser._id, newUser.username, newUser.firebaseToken)
    const response = await request(app)
        .get(`/user/${newUser._id}`)
        .expect(200)

    expect(response.body).toHaveProperty('_id'); // Check that the response contains _id
    expect(response.body._id).toBe(newUser._id); // Check userId matches
    expect(response.body.username).toBe(newUser.username); // Check userId matches
    expect(response.body.firebaseToken).toStrictEqual([newUser.firebaseToken]); // Check userId matches
  });
})

describe('Unmocked User APIs: Erroneus Behaviour', () => {
  it('tries to get a non-existant user', async () => {
    const newUser = {
      _id: "1234",
      username: "user123",
      firebaseToken: "user12345"
    };

    const user = await userService.createUser(newUser._id, newUser.username, newUser.firebaseToken)
    const response = await request(app)
        .get(`/user/4321`)
        .expect(200)

    expect(response.body).toBeNull()
  });

  it('should fail to sign in a user with improper credentials', async () => {
    const response = await request(app)
        .post(`/user/auth`)
        .send({
          irrelevant: "something useless"
        })
        .expect(400)

    expect(response.body.message).toBe("No Token provided")
  });
})