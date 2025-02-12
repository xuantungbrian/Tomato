import express, {NextFunction, Request, Response} from 'express';
import { validationResult } from 'express-validator';
import { TodoRoutes } from './routes/TodoRoutes';
import { client } from "./services";
import morgan from 'morgan'
import { verifyGoogleToken } from './middleware/verifyGoogleToken';
import { config } from 'dotenv';

config();

const app = express();

app.use(express.json());
app.use(morgan('tiny'))
app.use(verifyGoogleToken) // If anything happens, use the TodoRoutes below

app.get('/', (req: Request, res: Response) => {
    res.send('Hello World');
});

// const errorHandler = (req: Request, res: Response) => {
//     console.error(res.status)
// }

// TodoRoutes.forEach((route) => {
//     (app as any)[route.method](
//         route.route,
//         route.validation,
//         async (req: Request, res: Response, next: NextFunction) => {
//             const errors = validationResult(req);
//             if (!errors.isEmpty()) {
//                 /* If there are validation errors, send a response with the error messages */
//                 return res.status(400).send({ errors: errors.array() });
//             }
//             try {
//                 await route.action(
//                     req,
//                     res,
//                     next,
//                 );
//             } catch (err) {
//                 console.log(err)
//                 return res.sendStatus(500); // Don't expose internal server workings
//             }
//         },
//     );
// });

const PORT = process.env.PORT || 3000;

client.connect().then(() => {
    console.log('Connected to MongoDB');

    app.listen(PORT, () => {
        console.log(`Server is running on port ${PORT}`);
    });
}).catch((err) => {
    console.error(err);
    client.close();
})
