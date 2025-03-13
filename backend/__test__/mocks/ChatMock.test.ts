import express, { RequestHandler } from 'express';
import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import morgan from 'morgan';
import request from 'supertest';
import { ChatController } from '../../controllers/ChatController';
import { ChatModel } from '../../model/ChatModel';
import { MessageModel } from '../../model/MessageModel';
import { verify } from 'jsonwebtoken';
import { config } from 'dotenv';

const {verifyToken} = require('../../middleware/verifyToken')
// Setup MongoDB in-memory server
let mongoServer = new MongoMemoryServer();
// Create the Express app
const app = express();
config();
app.use(express.json());  // Middleware to parse JSON bodies
app.use(morgan('tiny')); // Logger

jest.mock('jsonwebtoken', () => ({
  ...jest.requireActual('jsonwebtoken'), // import and retain the original functionalities
  verify: jest.fn().mockReturnValue({id: "user123"}), // overwrite verify
  sign: jest.fn().mockReturnValue("token")
  }));
const chatController = new ChatController();

//App routes
// createChat routes for testing
app.post('/chats', verifyToken, chatController.createChat); 

// getChatMessages routes for testing
app.get('/chats/:id', verifyToken, chatController.getChatMessages);  // Route for getting a post by ID

// getChats routes for testing
app.get('/chats', verifyToken, chatController.getChats);  

// addMessage routes for testing
app.post('/chat/:id', verifyToken, chatController.addMessage);

// deleteChat routes for testing
app.delete('/chats/:id', verifyToken, chatController.deleteChat);

// deleteMessage routes for testing
app.delete('/chat/:id/messages/:message_id',verifyToken, chatController.deleteMessage);

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
  await ChatModel.deleteMany({});
  await MessageModel.deleteMany({});
});

afterEach(async () => {
  jest.clearAllMocks();
})

describe('Mocked Chats API: Erroneus Behaviour', () => {
  beforeEach(() => {
    jest.restoreAllMocks();
    jest.clearAllMocks();
  });
  it('should fail to add a message if an error occurs', async () => {
    let spy = await jest.spyOn(MessageModel.prototype, "save").mockImplementation(() => {
      throw new Error("Database error 1")
    })
    const newChat = {
      member_1: "String",
      member_2: "user123"
    };
  
    const newMessage = {
      sender: "user123",
      message: "hi"
    }
  
    const chat = await request(app)
      .post('/chats') // Testing the protected /posts route
      .send(newChat) // Send the post body directly
      .set('Authorization', 'Bearer 90909090')
      .expect(200);
  
    const response = await request(app)
      .post(`/chat/${chat.body._id}`) // Testing the protected /posts route
      .send(newMessage) // Send the post body directly
      .set('Authorization', 'Bearer 90909090')
      .expect(200);

    expect(response.body).toBeNull();
    await spy.mockClear();
  });

  it("should fail to create a chat if error occurs", async () => {
    let spy = jest.spyOn(ChatModel, "findOne").mockImplementation(() => {
      throw new Error("Database error 2")
    })
    const newChat = {
      member_1: "new",
      member_2: "user123"
    };
  
    const response = await request(app)
      .post('/chats') // Testing the protected /posts route
      .send(newChat) // Send the post body directly
      .set('Authorization', 'Bearer 90909090')
      .expect(200);

    expect(response.body).toBeNull();
    spy.mockClear();
  })

  it('should fail to get chats if error occurs', async () => {
    let spy = await jest.spyOn(ChatModel, "find").mockImplementation(() => {
      throw new Error("Database error 3")
    })
    const newChat = {
      member_1: "String",
      member_2: "user123"
    };
  
    const newChat_2 = {
      member_1: "user123",
      member_2: "other"
    };
  
    const main_user = "user123"
    await request(app)
      .post('/chats') // Testing the protected /posts route
      .send(newChat) // Send the post body directly
      .set('Authorization', 'Bearer 90909090')
      .expect(200);
  
    await request(app)
      .post('/chats') // Testing the protected /posts route
      .send(newChat_2) // Send the post body directly
      .set('Authorization', 'Bearer 90909090')
      .expect(200);
  
    const response = await request(app)
      .get('/chats') // Testing the protected /posts route
      .set('Authorization', 'Bearer 90909090')
      .expect(200);

    expect(response.body).toBeNull()
    await spy.mockClear()
  });

  it('should fail to get messages if error occurs', async () => {
    let spy = await jest.spyOn(MessageModel, "find").mockImplementation(() => {
      throw new Error("Database error 4")
    })
    const newChat = {
      member_1: "user123",
      member_2: "new"
    };
  
    const chat = await request(app)
      .post('/chats') // Testing the protected /posts route
      .send(newChat) // Send the post body directly
      .set('Authorization', 'Bearer 90909090')
      .expect(200);
  
    const response = await request(app)
      .get(`/chats/${chat.body._id}`)
      .set('Authorization', 'Bearer 90909090')
      .expect(200)

    expect(response.body).toBeNull();
    await spy.mockClear()
  });

  it('should fail to delete chat if error occurs', async () => {
    let spy = await jest.spyOn(ChatModel, "deleteOne").mockImplementation(() => {
      throw new Error("Database error 5")
    })
    const newChat = {
      member_1: "user123",
      member_2: "new"
    };
  
    const chat = await request(app)
      .post('/chats') // Testing the protected /posts route
      .send(newChat) // Send the post body directly
      .set('Authorization', 'Bearer 90909090')
      .expect(200);
  
    const response = await request(app)
      .delete(`/chats/${chat.body._id}`) // Testing the protected /posts route
      .set('Authorization', 'Bearer 90909090')
      .expect(200);
      
    expect(response.body).toBeNull()
    await spy.mockClear()
  });
 
  it('should fail to delete message if error occurs', async () => {
    let spy = await jest.spyOn(MessageModel.prototype, "deleteOne").mockImplementation(() => {
      throw new Error("Database error 6")
    })

    const newChat = {
      member_1: "string",
      member_2: "user123"
    };
  
    const newMessage = {
      sender: "user123",
      message: "hi"
    }
  
    const chat = await request(app)
      .post('/chats') // Testing the protected /posts route
      .send(newChat) // Send the post body directly
      .set('Authorization', 'Bearer 90909090')
      .expect(200);
  
    const message = await request(app)
      .post(`/chat/${chat.body._id}`) // Testing the protected /posts route
      .send(newMessage) // Send the post body directly
      .set('Authorization', 'Bearer 90909090')
      .expect(200);
  
    const response = await request(app)
      .delete(`/chat/${chat.body._id}/messages/${message.body._id}`)
      .set('Authorization', 'Bearer 90909090')
      .expect(200)

    expect(response.body).toBeNull()
    await spy.mockClear()
  });
})
