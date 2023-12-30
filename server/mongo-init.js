db = db.getSiblingDB("twitter");
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
