import { AuthenticatedRequest, isAuthenticatedRequest } from "..";
import {Post } from "../model/PostModel";
import { PostService } from "../service/PostService";
import { Response, Request } from "express";

export class RecommendationController {
    private postService: PostService;


    constructor() {
        this.postService = new PostService();
    }

    getRecommendation = async (req: Request, res: Response) => {
        if (!isAuthenticatedRequest(req)) {
            res.status(401).json({ message: "Unauthorized" });
            return;
        }

        const userId : string = req.user.id
        const max : number = !isNaN(Number(req.query.max)) ? parseInt(req.query.max as string, 10) : 10
        const posts : Post[] = (await this.postService.getUserPost(userId, true)) as Post[]
        // let similar_users : Array<String> = []
        let similar_users : string[] = []
        let just_coords : string[] = []
        for (const post of posts) {
            let lat : number = post.latitude ? post.latitude : 0
            let long : number = post.longitude ? post.longitude : 0
            const posts_at_location : Post[] = await this.postService.getPostsAtLocation(lat, long, true) as Post[]
            just_coords.push(lat.toString().concat(" ", long.toString()))

            posts_at_location?.forEach((user_post) => {
                if (user_post.userId != userId)
                    similar_users.push(user_post.userId as string)
            })
        }
        let potential_places : string[] = []
        if (similar_users.length > 0) {
            for (let i = 0; i < 3 && similar_users.length > 0; i++) {
                const most_similar : string = this.mode(similar_users) // userId
                const most_similar_posts : Post[] = await this.postService.getUserPost(most_similar, true) as Post[]
                most_similar_posts?.forEach(sim_post => {
                    if (!just_coords.includes((sim_post.latitude as Number).toString().concat(" ", (sim_post.longitude as Number).toString()))) {
                        potential_places.push((sim_post.latitude as Number).toString().concat(" ", (sim_post.longitude as Number).toString()))
                    }
                })

                // similar_users = this.deleteOccurences(similar_users, most_similar) as Array<String>
                similar_users = this.deleteOccurences(similar_users, most_similar) as string[]
            }
        }
        else {
            const every_post = await this.postService.getEveryPost()

            every_post?.forEach(all_post => {
                let lati : number = all_post.latitude as number ? all_post.latitude as number : 0
                let longi : number = all_post.longitude as number ? all_post.longitude as number : 0
                const curr_coord : string = lati.toString().concat(" ", longi.toString())
                if (!just_coords.includes(curr_coord)) {
                    potential_places.push(curr_coord)
                }
            })
        }

        let best_places = []

        while (potential_places.length > 0 && best_places.length <= max) {
            let best_place : string = this.mode(potential_places)
            best_places.push(best_place)
            potential_places = this.deleteOccurences(potential_places, best_place) as any[]
        }

        let best_posts : Post[] = []
        for(let i = 0; i < max; i++) {
            let place : string = best_places[i]
            if (!place) {
                break;
            }
            let lat : number = parseFloat(place.split(" ", 2)[0] as string) as number
            let long : number = parseFloat(place.split(" ", 2)[1] as string) as number
            let posts : Post[] = await this.postService.getPostsAtLocation(lat, long, false) as Post[]
            for(let post of posts){
                best_posts.push(post)
            }
        }
        res.json({posts: best_posts})
    }

    mode(arr : string[]) : string {
        return arr.sort((a,b) =>
              arr.filter(v => v===a).length
            - arr.filter(v => v===b).length
        ).pop() || '';
    }

    deleteOccurences(a : string[], e : string) : string[] | -1 {
        if (!a.includes(e)) {
            return -1;
        }
    
        return a.filter(
            (item) => item !== e);
    }
}