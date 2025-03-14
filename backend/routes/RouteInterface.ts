import { Request, Response } from "express";
import { AuthenticatedRequest } from "..";

export interface Route{
    route: string;
    method: string;
    validation: unknown[];
    action: (req: Request , res: Response) => Promise<void>;
    protected: boolean;
}