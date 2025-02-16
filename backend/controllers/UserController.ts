import { UserService } from "../service/UserService";
import { NextFunction } from "express"

export class UserController {
    private userService: UserService;

    constructor() {
        this.userService = new UserService();
    }

    async createUser(req: Request, res: Response, next: NextFunction) { // TODO: Likely not be used so I have not tested this, cleanup later
        const {id, name} = (req as any).user
        return this.userService.createUser(id, name);
    }

    async getUser(req: Request, res: Response, next: NextFunction) { // TODO: Likely not be used so I have not tested this, cleanup later
        return this.userService.getUser((req as any).user.id);
    }
}