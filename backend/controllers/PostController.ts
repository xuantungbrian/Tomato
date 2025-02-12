import { PostService } from "../service/PostService";
import { NextFunction } from "express"

export class PostController {
    private postService: PostService;

    constructor() {
        this.postService = new PostService();
    }

    async createPost(req: Request, res: Response, next: NextFunction) {
        return this.postService.createPost();
    }

    async getPosts(req: Request, res: Response, next: NextFunction) {
        return this.postService.getPost();
    }

    async getPostById(req: Request, res: Response, next: NextFunction) {
    }

    async updatePost(req: Request, res: Response, next: NextFunction) {
        return this.postService.updatePost();
    }

    async deletePost(req: Request, res: Response, next: NextFunction) {
        return this.postService.deletePost();
    }
}