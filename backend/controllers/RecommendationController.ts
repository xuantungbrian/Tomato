import { AuthenticatedRequest } from "..";
import { Post, PostService } from "../service/PostService";
import { Response} from "express"; 

export class RecommendationController {
    private postService: PostService;


    constructor() {
        this.postService = new PostService();
    }

    getRecommendation = async (req: AuthenticatedRequest, res: Response) => {
        const userId : string = req.user.id
        if (!userId) {
            res.status(401).send({message: "Unauthorized"})
            return;
        }
        const max = !isNaN((req as any).query.max) ? parseInt((req as any).query.max as string) : 10
        const posts = await this.postService.getUserPost(userId, true)
        let similar_users : Array<String> = []
        let just_coords : String[] = []
        
        if (posts) {
            for (const post of posts) {
                let lat : number = post.latitude as number
                let long : number = post.longitude as number
                const posts_at_location = await this.postService.getPostsAtLocation(lat, long, true)
                just_coords.push(lat.toString().concat(" ", long.toString()))

                if (posts_at_location) {
                    posts_at_location.forEach(user_post => {
                        if (user_post.userId != userId)
                            similar_users.push(user_post.userId as String)
                    })
                }
            }
        }
        let potential_places : string[] = []
        console.log("SIMILAR USERS: ", similar_users.length)
        if (similar_users.length > 0) {
            for (let i = 0; i < 3 && similar_users.length > 0; i++) {
                const most_similar = this.mode(similar_users)
                const most_similar_posts = await this.postService.getUserPost(most_similar, false)
                if (most_similar_posts) {
                    most_similar_posts.forEach(sim_post => {
                        if (!just_coords.includes((sim_post.latitude as Number).toString().concat(" ", (sim_post.longitude as Number).toString()))) {
                            potential_places.push((sim_post.latitude as Number).toString().concat(" ", (sim_post.longitude as Number).toString()))
                        }
                    })
                }

                // similar_users = this.deleteOccurences(similar_users, most_similar) as Array<String>
                similar_users = this.deleteOccurences(similar_users, most_similar) as string[]
            }
        }
        else {
            const every_post = await this.postService.getEveryPost()
            if (!every_post || every_post.length == 0) {
                return res.json(null);
            }
            every_post.forEach(all_post => {
                let lati : number = all_post.latitude as number
                let longi : number = all_post.longitude as number
                const curr_coord = lati.toString().concat(" ", longi.toString())
                if (!just_coords.includes(curr_coord)) {
                    potential_places.push(curr_coord)
                }
            })
        }

        let best_places = []
        console.log("POTENTIAL PLACES: ", potential_places.length)

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
        console.log("BEST POSTS: ", best_posts.length)
        return res.json({posts: best_posts})
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