import { UserService } from "../service/UserService";
import { Request, Response, NextFunction } from "express";
import { OAuth2Client } from "google-auth-library";

const clientId = process.env.WEB_CLIENT_ID
const client = new OAuth2Client(clientId);

export class UserController {
    private userService: UserService;

    constructor() {
        this.userService = new UserService();
        this.handleGoogleSignIn = this.handleGoogleSignIn.bind(this);
    }

    async createUser(req: Request, res: Response, next: NextFunction) { // TODO: Likely not be used so I have not tested this, cleanup later
        const {id, name} = (req as any).user
        return this.userService.createUser(id, name);
    }

    async getUser(req: Request, res: Response, next: NextFunction) { // TODO: Likely not be used so I have not tested this, cleanup later
        return this.userService.getUser((req as any).user.id);
    }

    async handleGoogleSignIn(req: Request, res: Response, next: NextFunction){
        try{
            const { token } = (req as any).body;
            
            if(!token) {
                return res.status(400).json({message: "No Token provided"});
            }

            console.log("TOKEN: ", token)
            const result = await this.userService.signInWithGoogle(token);
            console.log("RESULT: ", result)
            return res.status(200).json({result: result})

        }
        catch(err){
            console.log("ERROR: ", err)
            return res.status(400).json({message: err})
        }
    }

}