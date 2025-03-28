import express, { Request, Response, NextFunction } from 'express';
import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import morgan from 'morgan';
import request from 'supertest';
import { UserController } from '../../controllers/UserController';
import { UserModel } from '../../model/UserModel';



jest.mock("google-auth-library", (): { OAuth2Client: jest.Mock } => {
  return {
    OAuth2Client: jest.fn().mockImplementation((): {
      verifyIdToken: jest.Mock<Promise<{
        getPayload: () => { email: string }
      }>, [unknown]>
    } => ({
      verifyIdToken: jest.fn().mockImplementation((): Promise<{
        getPayload: () => { email: string }
      }> => Promise.resolve({
        getPayload: (): { email: string } => ({ email: "email" })
      }))
    }))
  };
});

let mongoServer = new MongoMemoryServer();

const app = express();
app.use(express.json());  
app.use(morgan('tiny')); 

const userController = new UserController();
app.post('/user-faulty/auth', (req: Request, res: Response, next: NextFunction): void => {
    try {
        userController.handleGoogleSignIn(req, res)
        .then(() => { next(); })
        .catch((err: unknown) => { next(err); });
    } catch (error) {
        next(error);
    }});  

beforeAll(async () => {
  mongoServer = await MongoMemoryServer.create();
  const uri = mongoServer.getUri();
  await mongoose.connect(uri as string);
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

beforeEach(async () => {
  await UserModel.deleteMany({});
});

describe('Testing handleGoogleSignIn', () => {
  it('should fail to sign in to google with faulty payload', async () => {
    await request(app)
        .post(`/user-faulty/auth`)
        .send({
          googleToken: "google",
          firebaseToken: "firebase"
        })
        .expect(400)
  });
})