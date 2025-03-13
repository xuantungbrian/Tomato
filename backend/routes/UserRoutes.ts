
import { UserController } from "../controllers/UserController";
import {Route} from "./RouteInterface";

const userController = new UserController();

export const UserRoutes: Route[] = [

    {
        method: "post",
        route: "/user/auth",    
        action: userController.handleGoogleSignIn,
        validation: [],
        protected: false
    },

    {
        method: "get",
        route: "/user/:id",    
        action: userController.getUser,
        validation: [],
        protected: false
    }
]