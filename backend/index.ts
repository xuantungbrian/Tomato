import express, {NextFunction, Request, Response} from 'express';
import { validationResult } from 'express-validator';
import { PostRoutes } from './routes/PostRoutes';
import connectDB  from "./db";
import morgan from 'morgan'
import verifyGoogleToken from './middleware/VerifyGoogleToken';
import UploadFile from './middleware/UploadFile';
import { FileRoutes } from './routes/FileRoutes';
import { ChatRoutes } from './routes/ChatRoutes';
import { config } from 'dotenv';
config();

const app = express();

app.use(express.json());
app.use(morgan('tiny'))
app.use(verifyGoogleToken)
app.use(UploadFile) // TODO: Add this for one route only
// TODO: Cleanup as any

app.get('/', (req: Request, res: Response) => {
    res.send('Hello World');
});

const allRoutes = [...PostRoutes, ...FileRoutes, ...ChatRoutes]
allRoutes.forEach((route) => {
    (app as any)[route.method](
        route.route,
        route.validation,
        async (req: express.Request, res: Response, next: NextFunction) => {
            const errors = validationResult(req);
            if (!errors.isEmpty()) {
                /* If there are validation errors, send a response with the error messages */
                return res.status(400).send({ errors: errors.array() });
            }
            try {
                await route.action(req, res, next);
            } catch (err) {
                console.log(err)
                return res.sendStatus(500); // Don't expose internal server workings
            }
        },
    );
});

const PORT = process.env.PORT || 3000

connectDB().then(() => {
    app.listen(PORT, () => {
        console.log(`Server is running on port ${PORT}`);
    });
}).catch((err) => {
    console.error(err);
})
