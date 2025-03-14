import { UserService } from "../service/UserService";
import { Request, Response } from "express";
import { OAuth2Client } from "google-auth-library";

const clientId = process.env.WEB_CLIENT_ID
const client = new OAuth2Client(clientId);

export class UserController {
    private userService: UserService;

    constructor() {
        this.userService = new UserService();
        this.handleGoogleSignIn = this.handleGoogleSignIn.bind(this);
        this.getUser = this.getUser.bind(this)
    }

    async getUser(req: Request, res: Response): Promise<void> { 
        const userId = req.params.id
        res.status(200).json(await this.userService.getUser(userId));
    }

    async handleGoogleSignIn(req: Request, res: Response): Promise<void> { 
        try{
            const { googleToken, firebaseToken } = (req as any).body;
            
            if(!googleToken || !firebaseToken) {
                res.status(400).json({message: "No Token provided"});
                return
            }

            const result = await this.userService.signInWithGoogle(googleToken, firebaseToken); //TODO: Not just google now, need better naming
            res.status(200).json(result)
            return

        }
        catch(err){
            console.log("ERROR: ", err)
            res.status(400).json({message: err})
            return
        }
    }

}