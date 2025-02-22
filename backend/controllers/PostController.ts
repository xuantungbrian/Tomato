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
        const { start_lat, end_lat, start_long, end_long, isPrivate } = req.query;
        const user_id = undefined // TODO: Find user id right here
        // Parse the coordinates if present
        const parsedStartLat = start_lat ? parseFloat(start_lat as string) : undefined;
        const parsedEndLat = end_lat ? parseFloat(end_lat as string) : undefined;
        const parsedStartLong = start_long ? parseFloat(start_long as string) : undefined;
        const parsedEndLong = end_long ? parseFloat(end_long as string) : undefined;
    
        // Parse the "isPrivate" parameter as a boolean
        const isPostPrivate = isPrivate === 'true'; // "true" or "false" as strings from query param
    
        // Optionally log or handle missing params
        if (!parsedStartLat || !parsedEndLat || !parsedStartLong || !parsedEndLong) {
            return res.status(400).json({ message: "Missing required query parameters" });
        }
    
        // Call your service with the query params
        res.json(await this.postService.getPosts(
            user_id as any,
            parsedStartLat, 
            parsedEndLat, 
            parsedStartLong, 
            parsedEndLong,
            isPostPrivate 
        ));
    };

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