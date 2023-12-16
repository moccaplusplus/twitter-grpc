db = db.getSiblingDB("twitter");
db.createCollection("user");
db.createCollection("twit");
db.createUser(
  {
    user: "twitter",
    pwd: "twitter",
    roles: [
      {
        role: "readWrite",
        db: "twitter"
      }
    ]
  }
);
