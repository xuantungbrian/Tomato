import { FileService } from "../service/FileService";
import { Request, Response, NextFunction } from "express";

export class FileController {
    private fileService: FileService;

    constructor() {
        this.fileService = new FileService();
    }

    createFile = async (req: Request, res: Response, next: NextFunction) => {
        const postId = req.body.postId
        const fileId = (req as any).file.id
        res.json(await this.fileService.createFile(fileId, postId))
    }

    getFile = async (req: Request, res: Response, next: NextFunction) => {
        const fileId = req.params.id;
        (await this.fileService.getFile(fileId)).pipe(res);
    }
}