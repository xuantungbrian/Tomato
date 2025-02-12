import { NextFunction, Request, Response } from "express";

const { OAuth2Client } = require('google-auth-library');
const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

const verifyGoogleToken = async (req: Request, res: Response, next: NextFunction) => {
    try {
        const token = req.headers.authorization

        if (!token) {
            return await res.status(401).json({ message: 'No token provided' });
        }

        const ticket = await client.verifyIdToken({
            idToken: token.replace('Bearer ', ''),
            audience: process.env.GOOGLE_CLIENT_ID,
        });

        const payload = ticket.getPayload();
        (req as any).user = {
            googleId: payload.sub,
            email: payload.email,
            name: payload.name,
            picture: payload.picture,
        };
        // TODO: Add user to database if not exists

        next();
    } catch (error) {
        console.error('Google token verification failed:', error);
        res.status(403).json({ message: 'Invalid token' });
    }
};

export default verifyGoogleToken;
