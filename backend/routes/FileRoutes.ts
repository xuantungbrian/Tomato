
import { FileController } from "../controllers/FileController";

const fileController = new FileController();

export const FileRoutes = [
    {
        method: "get",
        route: "/files/:id",    
        action: fileController.getFile,
        validation: []
    },
    {
        method: "post",
        route: "/files",             
        action: fileController.createFile,
        validation: []
    },
]