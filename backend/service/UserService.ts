import jwt from "jsonwebtoken";
import { UserModel } from "../model/UserModel"
import { OAuth2Client } from "google-auth-library";

const client = new OAuth2Client(process.env.WEB_CLIENT_ID);

export class UserService {

    async createUser(id: string, name: string, firebaseToken: String) {
        try {
            const newUser = new UserModel({ _id: id, username: name, firebaseToken })
            return newUser.save()
        } catch(error) {
            console.error("Error creating user:", error);
            return null
        }
    }

    async getUser(id: string) {
        try {
            let user = await UserModel.findById(id)
            console.log("USER: ", user) 
            return UserModel.findById(id)
        } catch(error) {
            console.error("Error getting user:", error);
            return null
        }
    }

    async signInWithGoogle(googleToken: String, firebaseToken: String){
        // Verify Google token
        const ticket = await client.verifyIdToken({
            idToken: googleToken.replace('Bearer ', ''),
            audience: process.env.GOOGLE_CLIENT_ID,
        });


        const payload = ticket.getPayload();
        if (!payload || !payload.sub || !payload.name) {
            throw new Error("Invalid Google Token");
        }

        // Check if user exists, otherwise create a new one
        let user = await this.getUser(payload.sub);
    
        if (!user) {
            user = await this.createUser(payload.sub, payload.name, firebaseToken);
            console.log("CREATED NEW USER")
        }

        // Generate JWT
        const jwtToken = jwt.sign({ id: payload.sub, name: payload.name }, process.env.JWT_SECRET!, {
            expiresIn: "999d",
            algorithm: "HS256"
        });
        let userID = user!._id


        return { token: jwtToken, userID };
    }

}