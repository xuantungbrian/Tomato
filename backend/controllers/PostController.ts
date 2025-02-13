import { PostService } from "../service/PostService";
import { NextFunction } from "express"

export class PostController {
    private postService: PostService;

    constructor() {
        this.postService = new PostService();
    }

    async createPost(req: Request, res: Response, next: NextFunction) {
        let post = (req as any).body.post
        return this.postService.createPost(post);
    }

    async getPosts(req: Request, res: Response, next: NextFunction) {
        const postId = (req as any).param.id
        return this.postService.getPosts(postId);
    }

    async getPostById(req: Request, res: Response, next: NextFunction) {
        const postId = (req as any).param.id
        return this.postService.getPostById(postId);
    }

    async updatePost(req: Request, res: Response, next: NextFunction) {
        const postId = (req as any).param.id
        const updatedPost = (req as any).body.post
        return this.postService.updatePost(postId, updatedPost);
    }

    async deletePost(req: Request, res: Response, next: NextFunction) {
        const postId = (req as any).param.id
        return this.postService.deletePost(postId);
    }
}