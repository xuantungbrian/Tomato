import { NextFunction, Request, Response } from "express";
import { UserService } from "../service/UserService"

const { OAuth2Client } = require('google-auth-library');
const clientId = process.env.WEB_CLIENT_ID
const client = new OAuth2Client(clientId);
const userService = new UserService();

const verifyGoogleToken = async (req: Request, res: Response, next: NextFunction) => {
    try {
        const token = req.headers.authorization

        if (!token) {
            res.status(401).json({ message: 'No token provided' });
            return
        }

        const ticket = await client.verifyIdToken({
            idToken: token.replace('Bearer ', ''),
            audience: clientId,
        });

        const payload = ticket.getPayload();
        (req as any).user = {
            googleId: payload.sub,
            email: payload.email,
            name: payload.name,
            picture: payload.picture,
        };
        
        await userService.createUser(payload.sub, payload.name)

        next();
    } catch (error) {
        console.error('Google token verification failed:', error);
        res.status(403).json({ message: 'Invalid token' });
    }
};

export default verifyGoogleToken;
