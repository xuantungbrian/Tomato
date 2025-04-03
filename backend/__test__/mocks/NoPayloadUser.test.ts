import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import request from 'supertest';
import { UserModel } from '../../model/UserModel';

jest.mock('jsonwebtoken', () => ({
...jest.requireActual('jsonwebtoken'), 
verify: jest.fn().mockReturnValue({id: "user123"}), 
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

let mongoServer = new MongoMemoryServer();
const {app} = require('../app');

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