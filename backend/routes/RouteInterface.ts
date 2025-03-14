import { Request, Response } from "express";

export interface Route{
    route: string;
    method: string;
    validation: unknown[];
    action: (req: Request , res: Response) => Promise<void>;
    protected: boolean;
}