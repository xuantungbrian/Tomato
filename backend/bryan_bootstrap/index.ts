import express, {Request, Response} from 'express';
import morgan from "morgan"
import moment from "moment"

const app = express();

app.use(express.json())
app.use(morgan("tiny"))


app.get("/serverIP", (req: Request, res: Response) => {
    try{
        var result ={
            "serverIP": "...",
        }

        res.status(200).send(result);
    }

    catch(err){
        console.error(err);
        res.status(500).send(err);
    }
})

app.get("/time", (req: Request, res: Response) => {
    try{
        const date = moment().local();
        var time = `${date.format("HH:mm:ss")} GMT${date.format("Z")}`
        var result = {
            time: `${time}`
        }
        res.status(200).send(result);

    }
    catch(err){
        console.error(err);
        res.status(500).send(err);
    }
})

app.get("/name", (req: Request, res: Response) => {
    try{
        var result = {
            "firstName": "Bryan",
            "lastName": "Tanady"
        }
        res.status(200).send(result);
    }

    catch(err) {
        console.error(err)
        res.status(500).send(err)
    }
   
})
app.listen(process.env.PORT, () => {
    console.log("Listening on PORT " + process.env.PORT);
})

