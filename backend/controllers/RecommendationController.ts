import { PostService } from "../service/PostService";
import { Request, Response, NextFunction } from "express";


export class RecommendationController {
    private postService: PostService;

    constructor() {
        this.postService = new PostService();
    }

    getRecommendation = async (req: Request, res: Response, next: NextFunction) => {
        const userId = (req as any).body.id
        const max = (req as any).query ? parseInt((req as any).query as string) : 10
        const posts = await this.postService.getPosts(userId)
        let similar_users : Array<String> = []
        let just_coords : String[] = []

        for (const post of posts ?? []) {
            let lat : number = post.latitude as number
            let long : number = post.longitude as number
            const posts_at_location = await this.postService.getPostsAtLocation(lat, long)
            just_coords.push(lat.toString().concat(" ", long.toString()))

            posts_at_location?.forEach(user_post => {
                if (user_post.userId != userId)
                    similar_users.push(user_post.userId as String)
            })
        }

        let potential_places : any[] = []
        if (similar_users.length > 0) {
            for (let i = 0; i < 3 && similar_users.length > 0; i++) {
                const most_similar = this.mode(similar_users)
                const most_similar_posts = await this.postService.getPosts(most_similar)
                most_similar_posts?.forEach(sim_post => {
                    if (!just_coords.includes((sim_post.latitude as Number).toString().concat(" ", (sim_post.longitude as Number).toString()))) {
                        potential_places.push((sim_post.latitude as Number).toString().concat(" ", (sim_post.longitude as Number).toString()))
                    }
                })

                similar_users = this.deleteOccurences(similar_users, most_similar) as Array<String>
            }
        }
        else {
            const every_post = await this.postService.getEveryPost()
            every_post?.forEach(all_post => {
                if (!just_coords.includes((all_post.latitude as Number).toString().concat(" ", (all_post.longitude as Number).toString()))) {
                    potential_places.push((all_post.latitude as Number).toString().concat(" ", (all_post.longitude as Number).toString()))
                }
            })
        }

        let best_places = []

        while (potential_places.length > 0 && best_places.length <= max) {
            let best_place = this.mode(potential_places)
            best_places.push(best_place)
            potential_places = this.deleteOccurences(potential_places, best_place) as any[]
        }

        let best_posts : any[] = new Array(10);
        for(let i = 0; i < max; i++) {
            let place = best_places[i]
            if (!place) {
                break;
            }
            const {latitude, longitude} = place.split(" ", 2)
            let lat = parseFloat(latitude as string) as number
            let long = parseFloat(longitude as string) as number
            let posts = await this.postService.getPostsAtLocation(lat, long) as Array<any>
            best_posts[i] = posts
        }
        res.json({posts : best_posts})
    }

    mode(arr : Array<any>) : any {
        return arr.sort((a,b) =>
              arr.filter(v => v===a).length
            - arr.filter(v => v===b).length
        ).pop();
    }

    deleteOccurences(a : Array<any>, e : any) {
        if (!a.includes(e)) {
            return -1;
        }
    
        return a.filter(
            (item) => item !== e);
    }
}