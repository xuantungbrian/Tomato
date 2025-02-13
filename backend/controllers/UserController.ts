import { UserService } from "../service/UserService";
import { NextFunction } from "express"

export class UserController {
    private userService: UserService;

    constructor() {
        this.userService = new UserService();
    }

    async createUser(req: Request, res: Response, next: NextFunction) {
        return this.userService.createUser((req as any).user.googleId, (req as any).user.name);
    }

    async getUser(req: Request, res: Response, next: NextFunction) {
        return this.userService.getUser((req as any).user.googleId);
    }
}