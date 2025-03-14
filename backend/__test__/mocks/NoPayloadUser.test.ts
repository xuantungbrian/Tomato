import express, { Request, Response, NextFunction } from 'express';
import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import morgan from 'morgan';
import request from 'supertest';
import { UserController } from '../../controllers/UserController';
import { UserModel } from '../../model/UserModel';
import jwt from 'jsonwebtoken';
import verifyToken from '../../middleware/verifyToken';

jest.mock('jsonwebtoken', () => ({
...jest.requireActual('jsonwebtoken'), // import and retain the original functionalities
verify: jest.fn().mockReturnValue({id: "user123"}), // overwrite verify
sign: jest.fn().mockReturnValue("token")
}));
jest.mock("google-auth-library", () => {
  return {
      OAuth2Client: jest.fn().mockImplementation(() => ({
          verifyIdToken: jest.fn().mockResolvedValue({
              getPayload: () => ({
                email: "email"
              })
          })
      }))
  };
});

// Setup MongoDB in-memory server
let mongoServer = new MongoMemoryServer();
// Create the Express app
const app = express();
app.use(express.json());  // Middleware to parse JSON bodies
app.use(morgan('tiny')); // Logger

// Define your routes
const userController = new UserController();
app.post('/user-faulty/auth', async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
        await userController.handleGoogleSignIn(req, res);
    } catch (error) {
        next(error);
    }});  // Route for creating a post

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

describe('Mocked User APIs: Erroneus Behaviour', () => {
  it('should fail to sign in to google with faulty payload', async () => {
    const response = await request(app)
        .post(`/user-faulty/auth`)
        .send({
          googleToken: "google",
          firebaseToken: "firebase"
        })
        .expect(400)
  });
})