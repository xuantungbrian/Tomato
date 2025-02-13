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
        return this.postService.getPosts();
    }

    async getPostById(req: Request, res: Response, next: NextFunction) {
        const postId = (req as any).param.postId
        return this.postService.getPostById(postId);
    }

    async updatePost(req: Request, res: Response, next: NextFunction) {
        return this.postService.updatePost();
    }

    async deletePost(req: Request, res: Response, next: NextFunction) {
        return this.postService.deletePost();
    }
}