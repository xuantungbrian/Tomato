const multer = require("multer");
const { GridFsStorage } = require('multer-gridfs-storage');

// Set up GridFS storage engine
// TODO: Set up the environment for database name and not default test one
const url = process.env.MONGODB_URI + "test"
console.log("url "+ url)
const storage = new GridFsStorage({
  url,
  file: (req: any, file: any) => {
    return {
        bucketName: process.env.MONGODB_UPLOAD_BUCKET, 
    };
  }
});

const upload = multer({ storage });
const UploadFile = upload.single('file')
export default UploadFile