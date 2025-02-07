import { NextFunction, Request, Response } from "express";
import * as os from "os";
import { client } from "../services";
import { ObjectId } from "mongodb";

export class TodoController {
    async getTodos(req: Request, res: Response, next: NextFunction) {
        const todos = await client.db("test").collection("todolist").find().toArray();
        res.status(200).send(todos);
    };

    async postTodos(req: Request, res: Response, next: NextFunction) {
        const createData = await client.db("test").collection("todolist").insertOne(req.body);
        res.status(200).send(`Todo Item Created with id: ${createData.insertedId}`);
    }

    async putTodos(req: Request, res: Response, next: NextFunction) {
        const updateData = await client.db("test").collection("todolist").replaceOne({ _id: new ObjectId(req.params.id) }, req.body);
        if (!updateData.acknowledged || updateData.modifiedCount == 0) {
            res.status(404).send("Todo Item Not Found");
        } else {
            res.status(200).send("Todo Item Updated");
        }
    }

    async deleteTodos(req: Request, res: Response, next: NextFunction) {
        const deleteData = await client.db("test").collection("todolist").deleteOne({ _id: new ObjectId(req.params.id) });
        if (!deleteData.acknowledged || deleteData.deletedCount == 0) {
            res.status(404).send("Todo Item Not Found");
        } else {
            res.status(200).send("Todo Item Deleted");
        }
    }

    async getServerLocalTime(req: Request, res: Response, next: NextFunction) {
        const now = new Date();
        const offset = -now.getTimezoneOffset();
        const hours = Math.floor(offset / 60);
        const minutes = offset % 60;
        const formattedOffset = `GMT${hours >= 0 ? "+" : ""}${hours
            .toString()
            .padStart(2, "0")}:${minutes.toString().padStart(2, "0")}`;
        const serverTime = now.toISOString().slice(11, 19) + ` ${formattedOffset}`;
        res.status(200).send(`The server time is: ${serverTime}`);
    }

    async getFirstLastName(req: Request, res: Response, next: NextFunction) {
        res.status(200).send(`James Hong`);
    }
}