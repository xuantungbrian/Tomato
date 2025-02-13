import { body, param } from "express-validator";
import { TodoController } from "../controllers/TodoController";
import { PostController } from "../controllers/PostController";
import { UserController } from "../controllers/UserController";

const postController = new PostController();

export const TodoRoutes = [
    {
        method: "get",
        route: "/posts",            
        action: postController.getPosts,
        validation: []
    },
    {
        method: "get",
        route: "/posts/:id",    
        action: postController.getPostById,
        validation: []
    },
    {
        method: "post",
        route: "/posts",             
        action: postController.createPost,
        validation: []
    },
    {
        method: "put",
        route: "/posts/:id",          
        action: postController.updatePost,
        validation: []
    },
    {
        method: "delete",
        route: "/posts/:id",          
        action: postController.deletePost,
        validation: []
    }
   
]