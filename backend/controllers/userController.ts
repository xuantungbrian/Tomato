import { UserService } from "../service/userService";

export class UserController {
    private userService: UserService;

    constructor() {
        const userService = new UserService();
    }

    async createUser(req: Request, res: Response, next: NextFunction) {
        return userService.createUser();
    }

    async getUser(req: Request, res: Response, next: NextFunction) {
        return userService.getUser();
    }
}