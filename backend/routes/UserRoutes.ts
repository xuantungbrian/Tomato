
import { UserController } from "../controllers/UserController";
import {Route} from "./RouteInterface";

const userController = new UserController();

export const UserRoutes: Route[] = [

    {
        method: "post",
        route: "/user/auth",    
        action: (req, res) => userController.handleGoogleSignIn(req, res),
        validation: [],
        protected: false
    },

    {
        method: "get",
        route: "/user/:id",    
        action: (req, res) => userController.getUser(req, res),
        validation: [],
        protected: false
    }
]