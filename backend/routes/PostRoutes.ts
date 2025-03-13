import { PostController } from "../controllers/PostController";
import {Route} from "./RouteInterface";

const postController = new PostController();

export const PostRoutes: Route[] = [
    {
        method: "get",
        route: "/posts",            
        action: postController.getPublicPost,
        validation: [],
        protected: false
    },
    {
        method: "get",
        route: "/posts-authenticated",            
        action: postController.getAuthenticatedUserPost,
        validation: [],
        protected: true 
    },
    {
        method: "get",
        route: "/posts/:id",    
        action: postController.getPostById,
        validation: [],
        protected: false
    },
    {
        method: "post",
        route: "/posts",             
        action: postController.createPost,
        validation: [],
        protected: true
    },
    {
        method: "put",
        route: "/posts/:id",          
        action: postController.updatePost,
        validation: [],
        protected: true
    },
    {
        method: "delete",
        route: "/posts/:id",          
        action: postController.deletePost,
        validation: [],
        protected: true
    }
]
