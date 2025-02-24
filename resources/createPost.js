// Connect to the database
const { MongoClient } = require("mongodb");

async function run() {
  const client = new MongoClient("mongodb://127.0.0.1:27017"); // adjust connection string as needed
  try {
    await client.connect();
    const db = client.db("test"); // Use the 'test' database

    // Sample posts to insert
    const samplePosts = [
      {
        latitude: 40.7128,
        longitude: -74.0060,
        userId: "user1",
        date: new Date("2023-01-01T00:00:00Z"),
        note: "Visited the Statue of Liberty!",
        private: false
      },
      {
        latitude: 34.0522,
        longitude: -118.2437,
        userId: "user2",
        date: new Date("2023-02-14T00:00:00Z"),
        note: "Saw the Hollywood Sign!",
        private: true
      },
      {
        latitude: 51.5074,
        longitude: -0.1278,
        userId: "user3",
        date: new Date("2023-03-01T00:00:00Z"),
        note: "Explored London!",
        private: false
      },
      {
        latitude: 48.8566,
        longitude: 2.3522,
        userId: "user1",
        date: new Date("2023-04-10T00:00:00Z"),
        note: "Visited the Eiffel Tower!",
        private: true
      }
    ];

    // Insert the sample posts into the "posts" collection
    const result = await db.collection("posts").insertMany(samplePosts);
    console.log(`${result.insertedCount} posts inserted`);
  } finally {
    await client.close();
  }
}

run().catch(console.error);
