import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import request from 'supertest';
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
const {app} = require('../app');

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