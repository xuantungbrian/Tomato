import { UserService } from "../service/UserService";
import { NextFunction } from "express"

export class UserController {
    private userService: UserService;

    constructor() {
        this.userService = new UserService();
    }

    async createUser(req: Request, res: Response, next: NextFunction) {
        return this.userService.createUser(req.user.googleId, req.user.name);
    }

    async getUser(req: Request, res: Response, next: NextFunction) {
        return this.userService.getUser();
    }
}