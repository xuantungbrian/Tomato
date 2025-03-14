import jwt, { decode } from 'jsonwebtoken'
import { NextFunction, Request, Response } from "express";


const verifyToken = async (req: Request, res: Response, next: NextFunction) => {
    const token = req.header("Authorization")?.replace("Bearer ", "");
    const JWT_SECRET = process.env.JWT_SECRET!

    if (!token) {
        console.log("token doesn't exist")

        return res.status(401).json({ message: 'No token provided' });
    }

    if (!JWT_SECRET){
        return res.status(500).json({message: "Internal Server Error"});
    }

    try {
        const decoded = jwt.verify(token, JWT_SECRET, {algorithms: ["HS256"]});  // Verify the JWT
        (req as any).user = decoded;  // Store the decoded user info in the request object
        next();  // Proceed to the next middleware or route handler
    } catch (err) {
        console.log("INVALID TOKEN")
        return res.status(400).json({ message: 'Invalid token.' });
    }
};

module.exports =  {verifyToken};
