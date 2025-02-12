const { OAuth2Client } = require('google-auth-library');
const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

const verifyGoogleToken = async (req, res, next) => {
    try {
        const token = req.header('Authorization');

        if (!token) {
            return res.status(401).json({ message: 'No token provided' });
        }

        const ticket = await client.verifyIdToken({
            idToken: token.replace('Bearer ', ''),
            audience: process.env.GOOGLE_CLIENT_ID,
        });

        const payload = ticket.getPayload();
        req.user = {
            googleId: payload.sub,
            email: payload.email,
            name: payload.name,
            picture: payload.picture,
        };
        // TODO: Add user to database if not exists

        next();
    } catch (error) {
        console.error('Google token verification failed:', error);
        res.status(403).json({ message: 'Invalid token' });
    }
};

module.exports = verifyGoogleToken;
