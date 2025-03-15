import { RecommendationController } from "../controllers/RecommendationController";
import {Route} from "./RouteInterface";

const recommendationController = new RecommendationController();

export const RecommendationRoutes: Route[] = [
    {
        method: "get",
        route: "/recommendations",            
        action: recommendationController.getRecommendation,
        validation: [],
        protected: true,
    }
]