import { UserService } from "../service/UserService";
import { Request, Response } from "express";


export class UserController {
    private userService: UserService;

    constructor() {
        this.userService = new UserService();
        this.handleGoogleSignIn = this.handleGoogleSignIn.bind(this);
        this.getUser = this.getUser.bind(this)
    }

    /**
     * Given a user id in the request params, return the user object.
     */
    async getUser(req: Request, res: Response): Promise<void> { 
        if(!req.params.id || req.params.id.trim() === ""){
            res.status(400).json({ error: 'ID is required' });
            return
        }
        const userId = req.params.id
        res.status(200).json(await this.userService.getUser(userId));
    }

    /**
     * Given a google token and a firebase token generated in the FrontEnd, handle
     * user sign in by generating JWT token and create user if not exists.
     */
    async handleGoogleSignIn(req: Request, res: Response): Promise<void> { 
        try{
            const { googleToken, firebaseToken } = req.body;
            
            if(!googleToken || !firebaseToken) {
                res.status(400).json({message: "No Token provided"});
                return
            }

            const result = await this.userService.signInWithGoogle(googleToken as string, firebaseToken as string); //TODO: Not just google now, need better naming
            res.status(200).json(result)
            return

        }
        catch(err){
            console.error("ERROR: ", err)
            res.status(400).json({message: err})
            return
        }
    }

}