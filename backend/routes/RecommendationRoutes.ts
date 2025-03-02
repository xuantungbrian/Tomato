import { RecommendationController } from "../controllers/RecommendationController";

const recommendationController = new RecommendationController();

export const RecommendationRoutes = [
    {
        method: "get",
        route: "/recommendations",            
        action: recommendationController.getRecommendation,
        validation: [],
        protected: true,
    }
]