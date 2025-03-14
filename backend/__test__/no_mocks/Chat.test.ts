import express, {Request, Response, NextFunction} from 'express';
import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import morgan from 'morgan';
import request from 'supertest';
import { ChatController } from '../../controllers/ChatController';
import { ChatModel } from '../../model/ChatModel';
import { MessageModel } from '../../model/MessageModel';
import { AuthenticatedRequest } from '../..';
import { ChatService } from '../../service/ChatService';

// Setup MongoDB in-memory server
let mongoServer = new MongoMemoryServer();
// Create the Express app
const app = express();
app.use(express.json());  // Middleware to parse JSON bodies
app.use(morgan('tiny')); // Logger


const chatController = new ChatController();
const chatService = new ChatService();

//App routes

// createChat routes for testing
app.post('/chats', (req, res, next) => {
  (req as any).user = { id: 'user123' };
  next();
}, async(req: Request, res: Response, next: NextFunction): Promise<void> => {
  try{
    await chatController.createChat(req as AuthenticatedRequest, res);
  } catch(err) {
    next(err);
  }}); 
app.post('/chats-string', (req, res, next) => {
  (req as any).user = { id: 'string' }; 
  next();
}, async(req: Request, res: Response, next: NextFunction): Promise<void> => {
  try{
    await chatController.createChat(req as AuthenticatedRequest, res);
  } catch(err) {
    next(err);
  }}); 
app.post('/chats-no-middleware', async(req: Request, res: Response, next: NextFunction): Promise<void> => {
  try{
    await chatController.createChat(req as AuthenticatedRequest, res);
  } catch(err) {
    next(err);
  }});
// getChatMessages routes for testing
app.get('/chats/:id', (req, res, next) => {
    (req as any).user = { id: 'user123' }; // Mock the authenticated user
    next();
  }, async(req: Request, res: Response, next: NextFunction): Promise<void> => {
    try{
      await chatController.getChatMessages(req as AuthenticatedRequest, res);
    } catch(err) {
      next(err);
    }});  // Route for getting a post by ID
app.get('/chats-no-middleware/:id', async(req: Request, res: Response, next: NextFunction): Promise<void> => {
  try{
    await chatController.getChatMessages(req as AuthenticatedRequest, res);
  } catch(err) {
    next(err);
  }});  // Route for getting a post by ID

// getChats routes for testing
app.get('/chats', (req, res, next) => {
    (req as any).user = { id: 'user123' }; // Mock the authenticated user
    next();
  },async(req: Request, res: Response, next: NextFunction): Promise<void> => {
    try{
      await chatController.getChats(req as AuthenticatedRequest, res);
    } catch(err) {
      next(err);
    }});  
app.get('/chats-unauthorized', async(req: Request, res: Response, next: NextFunction): Promise<void> => {
  try{
    await chatController.getChats(req as AuthenticatedRequest, res);
  } catch(err) {
    next(err);
  }});  

// addMessage routes for testing
app.post('/chat/:id', (req, res, next) => {
    (req as any).user = { id: 'user123' }; // Mock the authenticated user
    next();
  },async(req: Request, res: Response, next: NextFunction): Promise<void> => {
    try{
      await chatController.addMessage(req as AuthenticatedRequest, res);
    } catch(err) {
      next(err);
    }});
app.post('/chat-string/:id', (req, res, next) => {
    (req as any).user = { id: 'string' }; // Mock the authenticated user
    next();
  }, async(req: Request, res: Response, next: NextFunction): Promise<void> => {
    try{
      await chatController.addMessage(req as AuthenticatedRequest, res);
    } catch(err) {
      next(err);
    }});
app.post('/chat-no-middleware/:id', async(req: Request, res: Response, next: NextFunction): Promise<void> => {
  try{
    await chatController.addMessage(req as AuthenticatedRequest, res);
  } catch(err) {
    next(err);
  }});

// deleteChat routes for testing
app.delete('/chats/:id', (req, res, next) => {
    (req as any).user = { id: 'user123' }; // Mock the authenticated user
    next();
  }, async(req: Request, res: Response, next: NextFunction): Promise<void> => {
    try{
      await chatController.deleteChat(req as AuthenticatedRequest, res);
    } catch(err) {
      next(err);
    }});
app.delete('/chats-no-middleware/:id', async(req: Request, res: Response, next: NextFunction): Promise<void> => {
  try{
    await chatController.deleteChat(req as AuthenticatedRequest, res);
  } catch(err) {
    next(err);
  }});

// deleteMessage routes for testing
app.delete('/chat/:id/messages/:message_id', (req, res, next) => {
    (req as any).user = { id: 'user123' }; // Mock the authenticated user
    next();
  }, async(req: Request, res: Response, next: NextFunction): Promise<void> => {
    try{
      await chatController.deleteMessage(req as AuthenticatedRequest, res);
    } catch(err) {
      next(err);
    }});
app.delete('/chat-no-middleware/:id/messages/:message_id', async(req: Request, res: Response, next: NextFunction): Promise<void> => {
  try{
    await chatController.deleteMessage(req as AuthenticatedRequest, res);
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
  await ChatModel.deleteMany({});
  await MessageModel.deleteMany({});
});

describe('Unmocked Chats API: Expected Behaviour', () => {
  describe("Testing createChat", () => {
    it('should create a chat', async () => {
      const newChat = {
        member_1: "String",
        member_2: "user123"
      };
  
      const main_user = "user123"
      const other_user = "String"
      const response = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      expect(response.body).toHaveProperty('_id'); // Check that the response contains _id
      expect([response.body.member_1, response.body.member_2]).toContain(main_user); // Check userId matches
      expect([response.body.member_1, response.body.member_2]).toContain(other_user); // Check userId matches
    });
  })

  describe("Testing getChats and getChat", () => {
    it('should get chats', async () => {
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
        .expect(200);
  
      await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat_2) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .get('/chats') // Testing the protected /posts route
        .expect(200);
      expect(response.body[0]).toHaveProperty('_id'); // Check that the response contains _id
      expect([response.body[0].member_1, response.body[0].member_2]).toContain(main_user); // Check userId matches
      expect([response.body[1].member_1, response.body[1].member_2]).toContain(main_user);
    });

    it('should get chat by id', async () => {
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
        .expect(200);
  
      let chat = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat_2) // Send the post body directly
        .expect(200);
  
      const response = await chatService.getChat(chat.body._id);
      expect(response).toHaveProperty('_id'); // Check that the response contains _id
      expect([response?.member_1, response?.member_2]).toContain(main_user); // Check userId matches
      expect([response?.member_1, response?.member_2]).toContain(main_user);
    });
  })

  describe("Testing addMessage", () => {
    it('should add a message', async () => {
      const newChat = {
        member_1: "String",
        member_2: "user123"
      };
  
      const newMessage = {
        sender: "user123",
        message: "hi"
      }
  
      const main_user = "user123"
      const chat = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .post(`/chat/${chat.body._id}`) // Testing the protected /posts route
        .send(newMessage) // Send the post body directly
        .expect(200);
  
      expect(response.body).toHaveProperty('_id'); // Check that the response contains _id
      expect(response.body.sender).toBe(main_user); // Check userId matches
      expect(response.body.message).toBe("hi"); // Check userId matches
      expect(response.body.chatroom_id).toBe(chat.body._id); // Check userId matches
    });
  })
  
  describe('Testing deleteMessage', () => {
    it('should delete a message', async () => {
      const newChat = {
        member_1: "String",
        member_2: "user123"
      };
  
      const newMessage = {
        sender: "user123",
        message: "hi"
      }
  
      const main_user = "user123"
      const chat = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const message = await request(app)
        .post(`/chat/${chat.body._id}`) // Testing the protected /posts route
        .send(newMessage) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .delete(`/chat/${chat.body._id}/messages/${message.body._id}`)
        .expect(200)
  
      const deleted = await MessageModel.findById(message.body._id)
  
      expect(response.body).toHaveProperty('_id'); // Check that the response contains _id
      expect(response.body.sender).toBe(main_user); // Check userId matches
      expect(response.body.message).toBe("hi"); // Check userId matches
      expect(response.body.chatroom_id).toBe(chat.body._id); // Check userId matches
      expect(deleted).toBeNull()
    });
  })

  describe("Testing getChatMessages", () => {
    it('should get chat messages', async () => {
      const newChat = {
        member_1: "String",
        member_2: "user123"
      };
  
      const newMessage = {
        sender: "user123",
        message: "hi"
      }
  
      const newMessage_2 = {
        sender: "user123",
        message: "whats up"
      }
  
      const main_user = "user123"
      const chat = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const message1 = await request(app)
        .post(`/chat/${chat.body._id}`) // Testing the protected /posts route
        .send(newMessage) // Send the post body directly
        .expect(200);
  
      const message2 = await request(app)
        .post(`/chat/${chat.body._id}`) // Testing the protected /posts route
        .send(newMessage_2) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .get(`/chats/${chat.body._id}`)
        .expect(200)
  
      expect(response.body[0]).toHaveProperty('_id'); // Check that the response contains _id
      expect(response.body[0].sender).toBe(main_user); // Check userId matches
      expect(response.body[0].message).toBe("hi"); // Check userId matches
      expect(response.body[0].chatroom_id).toBe(chat.body._id); // Check userId matches
      expect(response.body[1].sender).toBe(main_user); // Check userId matches
      expect(response.body[1].message).toBe("whats up"); // Check userId matches
      expect(response.body[1].chatroom_id).toBe(chat.body._id); // Check userId matches
    });
  })

  describe("Testing deleteChats", () => {
    it('should delete chat', async () => {
      const newChat = {
        member_1: "String",
        member_2: "user123"
      };
  
      const main_user = "user123"
      const chat = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .delete(`/chats/${chat.body._id}`) // Testing the protected /posts route
        .expect(200);
  
      const deleted = await ChatModel.findById(response.body._id)
      expect(response.body).toHaveProperty('_id'); // Check that the response contains _id
      expect([response.body.member_1, response.body.member_2]).toContain(main_user); // Check userId matches
      expect(deleted).toBeNull();
    });
  })
});

describe('Unmocked Chats API: Erroneus Behaviour', () => {
  describe("Testing addMessage", () => {
    it('should fail to add a message from a non-sender user', async () => {
      const newChat = {
        member_1: "String",
        member_2: "user123"
      };
  
      const newMessage = {
        sender: "String",
        message: "hi"
      }
  
      const main_user = "user123"
      const chat = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .post(`/chat/${chat.body._id}`) // Testing the protected /posts route
        .send(newMessage) // Send the post body directly
        .expect(401);
      
    });

    it('should fail to add a message from unauthorized user', async () => {
      const newChat = {
        member_1: "String",
        member_2: "user123"
      };
  
      const newMessage = {
        sender: "String",
        message: "hi"
      }
  
      const main_user = "user123"
      const chat = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .post(`/chat-no-middleware/${chat.body._id}`) // Testing the protected /posts route
        .send(newMessage) // Send the post body directly
        .expect(401);
    });

    it('should fail to add an invalid message', async () => {
      const newChat = {
        member_1: "String",
        member_2: "user123"
      };
  
      const newMessage = {}
  
      const main_user = "user123"
      const chat = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .post(`/chat/${chat.body._id}`) // Testing the protected /posts route
        .send(newMessage) // Send the post body directly
        .expect(400);
    });
  })

  describe("Testing createChat", () => {
    it("should fail to create a chat without the user in it", async () => {
      const newChat = {
        member_1: "new",
        member_2: "user"
      };
  
      const response = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(401);
    })

    it("should fail to create a chat without an authorized user", async () => {
      const newChat = {
        member_1: "new",
        member_2: "user123"
      };
  
      const response = await request(app)
        .post('/chats-no-middleware') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(401);
    })

    it("should return an already existant chat", async () => {
      const newChat = {
        member_1: "String",
        member_2: "user123"
      };
  
      const main_user = "user123"
      const other_user = "String"
      const first = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);

      const response = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      expect(response.body).toHaveProperty('_id'); // Check that the response contains _id
      expect([response.body.member_1, response.body.member_2]).toContain(main_user); // Check userId matches
      expect([response.body.member_1, response.body.member_2]).toContain(other_user); // Check userId matches
    })

    it("should fail to create a chat without all the members", async () => {
      const newChat = {
        member_1: "user123",
      };
  
      const response = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(400);
    })
  })

  describe("Testing getChats", () => {
    it('should fail to get chats without a valid user', async () => {
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
        .expect(200);
  
      await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat_2) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .get('/chats-unauthorized') // Testing the protected /posts route
        .expect(401);
    });
  })

  describe("Testing getChatMessages", () => {
    it('should fail to get messages of unauthorized chat', async () => {
      const newChat = {
        member_1: "string",
        member_2: "new"
      };
  
      const chat = await request(app)
        .post('/chats-string') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .get(`/chats/${chat.body._id}`)
        .expect(401)
    });

    it('should fail to get messages as an unauthorized user', async () => {
      const newChat = {
        member_1: "string",
        member_2: "new"
      };
  
      const chat = await request(app)
        .post('/chats-string') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .get(`/chats-no-middleware/${chat.body._id}`)
        .expect(401)
    });

    it('should fail to get messages of non-existant chat', async () => {
      const newChat = {
        member_1: "user123",
        member_2: "new"
      };
  
      const chat = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);

      const newId = new mongoose.Types.ObjectId(0)
  
      const response = await request(app)
        .get(`/chats/${newId}`)
        .expect(404)
    });
  })

  describe("Testing deleteChat", () => {
    it('should fail to delete non-existant chat', async () => {
      const newId = new mongoose.Types.ObjectId(0)
      const response = await request(app)
        .delete(`/chats/${newId}`) // Testing the protected /posts route
        .expect(404);
    });

    it('should fail to delete chat as an unauthorized user', async () => {
      const newChat = {
        member_1: "string",
        member_2: "new"
      };
  
      const chat = await request(app)
        .post('/chats-string') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .delete(`/chats-no-middleware/${chat.body._id}`) // Testing the protected /posts route
        .expect(401);
    });

    it('should fail to delete chat without proper authorization', async () => {
      const newChat = {
        member_1: "string",
        member_2: "new"
      };
  
      const chat = await request(app)
        .post('/chats-string') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .delete(`/chats/${chat.body._id}`) // Testing the protected /posts route
        .expect(401);
    });
  })

  describe("Testing deleteMessages", () => {
    it('should fail to delete non-existant message', async () => {
      const newId = new mongoose.Types.ObjectId(0)
      const response = await request(app)
        .delete(`/chat/${newId}/messages/${newId}`) // Testing the protected /posts route
        .expect(404);
    });

    it('should fail to delete message without proper authorization', async () => {
      const newChat = {
        member_1: "string",
        member_2: "user123"
      };
  
      const newMessage = {
        sender: "string",
        message: "hi"
      }
  
      const chat = await request(app)
        .post('/chats') // Testing the protected /posts route
        .send(newChat) // Send the post body directly
        .expect(200);
  
      const message = await request(app)
        .post(`/chat-string/${chat.body._id}`) // Testing the protected /posts route
        .send(newMessage) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .delete(`/chat/${chat.body._id}/messages/${message.body._id}`)
        .expect(401)
    });

    it('should fail to delete message as an unauthorized user', async () => {
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
        .expect(200);
  
      const message = await request(app)
        .post(`/chat/${chat.body._id}`) // Testing the protected /posts route
        .send(newMessage) // Send the post body directly
        .expect(200);
  
      const response = await request(app)
        .delete(`/chat-no-middleware/${chat.body._id}/messages/${message.body._id}`)
        .expect(401)
    });
  })
})
