import { body, param } from "express-validator";
import { TodoController } from "../controllers/TodoController";
import { PostController } from "../controllers/PostController";
import { UserController } from "../controllers/UserController";

const controller = new TodoController();
const postController = new PostController();
const userController = new UserController();

export const TodoRoutes = [
    {
        method: "get",
        route: "/todolist",
        action: controller.getTodos,
        validation: []
    },
    {
        method: "post",
        route: "/todolist",
        action: controller.postTodos,
        validation: [
            body("task").isString(),
            body("urgent").isBoolean().optional()
        ]
    },
    {
        method: "put",
        route: "/todolist/:id",
        action: controller.putTodos,
        validation: [
            param("id").isMongoId(),
            body("task").isString(),
            body("urgent").isBoolean().optional()
        ]
    },
    {
        method: "get",
        route: "/todolist/:id",
        action: controller.deleteTodos,
        validation: []
    },
    {
        method: "get",
        route: "/serverlocaltime",
        action: controller.getServerLocalTime,
        validation: []
    },    
    {
        method: "get",
        route: "/firstlastname",
        action: controller.getFirstLastName,
        validation: []
    },
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