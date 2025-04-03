import express, {Request, Response, NextFunction} from 'express';
import morgan from 'morgan';
import { ChatController } from '../controllers/ChatController';
import { AuthenticatedRequest } from '../types/AuthenticatedRequest';
import { PostController } from '../controllers/PostController';
import { RecommendationController } from '../controllers/RecommendationController';
import { UserController } from '../controllers/UserController';

let getPostWrapper = (req: Request, res: Response): void => {
    postController.getPostById(req as AuthenticatedRequest, res)
    .then(() => { return; })
    .catch((err: unknown) => { console.error(err); });
  }

const app: express.Application = express();
app.use(express.json());  
app.use(morgan('tiny')); 

const chatController = new ChatController();
const postController = new PostController();
const recommendationController = new RecommendationController();
const userController = new UserController();
const {verifyToken} = require('../middleware/verifyToken');

// Chat.test.ts routes
app.post('/chats', (req, res, next) => {
    (req as AuthenticatedRequest).user = { id: 'user123' };
    next();
  },  (req: Request, res: Response, next: NextFunction): void => {
    try{
      chatController.createChat(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }}); 
app.post('/chats-string', (req, res, next) => {
    (req as AuthenticatedRequest).user = { id: 'string' }; 
    next();
  },  (req: Request, res: Response, next: NextFunction): void => {
    try{
      chatController.createChat(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }}); 
app.post('/chats-no-middleware',  (req: Request, res: Response, next: NextFunction): void => {
    try{
      chatController.createChat(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});
app.get('/chats/:id', (req, res, next) => {
      (req as AuthenticatedRequest).user = { id: 'user123' }; 
      next();
    },  (req: Request, res: Response, next: NextFunction): void => {
      try{
        chatController.getChatMessages(req as AuthenticatedRequest, res)
        .then(() => { next(); })
       .catch((err: unknown) => { next(err); });
      } catch(err) {
        next(err);
      }});  
app.get('/chats-no-middleware/:id',  (req: Request, res: Response, next: NextFunction): void => {
    try{
      chatController.getChatMessages(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});  
app.get('/chats', (req, res, next) => {
      (req as AuthenticatedRequest).user = { id: 'user123' }; 
      next();
    }, (req: Request, res: Response, next: NextFunction): void => {
      try{
        chatController.getChats(req as AuthenticatedRequest, res)
        .then(() => { next(); })
       .catch((err: unknown) => { next(err); });
      } catch(err) {
        next(err);
      }});  
app.get('/chats-unauthorized',  (req: Request, res: Response, next: NextFunction): void => {
    try{
      chatController.getChats(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});  
app.post('/chat/:id', (req, res, next) => {
      (req as AuthenticatedRequest).user = { id: 'user123' }; 
      next();
    }, (req: Request, res: Response, next: NextFunction): void => {
      try{
        chatController.addMessage(req as AuthenticatedRequest, res)
        .then(() => { next(); })
       .catch((err: unknown) => { next(err); });
      } catch(err) {
        next(err);
      }});
app.post('/chat-string/:id', (req, res, next) => {
      (req as AuthenticatedRequest).user = { id: 'string' }; 
      next();
    },  (req: Request, res: Response, next: NextFunction): void => {
      try{
        chatController.addMessage(req as AuthenticatedRequest, res)
        .then(() => { next(); })
       .catch((err: unknown) => { next(err); });
      } catch(err) {
        next(err);
      }});
app.post('/chat-no-middleware/:id',  (req: Request, res: Response, next: NextFunction): void => {
    try{
      chatController.addMessage(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});
app.delete('/chats/:id', (req, res, next) => {
      (req as AuthenticatedRequest).user = { id: 'user123' }; 
      next();
    },  (req: Request, res: Response, next: NextFunction): void => {
      try{
        chatController.deleteChat(req as AuthenticatedRequest, res)
        .then(() => { next(); })
       .catch((err: unknown) => { next(err); });
      } catch(err) {
        next(err);
      }});
app.delete('/chats-no-middleware/:id',  (req: Request, res: Response, next: NextFunction): void => {
    try{
      chatController.deleteChat(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});
app.delete('/chat/:id/messages/:message_id', (req, res, next) => {
      (req as AuthenticatedRequest).user = { id: 'user123' }; 
      next();
    },  (req: Request, res: Response, next: NextFunction): void => {
      try{
        chatController.deleteMessage(req as AuthenticatedRequest, res)
        .then(() => { next(); })
       .catch((err: unknown) => { next(err); });
      } catch(err) {
        next(err);
      }});
app.delete('/chat-no-middleware/:id/messages/:message_id',  (req: Request, res: Response, next: NextFunction): void => {
    try{
      chatController.deleteMessage(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});

// Post.test.ts routes
app.get('/posts-authenticated', (req : Request, res : Response, next : NextFunction) => {
    (req as AuthenticatedRequest).user = { id: 'user123' }; 
    next();
  },  (req: Request, res: Response, next: NextFunction): void => {
    try {
        postController.getAuthenticatedUserPost(req as AuthenticatedRequest, res)
        .then(() => { next(); })
       .catch((err: unknown) => { next(err); });
    } catch (error) {
        next(error);
    }
  }); 
app.get('/posts-authenticated-not',  (req: Request, res: Response, next: NextFunction): void => {
    try {
        postController.getAuthenticatedUserPost(req as AuthenticatedRequest, res)
        .then(() => { next(); })
       .catch((err: unknown) => { next(err); });
    } catch (error) {
        next(error);
    }
  }); 
app.post('/posts', (req, res, next) => {
    (req as AuthenticatedRequest).user = { id: 'user123' };
    next();
  },  (req: Request, res: Response, next: NextFunction): void => {
    try{
      postController.createPost(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});
app.post('/posts-not-authenticated',  (req: Request, res: Response, next: NextFunction): void => {
    try{
      postController.createPost(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});
app.post('/posts-from-other', (req, res, next) => {
    (req as AuthenticatedRequest).user = { id: 'other' };
    next();
  },  (req: Request, res: Response, next: NextFunction): void => {
    try{
      postController.createPost(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }}); 
app.get('/posts/:id', getPostWrapper);    
app.put('/posts/:id', (req, res, next) => {
    (req as AuthenticatedRequest).user = { id: 'user123' }; 
    next();
  },  (req: Request, res: Response, next: NextFunction): void => {
    try{
      postController.updatePost(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});  
app.put('/posts-not-auth/:id',  (req: Request, res: Response, next: NextFunction): void => {
    try{
      postController.updatePost(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});    
app.delete('/posts/:id', (req, res, next) => {
    (req as AuthenticatedRequest).user = { id: 'user123' }; 
    next();
  },  (req: Request, res: Response, next: NextFunction): void => {
    try{
      postController.deletePost(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});
app.delete('/posts-not-auth/:id',  (req: Request, res: Response, next: NextFunction): void => {
    try{
      postController.deletePost(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }});  
app.get('/posts',   (req: Request, res: Response, next: NextFunction): void => {
    try {
        postController.getPublicPost(req, res)
        .then(() => { next(); })
       .catch((err: unknown) => { next(err); });
    } catch (error) {
        next(error);
    }
  });  

// Recommendation.test.ts routes
app.get('/recommendations-no-middlewware', (req, res, next) : void => {
    try {
      recommendationController.getRecommendation(req as AuthenticatedRequest, res)
      .then(() => { next(); })
      .catch((err: unknown) => { next(err); });
    } catch(error) {
      next(error)
    }
  });  
app.post('/posts-from-someone', (req, res, next) => {
    (req as AuthenticatedRequest).user = { id: 'someone' }; 
    next();
  }, (req: Request, res: Response, next: NextFunction): void => {
    try{
      postController.createPost(req as AuthenticatedRequest, res)
      .then(() => { next(); })
      .catch((err: unknown) => { next(err); }); 
    } catch(err) {
      next(err);
    }}); 
app.post('/posts-from-else', (req, res, next) => {
    (req as AuthenticatedRequest).user = { id: 'else' }; 
    next();
  }, (req: Request, res: Response, next: NextFunction): void => {
    try{
      postController.createPost(req as AuthenticatedRequest, res)
      .then(() => { next(); })
      .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }}); 
app.post('/posts-from-fourth', (req, res, next) => {
    (req as AuthenticatedRequest).user = { id: 'fourth' }; 
    next();
  }, (req: Request, res: Response, next: NextFunction): void => {
    try{
      postController.createPost(req as AuthenticatedRequest, res)
      .then(() => { next(); })
      .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }}); 

// User.test.ts routes
app.post('/user/auth',  (req: Request, res: Response, next: NextFunction): void => {
    try {
        userController.handleGoogleSignIn(req, res)
        .then(() => { next(); })
       .catch((err: unknown) => { next(err); });
    } catch (error) {
        next(error);
    }}); 
app.get('/user/:id',  (req: Request, res: Response, next: NextFunction): void => {
    try {
        userController.getUser(req, res)
        .then(() => { next(); })
        .catch((err: unknown) => { next(err); });
    } catch (error) {
        next(error);
    }}); 

// VerifyTokenMock.test.ts routes
app.post('/posts-verify', verifyToken, (req: Request, res: Response, next: NextFunction): void => {
    try{
      postController.createPost(req as AuthenticatedRequest, res)
      .then(() => { next(); })
     .catch((err: unknown) => { next(err); });
    } catch(err) {
      next(err);
    }}); 

// NoPayloadUser.test.ts routes
app.post('/user-faulty/auth', (req: Request, res: Response, next: NextFunction): void => {
    try {
        userController.handleGoogleSignIn(req, res)
        .then(() => { next(); })
        .catch((err: unknown) => { next(err); });
    } catch (error) {
        next(error);
    }});  


module.exports =  {app};