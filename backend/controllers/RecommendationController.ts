import { PostService } from "../service/PostService";
import { Request, Response, NextFunction } from "express";


export class RecommendationController {
    private postService: PostService;

    constructor() {
        this.postService = new PostService();
    }

    getRecommendation = async (req: Request, res: Response, next: NextFunction) => {
        const userId = (req as any).user.id
        const posts = await this.postService.getUserPost(userId, false)
        let similar_users : Array<String> = []
        let just_coords : String[] = []
        
        // User hasn't posted any, can't recommend anything
        if (posts == null || posts.length == 0){
            return res.json({coordinates: []})
        }

        posts.forEach(post => {
            let lat : number = post.latitude as number
            let long : number = post.longitude as number
            const posts_at_location = this.postService.getPostsAtLocation(lat, long)
            just_coords.push(lat.toString().concat(" ", long.toString()))

            posts_at_location.then(user_posts => {
                user_posts?.forEach(user_post => {
                    similar_users.push(user_post.userId as String)
                })
            })
        })

        let potential_places : any[] = []
        if (similar_users.length > 0) {
            for (let i = 0; i < 3 && similar_users.length > 0; i++) {
                const most_similar = this.mode(similar_users)
                const most_similar_posts = await this.postService.getUserPost(most_similar, false)
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

        while (potential_places.length > 0) {
            let best_place = this.mode(potential_places)
            best_places.push(best_place)
            potential_places = this.deleteOccurences(potential_places, best_place) as any[]
        }

        res.json({coordinates : best_places})
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