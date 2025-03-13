import express, { NextFunction, Request, Response } from 'express';
import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import morgan from 'morgan';
import { PostController } from '../../controllers/PostController';
import request from 'supertest';
import { PostModel } from '../../model/PostModel';

// Setup MongoDB in-memory server
let mongoServer = new MongoMemoryServer();
// Create the Express app
const app = express();
app.use(express.json());  // Middleware to parse JSON bodies
app.use(morgan('tiny')); // Logger

// Define your routes
const postController = new PostController();
app.get('/posts-authenticated', (req : Request, res : Response, next : NextFunction) => {
  (req as any).user = { id: 'user123' }; // Mock the authenticated user
  next();
}, async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
      await postController.getAuthenticatedUserPost(req, res, next);
  } catch (error) {
      next(error);
  }
}); 

// createPost routes for testing 
app.post('/posts', (req, res, next) => {
  (req as any).user = { id: 'user123' };
  next();
}, postController.createPost);
app.post('/posts-from-other', (req, res, next) => {
  (req as any).user = { id: 'other' };
  next();
}, postController.createPost); 


app.get('/posts/:id', postController.getPostById);  // Route for getting a post by ID
app.put('/posts/:id', (req, res, next) => {
  (req as any).user = { id: 'user123' }; // Mock the authenticated user
  next();
}, postController.updatePost);  // Route for updating a post
app.delete('/posts/:id', (req, res, next) => {
  (req as any).user = { id: 'user123' }; // Mock the authenticated user
  next();
}, postController.deletePost);  // Route for deleting a post
app.get('/posts',  async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
      await postController.getPublicPost(req, res, next);
  } catch (error) {
      next(error);
  }
});  // Route for deleting a post
 // Route for deleting a post

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
describe('Unmocked Posts API: Expect Behaviour', () => {
  describe('Testing createPost', () => {
    it('should create a post', async () => {
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
        .expect(200);
  
      expect(response.body).toHaveProperty('_id'); // Check that the response contains _id
      expect(response.body.userId).toBe(newPost.userId); // Check userId matches
      expect(response.body.note).toBe(newPost.note); // Check note matches
    });
  })

  it('should get a post by ID', async () => {
    const newPost = {
      userId: 'user123',
      latitude: 40.7128,
      longitude: -74.0060,
      images: ["string", "image/jpeg"],
      date: new Date(),
      note: 'Test post',
      isPrivate: false,
    };

    const createdPost = await request(app)
      .post('/posts')
      .send(newPost)
      .expect(200);

    const response = await request(app)
      .get(`/posts/${createdPost.body._id}`) // Test fetching a post by ID
      .expect(200);

    expect(response.body.postData).toHaveProperty('_id');
    expect(response.body.postData.userId).toBe(newPost.userId);
  });

  describe('Testing getUserPosts', () => {
    it('should get all authenticated posts, userpostonly = true', async () => {
      const newPost = {
        userId: 'user123',
        latitude: 40.7128,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: true,
      };
  
      const newPost2 = {
        userId: 'user123',
        latitude: 40.7180,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const newPost3 = {
        userId: 'other',
        latitude: 40.90,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const newPost4 = {
        userId: 'other',
        latitude: 40.7180,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: true,
      };
  
      const createdPost = await request(app)
        .post('/posts')
        .send(newPost)
        .expect(200);
      
      const anotherPost = await request(app)
        .post('/posts')
        .send(newPost2)
        .expect(200);
  
      const thirdpost = await request(app)
        .post('/posts-from-other')
        .send(newPost3)
        .expect(200);
  
      const fourth = await request(app)
        .post('/posts-from-other')
        .send(newPost4)
        .expect(200);
  
      const response = await request(app)
        .get(`/posts-authenticated`) // Test fetching a post by ID
        .query({
          userPostOnly: true
        })
        .expect(200);
  
      expect(response.body[0]).toHaveProperty('_id');
      expect(response.body[0].latitude).toBe(newPost.latitude);
      expect(response.body[1].latitude).toBe(newPost2.latitude);
      expect(response.body.length).toEqual(2);
    });

    it('should get authenticated posts in range, userpostonly = true', async () => {
      const newPost = {
        userId: 'user123',
        latitude: 40.7128,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: true,
      };
  
      const newPost2 = {
        userId: 'user123',
        latitude: 40.7180,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const newPost3 = {
        userId: 'other',
        latitude: 40.90,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const newPost4 = {
        userId: 'other',
        latitude: 40.7180,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: true,
      };
  
      const createdPost = await request(app)
        .post('/posts')
        .send(newPost)
        .expect(200);
      
      const anotherPost = await request(app)
        .post('/posts')
        .send(newPost2)
        .expect(200);
  
      const thirdpost = await request(app)
        .post('/posts-from-other')
        .send(newPost3)
        .expect(200);
  
      const fourth = await request(app)
        .post('/posts-from-other')
        .send(newPost4)
        .expect(200);
  
      const response = await request(app)
        .get(`/posts-authenticated`) // Test fetching a post by ID
        .query({
          userPostOnly: true,
          start_lat: 40.0,
          end_lat: 41.0,
          start_long: -75.0,
          end_long: -74.0
        })
        .expect(200);
  
      expect(response.body[0]).toHaveProperty('_id');
      expect(response.body[0].latitude).toBe(newPost.latitude);
      expect(response.body[1].latitude).toBe(newPost2.latitude);
      expect(response.body.length).toEqual(2);
    });
  
    it('should get authenticated posts in range, userpostonly = false', async () => {
      const newPost = {
        userId: 'user123',
        latitude: 40.7128,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: true,
      };
  
      const newPost2 = {
        userId: 'user123',
        latitude: 40.7180,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const newPost3 = {
        userId: 'other',
        latitude: 40.90,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const newPost4 = {
        userId: 'other',
        latitude: 40.7180,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: true,
      };
  
      const createdPost = await request(app)
        .post('/posts')
        .send(newPost)
        .expect(200);
      
      const anotherPost = await request(app)
        .post('/posts')
        .send(newPost2)
        .expect(200);
  
      const thirdpost = await request(app)
        .post('/posts-from-other')
        .send(newPost3)
        .expect(200);
  
      const fourth = await request(app)
        .post('/posts-from-other')
        .send(newPost4)
        .expect(200);
  
      const response = await request(app)
        .get(`/posts-authenticated`) // Test fetching a post by ID
        .query({
          userPostOnly: false,
          start_lat: 40.0,
          end_lat: 41.0,
          start_long: -75.0,
          end_long: -74.0
        })
        .expect(200);
  
      expect(response.body[0]).toHaveProperty('_id');
      expect(response.body[0].latitude).toBe(newPost.latitude);
      expect(response.body[1].latitude).toBe(newPost2.latitude);
      expect(response.body[2].latitude).toBe(newPost3.latitude);
      expect(response.body.length).toEqual(3);
    });
  })

  describe('Testing getPublicPosts', () => {
    it('should get public posts in range', async () => {
      const newPost = {
        userId: 'user123',
        latitude: 40.7128,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: true,
      };
  
      const newPost2 = {
        userId: 'user123',
        latitude: 40.7180,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const createdPost = await request(app)
        .post('/posts')
        .send(newPost)
        .expect(200);
      
      const anotherPost = await request(app)
        .post('/posts')
        .send(newPost2)
        .expect(200);
  
      const response = await request(app)
        .get(`/posts`) // Test fetching a post by ID
        .query({
          start_lat: 40.0,
          end_lat: 41.0,
          start_long: -75.0,
          end_long: -74.0
        })
        .expect(200);
  
      expect(response.body[0]).toHaveProperty('_id');
      expect(response.body[0].latitude).toBe(newPost2.latitude);
      expect(response.body.length).toStrictEqual(1)
    });

    it('should get all public posts', async () => {
      const newPost = {
        userId: 'user123',
        latitude: 40.7128,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: true,
      };
  
      const newPost2 = {
        userId: 'user123',
        latitude: 40.7180,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const createdPost = await request(app)
        .post('/posts')
        .send(newPost)
        .expect(200);
      
      const anotherPost = await request(app)
        .post('/posts')
        .send(newPost2)
        .expect(200);
  
      const response = await request(app)
        .get(`/posts`) // Test fetching a post by ID
        .expect(200);
  
      expect(response.body[0]).toHaveProperty('_id');
      expect(response.body[0].latitude).toBe(newPost2.latitude);
      expect(response.body.length).toStrictEqual(1)
    });
  })

  it('should update a post', async () => {
    const newPost = {
      userId: 'user123',
      latitude: 40.7128,
      longitude: -74.0060,
      images: ["string", "image/jpeg"],
      date: new Date(),
      note: 'Test post',
      isPrivate: false,
    };

    const createdPost = await request(app)
      .post('/posts')
      .send(newPost)
      .expect(200);

    const updatedPost = { ...createdPost.body, note: 'Updated note' };

    const response = await request(app)
      .put(`/posts/${createdPost.body._id}`)
      .send(updatedPost)
      .expect(200);

    expect(response.body.note).toBe('Updated note');
  });
  describe('Testing deletePost', () => {
    it('should delete a post', async () => {
      const newPost = {
        userId: 'user123',
        latitude: 40.7128,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const createdPost = await request(app)
        .post('/posts')
        .send(newPost)
        .expect(200);
  
      const response = await request(app)
        .delete(`/posts/${createdPost.body._id}`)
        .expect(200);
      
      const deleted = await PostModel.findById(response.body._id);
  
      expect(response.body).toHaveProperty('_id');
      expect(response.body._id).toBe(createdPost.body._id);
      expect(deleted).toBeNull();
    });
  })
});

describe('Managing Posts API: Erroneus Behaviour', () => {
  describe('Testing createPost', () => {

  })

  describe('Testing getPublicPosts', () => {
    it('should fail to get posts with no coordinates', async () => {
      const newPost = {
        userId: 'user123',
        latitude: 40.7128,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: true,
      };
  
      const newPost2 = {
        userId: 'user123',
        latitude: 40.7180,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const createdPost = await request(app)
        .post('/posts')
        .send(newPost)
        .expect(200);
      
      const anotherPost = await request(app)
        .post('/posts')
        .send(newPost2)
        .expect(200);
  
      const response = await request(app)
        .get(`/posts`) // Test fetching a post by ID
        .query({
          start_lat: 8
        })
        .expect(400);
    });
  })

  describe('Testing deletePost', () => {
    it('should fail to delete a non-existant post', async () => {
      const newPost = {
        userId: 'user123',
        latitude: 40.7128,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const createdPost = await request(app)
        .post('/posts')
        .send(newPost)
        .expect(200);
       
      const newid = new mongoose.Types.ObjectId(0)
      const response = await request(app)
        .delete(`/posts/${newid}`)
        .expect(404);
    })

    it('should fail to allow an unauthorized user to delete a post', async () => {
      const newPost = {
        userId: 'other',
        latitude: 40.7128,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const createdPost = await request(app)
        .post('/posts-from-other')
        .send(newPost)
        .expect(200);
       
      const response = await request(app)
        .delete(`/posts/${createdPost.body._id}`)
        .expect(401);
    })
  })

  describe('Testing getUserPosts', () => {
    it('should fail to get authenticated posts because of incorrect coordinates', async () => {
      const newPost = {
        userId: 'user123',
        latitude: 40.7128,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: true,
      };
  
      const newPost2 = {
        userId: 'user123',
        latitude: 40.7180,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const newPost3 = {
        userId: 'other',
        latitude: 40.90,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: false,
      };
  
      const newPost4 = {
        userId: 'other',
        latitude: 40.7180,
        longitude: -74.0060,
        images: ["string", "image/jpeg"],
        date: new Date(),
        note: 'Test post',
        isPrivate: true,
      };
  
      const createdPost = await request(app)
        .post('/posts')
        .send(newPost)
        .expect(200);
      
      const anotherPost = await request(app)
        .post('/posts')
        .send(newPost2)
        .expect(200);
  
      const thirdpost = await request(app)
        .post('/posts-from-other')
        .send(newPost3)
        .expect(200);
  
      const fourth = await request(app)
        .post('/posts-from-other')
        .send(newPost4)
        .expect(200);
  
      const response = await request(app)
        .get(`/posts-authenticated`) // Test fetching a post by ID
        .query({
          userPostOnly: false,
          start_lat: 9,
        })
        .expect(400);
    });
  })
})
