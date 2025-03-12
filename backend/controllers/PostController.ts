import { PostService } from "../service/PostService";
import { Request, Response } from "express";
import MissingCoordinateException from "../errors/customError";
import { PostModel } from "../model/PostModel";

interface ImageData {
    fileData: Buffer;
    fileType: string;
}

export class PostController {
    private postService: PostService;

    constructor() {
        this.postService = new PostService();
    }

    createPost = async (req: Request, res: Response) => {
        const post = req.body;
        post.userId = (req as any).user.id;
        post.images = (post.images as string[]).map((str: string): ImageData => ({
            fileData: Buffer.from(str, 'base64'),
            fileType: 'image/jpeg'
        }));

        res.json(await this.postService.createPost(post))
    }

    getPublicPost = async (req: Request, res: Response) => {
        try {
            const { parsedStartLat, parsedEndLat, parsedStartLong, parsedEndLong } = parseLocationParam(req)

            // Call service with the query params
            res.json(await this.postService.getPublicPost(
                parsedStartLat,
                parsedEndLat,
                parsedStartLong,
                parsedEndLong,
            ));
        }
        catch (error) {
            if (error instanceof MissingCoordinateException) {
                console.log("User Provided Invalid coordinate: ", error)
                return res.status(400).json({ message: "Incomplete coordinate" });
            }
            else {
                console.log("Error: ", error);
                return res.status(500).json({ message: "Internal Server Error" });

            }
        }
    };


    getAuthenticatedUserPost = async (req: Request, res: Response) => {
        try {
            const { userPostOnly, start_lat, end_lat, start_long, end_long } = req.query;

            // Parse the coordinates if present
            const parsedStartLat = start_lat ? parseFloat(start_lat as string) : undefined;
            const parsedEndLat = end_lat ? parseFloat(end_lat as string) : undefined;
            const parsedStartLong = start_long ? parseFloat(start_long as string) : undefined;
            const parsedEndLong = end_long ? parseFloat(end_long as string) : undefined;

            const parsedUserPostOnly = userPostOnly == "true"

            const userId = (req as any).user.id
            res.json(await this.postService.getUserPost(userId, parsedUserPostOnly, parsedStartLat,
                parsedEndLat, parsedStartLong,
                parsedEndLong))
        }

        catch (err) {
            console.log("ERROR: ", err)
            return res.status(500).json({ message: "Internal Server Error" })
        }


    }

    getPostById = async (req: Request, res: Response) => {
        const postId = req.params.id
        const [postData] = await Promise.all([
            this.postService.getPostById(postId)
        ]);

        res.json({ postData });
    }

    updatePost = async (req: Request, res: Response) => {
        const postId = req.params.id
        const updatedPost = req.body
        updatedPost.userId = (req as any).user.id
        res.json(await this.postService.updatePost(postId, updatedPost))
    }

    deletePost = async (req: Request, res: Response) => {
        const postId = req.params.id

        // Ensure that the post really belongs to the user
        const userId = (req as any).user.id
        const post = await PostModel.findById(postId)

        if (post?.userId !== userId) {
            res.status(401).send({ message: "Unauthorized" })
        }
        else {
            res.json(await this.postService.deletePost(postId))
        }
    }
}

/**
 * Parse the longitude and latitude information of a request param
 */
function parseLocationParam(req: Request) {
    const { start_lat, end_lat, start_long, end_long } = req.query;

    // Parse the coordinates if present
    const parsedStartLat = start_lat ? parseFloat(start_lat as string) : undefined;
    const parsedEndLat = end_lat ? parseFloat(end_lat as string) : undefined;
    const parsedStartLong = start_long ? parseFloat(start_long as string) : undefined;
    const parsedEndLong = end_long ? parseFloat(end_long as string) : undefined;

    return {
        parsedStartLat: parsedStartLat,
        parsedEndLat: parsedEndLat,
        parsedStartLong: parsedStartLong,
        parsedEndLong: parsedEndLong
    }

}