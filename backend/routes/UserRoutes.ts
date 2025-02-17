
import { UserController } from "../controllers/UserController";

const userController = new UserController();

export const UserRoutes = [

    {
        method: "post",
        route: "/user/auth",    
        action: userController.handleGoogleSignIn,
        validation: []
    }
]