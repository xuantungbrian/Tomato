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

    async getUser(req: Request, res: Response) { 
        const userId = req.params.id
        return res.status(200).json(await this.userService.getUser(userId));
    }

    async handleGoogleSignIn(req: Request, res: Response){ //TODO: Not just google now, need better naming
        try{
            const { googleToken, firebaseToken } = (req as any).body;
            
            if(!googleToken || !firebaseToken) {
                return res.status(400).json({message: "No Token provided"});
            }

            const result = await this.userService.signInWithGoogle(googleToken, firebaseToken); //TODO: Not just google now, need better naming
            return res.status(200).json(result)

        }
        catch(err){
            console.log("ERROR: ", err)
            return res.status(400).json({message: err})
        }
    }

}