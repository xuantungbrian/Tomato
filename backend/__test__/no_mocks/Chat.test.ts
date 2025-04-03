import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import request from 'supertest';
import { ChatModel } from '../../model/ChatModel';
import { MessageModel } from '../../model/MessageModel';
import { ChatService } from '../../service/ChatService';
import 'dotenv/config';

let mongoServer = new MongoMemoryServer();
const {app} = require('../app');
const chatService = new ChatService();

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
  await ChatModel.deleteMany({});
  await MessageModel.deleteMany({});
});
describe("Testing createChat", () => {
  it('should create a chat', async () => {
    const newChat = {
      member_1: "String",
      member_2: "user123"
    };

    const main_user = "user123"
    const other_user = "String"
    const response = await request(app)
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    expect(response.body).toHaveProperty('_id'); 
    expect([response.body.member_1, response.body.member_2]).toContain(main_user); 
    expect([response.body.member_1, response.body.member_2]).toContain(other_user); 
  });

  it("should fail to create a chat without the user in it", async () => {
    const newChat = {
      member_1: "new",
      member_2: "user"
    };

    const response = await request(app)
      .post('/chats') 
      .send(newChat) 
      .expect(401);
  })

  it("should fail to create a chat without an authorized user", async () => {
    const newChat = {
      member_1: "new",
      member_2: "user123"
    };

    const response = await request(app)
      .post('/chats-no-middleware') 
      .send(newChat) 
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    const response = await request(app)
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    expect(response.body).toHaveProperty('_id'); 
    expect([response.body.member_1, response.body.member_2]).toContain(main_user); 
    expect([response.body.member_1, response.body.member_2]).toContain(other_user); 
  })

  it("should fail to create a chat without all the members", async () => {
    const newChat = {
      member_1: "user123",
    };

    const response = await request(app)
      .post('/chats') 
      .send(newChat) 
      .expect(400);
  })
})

describe("Testing getChats", () => {
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    await request(app)
      .post('/chats') 
      .send(newChat_2) 
      .expect(200);

    const response = await request(app)
      .get('/chats') 
      .expect(200);
    expect(response.body[0]).toHaveProperty('_id'); 
    expect([response.body[0].member_1, response.body[0].member_2]).toContain(main_user); 
    expect([response.body[1].member_1, response.body[1].member_2]).toContain(main_user);
  });

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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    await request(app)
      .post('/chats') 
      .send(newChat_2) 
      .expect(200);

    const response = await request(app)
      .get('/chats-unauthorized') 
      .expect(401);
  });
})

describe("Testing getChat", () => {
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    let chat = await request(app)
      .post('/chats') 
      .send(newChat_2) 
      .expect(200);

    const response = await chatService.getChat(chat.body._id);
    expect(response).toHaveProperty('_id'); 
    expect([response?.member_1, response?.member_2]).toContain(main_user); 
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    const response = await request(app)
      .post(`/chat/${chat.body._id}`) 
      .send(newMessage) 
      .expect(200);

    expect(response.body).toHaveProperty('_id'); 
    expect(response.body.sender).toBe(main_user); 
    expect(response.body.message).toBe("hi"); 
    expect(response.body.chatroom_id).toBe(chat.body._id); 
  });

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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    const response = await request(app)
      .post(`/chat/${chat.body._id}`) 
      .send(newMessage) 
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    const response = await request(app)
      .post(`/chat-no-middleware/${chat.body._id}`) 
      .send(newMessage) 
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    const response = await request(app)
      .post(`/chat/${chat.body._id}`) 
      .send(newMessage) 
      .expect(400);
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    const message = await request(app)
      .post(`/chat/${chat.body._id}`) 
      .send(newMessage) 
      .expect(200);

    const response = await request(app)
      .delete(`/chat/${chat.body._id}/messages/${message.body._id}`)
      .expect(200)

    const deleted = await MessageModel.findById(message.body._id)

    expect(response.body).toHaveProperty('_id'); 
    expect(response.body.sender).toBe(main_user); 
    expect(response.body.message).toBe("hi"); 
    expect(response.body.chatroom_id).toBe(chat.body._id); 
    expect(deleted).toBeNull()
  });

  it('should fail to delete non-existant message', async () => {
    const newId = new mongoose.Types.ObjectId(0)
    const response = await request(app)
      .delete(`/chat/${newId}/messages/${newId}`) 
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    const message = await request(app)
      .post(`/chat-string/${chat.body._id}`) 
      .send(newMessage) 
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    const message = await request(app)
      .post(`/chat/${chat.body._id}`) 
      .send(newMessage) 
      .expect(200);

    const response = await request(app)
      .delete(`/chat-no-middleware/${chat.body._id}/messages/${message.body._id}`)
      .expect(401)
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    const message1 = await request(app)
      .post(`/chat/${chat.body._id}`) 
      .send(newMessage) 
      .expect(200);

    const message2 = await request(app)
      .post(`/chat/${chat.body._id}`) 
      .send(newMessage_2) 
      .expect(200);

    const response = await request(app)
      .get(`/chats/${chat.body._id}`)
      .expect(200)

    expect(response.body[0]).toHaveProperty('_id'); 
    expect(response.body[0].sender).toBe(main_user); 
    expect(response.body[0].message).toBe("hi"); 
    expect(response.body[0].chatroom_id).toBe(chat.body._id); 
    expect(response.body[1].sender).toBe(main_user); 
    expect(response.body[1].message).toBe("whats up"); 
    expect(response.body[1].chatroom_id).toBe(chat.body._id); 
  });

  it('should fail to get messages of unauthorized chat', async () => {
    const newChat = {
      member_1: "string",
      member_2: "new"
    };

    const chat = await request(app)
      .post('/chats-string') 
      .send(newChat) 
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
      .post('/chats-string') 
      .send(newChat) 
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    const newId = new mongoose.Types.ObjectId(0)

    const response = await request(app)
      .get(`/chats/${newId}`)
      .expect(404)
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
      .post('/chats') 
      .send(newChat) 
      .expect(200);

    const response = await request(app)
      .delete(`/chats/${chat.body._id}`) 
      .expect(200);

    const deleted = await ChatModel.findById(response.body._id)
    expect(response.body).toHaveProperty('_id'); 
    expect([response.body.member_1, response.body.member_2]).toContain(main_user); 
    expect(deleted).toBeNull();
  });

  it('should fail to delete non-existant chat', async () => {
    const newId = new mongoose.Types.ObjectId(0)
    const response = await request(app)
      .delete(`/chats/${newId}`) 
      .expect(404);
  });

  it('should fail to delete chat as an unauthorized user', async () => {
    const newChat = {
      member_1: "string",
      member_2: "new"
    };

    const chat = await request(app)
      .post('/chats-string') 
      .send(newChat) 
      .expect(200);

    const response = await request(app)
      .delete(`/chats-no-middleware/${chat.body._id}`) 
      .expect(401);
  });

  it('should fail to delete chat without proper authorization', async () => {
    const newChat = {
      member_1: "string",
      member_2: "new"
    };

    const chat = await request(app)
      .post('/chats-string') 
      .send(newChat) 
      .expect(200);

    const response = await request(app)
      .delete(`/chats/${chat.body._id}`) 
      .expect(401);
  });
})
