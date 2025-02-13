import { UserModel } from "../model/UserModel"

export class UserService {
    constructor() {
        // const userModel = new UserModel()
    }

    async createUser(_id: string, name: string) {
        const newUser = new UserModel({ _id, name })
        await newUser.save()
    }

    async getUser(_id: string) {
        const user = UserModel.findOne({ _id })
    }
}