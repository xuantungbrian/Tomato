# M4 - Reflections

## 1. Development Information
1. Public IP of backend server & domain:
* ```3.147.101.160```
* ```ec2-3-147-101-160.us-east-2.compute.amazonaws.com```

2. Complexity

    Complexity is found in the "Get Location Recommendations" use case. It is done by finding "similar" users posting in your area, then finding the most "popular" areas from these similar users and sorting them before passing them onto the user. Updated detailed implementation is in 4.8 of M3_Requirements_and_Design

3. Commit Hash of MVP version in Git: 

    Short: ``` 6598bba  ```

    Full SHA: ``` 6598bba1112faeae9ede6f1162e906e525455f93 ```

## 2. AI Reflections
AI Technology was used in the creation of the MVP.

1. Deepseek, ChatGPT, GitHub Copilot

2. These technologies would be used largely as a more specific search engine for documentation, as it is able to find appropriate methods from the documentation for a described use case. It is also used to generate boilerplate code instead of memorising or going to the documentation page to regenerate it. Lastly, it would also be used to debug specific problems as search engines would generally not be able to answer questions of the correct specificity.

3. AI is advantageous as it can tailor responses very specifically to the user's demands, as compared to googling an issue where you may find similar but not necessarily same conditions or issues.

    Some positive experiences were when the full stack used, the steps taken and the problem were described, then the AI technology was able to articulate what the exact problem was, and prescribe a solution based on the correct stack.

4. AI is disavantageous as it is very difficult to debug if it is only mostly* correct, as a lot of the code generated will not be very modular (i.e. cannot be taken as a part rather than a whole) and the user will not understand the rationale behind certain parts of the code. It also struggles with deployment steps as those are elements that are very subject to change and the data/methods used may be outdated.

    Salient examples include code that uses depreciated methods only, so the code has to be rewritten to fit modern frameworks.

## 3. Contributions of each team member
- Rumbi Chinamo: Chat Backend, Recommendation algorithm, Documentation - 25 hours
- Bryan Tanady: Handled all frontEnd except chats, notification, location autocomplete input. Manage the integration between frontEnd and backEnd for all features, except chat and notification. Extend backend functionality (separate route for public and authenticated user route, separate Google Sign In and JWT Token Verification). Estimated time spent: 60 hours 
- Xuan Tung Luu: Chat, notification, location autocomplete, posts related feature - 30 hours
- James: Backend Development, Backend Deployment, Documentation - 15 hours 
