import jwt from 'jsonwebtoken'
import { NextFunction, Request, Response } from "express";
import { AuthenticatedRequest } from '..';


const verifyToken = async (req: Request, res: Response, next: NextFunction) => {
    const token = req.header("Authorization")?.replace("Bearer ", "");
    const JWT_SECRET = process.env.JWT_SECRET

    if (!JWT_SECRET){
        res.status(500).json({message: "Internal Server Error"});
        return
    }

    if (!token) {
        console.log("token doesn't exist")

        res.status(401).json({ message: 'No token provided' });
        return
    }

    if (!JWT_SECRET){
        res.status(500).json({message: "Internal Server Error"});
        return
    }

    else{
        try {
            const decoded = jwt.verify(token, JWT_SECRET, {algorithms: ["HS256"]}) as {id: string};  // Verify the JWT
            (req as AuthenticatedRequest).user = decoded;  // Store the decoded user info in the request object
            next();  // Proceed to the next middleware or route handler
        } catch (err) {
            console.log("INVALID TOKEN")
            return res.status(400).json({ message: 'Invalid token.' });
        }

    }

};

export default verifyToken;
