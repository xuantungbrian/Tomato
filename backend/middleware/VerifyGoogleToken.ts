import { NextFunction, Request, Response } from "express";
import { UserService } from "../service/UserService"
import { OAuth2Client } from "google-auth-library";

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
        if (!payload || !payload.sub || !payload.name) {
            res.status(401).json({ message: 'Invalid payload' });
            return
        }

        (req as any).user = {
            id: payload.sub,
            name: payload.name,
        };
        
        if (!(await userService.getUser(payload.sub))) {
            await userService.createUser(payload.sub, payload.name)
        }

        next();
    } catch (error) {
        console.error('Google token verification failed:', error);
        res.status(403).json({ message: 'Invalid token' });
    }
};

export default verifyGoogleToken;
