mongoimport --authenticationDatabase admin --uri mongodb://mongo:mongo@localhost:27017 --db twitter --collection user --type=json --file "/data/db/user.json" --jsonArray
mongoimport --authenticationDatabase admin --uri mongodb://mongo:mongo@localhost:27017 --db twitter --collection twit --type=json --file "/data/db/twit.json" --jsonArray
