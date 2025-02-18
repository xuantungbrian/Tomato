import { UserModel } from "../model/UserModel"

export class UserService {
    async createUser(id: string, name: string) {
        try {
            const newUser = new UserModel({ _id: id, name })
            return newUser.save()
        } catch(error) {
            console.error("Error creating user:", error);
            return null
        }
    }

    async getUser(id: string) {
        try {
            return UserModel.findById(id)
        } catch(error) {
            console.error("Error getting user:", error);
            return null
        }
    }
}