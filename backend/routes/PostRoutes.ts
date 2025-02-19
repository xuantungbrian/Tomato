import { PostController } from "../controllers/PostController";

const postController = new PostController();

export const PostRoutes = [
    {
        method: "get",
        route: "/posts",            
        action: postController.getPosts,
        validation: [],
        protected: true
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
        validation: [],
        protected: true
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
    },
    {
        method: "get",
        route: "/posts/user",            
        action: postController.getUserPostsOnPage,
        validation: [],
    },
    {
        method: "get",
        route: "/posts/all",            
        action: postController.getAllPostsOnPage,
        validation: [],
    }
]