import jwt from "jsonwebtoken";
import { UserModel, IUser } from "../model/UserModel"
import { OAuth2Client } from "google-auth-library";

if (!process.env.WEB_CLIENT_ID) {
    throw new Error("WEB_CLIENT_ID is not defined in environment variables");
  }
if (!process.env.JWT_SECRET) {
    throw new Error("JWT_SECRET is not defined in environment variables");
}
const webClientId: string = process.env.WEB_CLIENT_ID
const jwtSecret: string = process.env.JWT_SECRET

const client: OAuth2Client = new OAuth2Client(webClientId);


export class UserService {

    async createUser(id: string, name: string, firebaseToken: string): Promise<IUser|null> {
        if(typeof id !== 'string' || typeof name !== 'string' || typeof firebaseToken !== 'string'){
            console.warn("Invalid input")
            return null
        }
        try {
            const newUser: IUser = new UserModel({ _id: id, username: name, firebaseToken })
            return await newUser.save()
        } catch (error: unknown) {
            if (error instanceof Error) {
              console.error("Error creating user:", error.message);
            } else {
              console.error("Error creating user:", error);
            }
            return null;
          }
    }

    async getUser(id: string): Promise<IUser|null> {
        try {
            const user: IUser|null = await UserModel.findById(id)
            if(!user){
                console.warn("USER NOT FOUND: ", id)
                return null;
            }

            console.log("USER: ", user)
            return user
        } catch(error) {
            console.error("Error getting user:", error);
            return null
        }
    }

    async signInWithGoogle(googleToken: string, firebaseToken: string){
        // Verify Google token
        const ticket = await client.verifyIdToken({
            idToken: googleToken.replace('Bearer ', ''),
            audience: process.env.GOOGLE_CLIENT_ID,
        });


        const payload = ticket.getPayload();
        if (!payload?.sub || !payload.name) {
            throw new Error("Invalid Google Token");
        }

        // Check if user exists, otherwise create a new one
        let user: IUser|null = await this.getUser(payload.sub);
    
        if (!user) {
            user = await this.createUser(payload.sub, payload.name, firebaseToken);
            console.log("CREATED NEW USER")
        } else {
            if (!user.firebaseToken.includes(firebaseToken)) {
                user.firebaseToken.push(firebaseToken);
                user.save(); //TODO: Need to invalidate the token when user sign out
            }            
        } 
        
        // Generate JWT
        const jwtToken = jwt.sign({ id: payload.sub, name: payload.name }, jwtSecret, {
            expiresIn: "999d",
            algorithm: "HS256"
        });
        let userID = user?._id

        return { token: jwtToken, userID };
    }

}