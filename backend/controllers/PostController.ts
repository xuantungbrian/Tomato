import { FileService } from "../service/FileService";
import { PostService } from "../service/PostService";
import { Request, Response, NextFunction } from "express";
interface ImageData {
    fileData: Buffer;
    fileType: string;
  }

export class PostController {
    private postService: PostService;
    private fileService: FileService;

    constructor() {
        this.postService = new PostService();
        this.fileService = new FileService();
    }

    createPost = async (req: Request, res: Response, next: NextFunction) => {
        const post = req.body;
        post.userId = (req as any).user.id;
        post.images = (post.images as string[]).map((str: string): ImageData => ({
            fileData: Buffer.from(str, 'base64'),
            fileType: 'image/jpeg' 
          }));

        res.json(await this.postService.createPost(post))
    }

    getPosts = async (req: Request, res: Response, next: NextFunction) => {
        const userId = (req as any).user.id
        res.json(await this.postService.getPosts(userId))
    }

    getUserPostsOnPage = async (req: Request, res: Response, next: NextFunction) => {
        const userId = (req as any).user.id
        const coordinate_info = (req as any).body
        res.json(await this.postService.getUserPostsOnPage(userId, coordinate_info.start_lat, coordinate_info.end_lat, coordinate_info.start_long, coordinate_info.end_long))
    }

    getAllPostsOnPage = async (req: Request, res: Response, next: NextFunction) => {
        const coordinate_info = (req as any).body
        res.json(await this.postService.getAllPostsOnPage(coordinate_info.start_lat, coordinate_info.end_lat, coordinate_info.start_long, coordinate_info.end_long))
    }

    getPostById = async (req: Request, res: Response, next: NextFunction) => {
        const postId = req.params.id
        const [fileData, postData] = await Promise.all([
            this.fileService.getFileInPost(postId),
            this.postService.getPostById(postId)
        ]);
          
        res.json({ postData, fileId: fileData});
    }

    updatePost = async (req: Request, res: Response, next: NextFunction) => {
        const postId = req.params.id
        const updatedPost = req.body
        updatedPost.userId = (req as any).user.id
        res.json(await this.postService.updatePost(postId, updatedPost))
    }

    deletePost = async (req: Request, res: Response, next: NextFunction) => {
        const postId = req.params.id
        res.json(await this.postService.deletePost(postId))
    }
}