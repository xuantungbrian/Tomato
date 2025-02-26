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
        try{
            const { start_lat, end_lat, start_long, end_long, includePrivate } = req.query;
            // Parse the coordinates if present
            const parsedStartLat = start_lat ? parseFloat(start_lat as string) : undefined;
            const parsedEndLat = end_lat ? parseFloat(end_lat as string) : undefined;
            const parsedStartLong = start_long ? parseFloat(start_long as string) : undefined;
            const parsedEndLong = end_long ? parseFloat(end_long as string) : undefined;
        
            // Parse the "isPrivate" parameter as a boolean
            const isPostPrivate = includePrivate === 'true'; // "true" or "false" as strings from query param
                
            // If there is parameter, ensure it's complete
            if ( (!parsedStartLat || !parsedEndLat || !parsedStartLong || !parsedEndLong)
                && Object.keys(req.query).length > 0) {
                return res.status(400).json({ message: "Missing required query parameters" });
            }
        
            // Call your service with the query params
            res.json(await this.postService.getPosts(
                parsedStartLat, 
                parsedEndLat, 
                parsedStartLong, 
                parsedEndLong,
            ));
        }
        catch(err){
            console.log("ERROR: ", err)
            return res.status(500).json({ message: "Internal Server Error" });

        }
    };

 
    getUserPost = async(req: Request, res: Response, next: NextFunction) => {
        try{
            const userId = (req as any).user.id
            res.json(await this.postService.getUserPost(userId))
        }

        catch(err){
            console.log("ERROR: ", err)
            return res.status(500).json({message: "Internal Server Error"})
        }


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