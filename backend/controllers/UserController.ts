import { UserService } from "../service/UserService";
import { NextFunction } from "express"

export class UserController {
    private userService: UserService;

    constructor() {
        this.userService = new UserService();
    }

<<<<<<< HEAD
    async createUser(req: Request, res: Response, next: NextFunction) {
        return this.userService.createUser((req as any).user.googleId, (req as any).user.name);
    }

    async getUser(req: Request, res: Response, next: NextFunction) {
        return this.userService.getUser((req as any).user.googleId);
=======
    async createUser(req: Request, res: Response, next: NextFunction) { // TODO: Likely not be used so I have not tested this, cleanup later
        const {id, name} = (req as any).user
        return this.userService.createUser(id, name);
    }

    async getUser(req: Request, res: Response, next: NextFunction) { // TODO: Likely not be used so I have not tested this, cleanup later
        return this.userService.getUser((req as any).user.id);
>>>>>>> f5c574e6c63e3c52412b93402d87edf23c435f89
    }
}