db = db.getSiblingDB('LibraryDB');

// 1. Create Application User
db.createUser({
    user: "DB_clientApp",
    pwd: "ABC.123",
    roles: [{ role: "readWrite", db: "LibraryDB" }]
});

// 2. Create Counters Collection (Simulating AUTO_INCREMENT)
// We set 'seq' to the highest ID currently in the seed data to avoid collisions.
db.createCollection("counters");
db.counters.insertMany([
    { _id: "bookId", seq: 13 },
    { _id: "genreId", seq: 8 },
    { _id: "auId", seq: 10 }
]);

// 3. Insert Data
// Collection: genres
db.createCollection("genres");
db.genres.insertMany([
    { _id: 1, genre: "Fantasy" },
    { _id: 2, genre: "Action" },
    { _id: 3, genre: "Database" },
    { _id: 4, genre: "Fiction" },
    { _id: 5, genre: "Literary Fiction" },
    { _id: 6, genre: "Dystopian" },
    { _id: 7, genre: "Historical Fiction" },
    { _id: 8, genre: "Contemporary" }
]);

// Collection: authors
db.createCollection("authors");
db.authors.insertMany([
    { _id: 1, name: "Andrzej Sapkowski", dob: null },
    { _id: 2, name: "George R. R. Martin", dob: null },
    { _id: 3, name: "J. K. Rowling", dob: null },
    { _id: 4, name: "C. S. Lewis", dob: null },
    { _id: 5, name: "Catherine Ricardo", dob: ISODate("1999-11-11") },
    { _id: 6, name: "Susan Ullman", dob: ISODate("1999-11-11") },
    { _id: 7, name: "Kazuo Ishiguro", dob: ISODate("1999-11-11") },
    { _id: 8, name: "Margaret Atwood", dob: ISODate("1999-11-11") },
    { _id: 9, name: "Douglas Stuart", dob: ISODate("1999-11-11") },
    { _id: 10, name: "Douglas Coupland", dob: ISODate("1999-11-11") }
]);

// Collection: books
db.createCollection("books");
db.books.insertMany([
    {
        _id: 1, isbn: "1000800091730", title: "The Witcher",
        published: ISODate("1999-09-01"), storyLine: "En monsterjägare letar efter sitt öde.",
        rating: 4, authorIds: [1], genreIds: [1]
    },
    {
        _id: 2, isbn: "9780553103540", title: "A Game of Thrones",
        published: ISODate("1996-08-06"), storyLine: "Vinter kommer och alla bråkar om en stol.",
        rating: 5, authorIds: [2], genreIds: [1]
    },
    {
        _id: 3, isbn: "9780747532743", title: "Lord of the rings",
        published: ISODate("1997-06-26"), storyLine: "En ring att styra dem alla.",
        rating: 5, authorIds: [3], genreIds: [1]
    },
    {
        _id: 4, isbn: "9780064471190", title: "The Lion, the Witch and the Wardrobe",
        published: ISODate("1950-10-16"), storyLine: "Fyra barn hittar ett land i en garderob.",
        rating: 3, authorIds: [4], genreIds: [1, 2]
    },
    {
        _id: 5, isbn: "1234567890123", title: "Databases Illuminated",
        published: ISODate("2018-01-01"), storyLine: "A comprehensive guide to database systems.",
        rating: 5, authorIds: [5, 6], genreIds: [3]
    },
    {
        _id: 6, isbn: "2345678901234", title: "Dark Databases",
        published: ISODate("1990-01-01"), storyLine: "A thriller about database security.",
        rating: 1, authorIds: [5], genreIds: [3, 4]
    },
    {
        _id: 7, isbn: "4567890123456", title: "The buried giant",
        published: ISODate("2015-03-01"), storyLine: "A fantasy novel set in post-Arthurian Britain.",
        rating: null, authorIds: [7], genreIds: [4, 5]
    },
    {
        _id: 8, isbn: "5678901234567", title: "Never let me go",
        published: ISODate("2005-01-01"), storyLine: "A dystopian science fiction novel.",
        rating: null, authorIds: [7], genreIds: [5, 6]
    },
    {
        _id: 9, isbn: "6789012345678", title: "The remains of the day",
        published: ISODate("1989-05-01"), storyLine: "A story of an English butler in post-war England.",
        rating: null, authorIds: [7], genreIds: [5, 7]
    },
    {
        _id: 10, isbn: "2345678900000", title: "Alias Grace",
        published: ISODate("1996-09-01"), storyLine: "Based on true events about a convicted murderess.",
        rating: 2, authorIds: [8], genreIds: [5, 7]
    },
    {
        _id: 11, isbn: "3456789111111", title: "The handmaids tale",
        published: ISODate("1985-01-01"), storyLine: "A dystopian novel about a totalitarian society.",
        rating: 3, authorIds: [8], genreIds: [4, 6]
    },
    {
        _id: 12, isbn: "3456789012222", title: "Shuggie Bain",
        published: ISODate("2020-01-01"), storyLine: "A story of growing up in 1980s Glasgow.",
        rating: 4, authorIds: [9], genreIds: [5, 8]
    },
    {
        _id: 13, isbn: "3456789123333", title: "Microserfs",
        published: ISODate("1995-01-01"), storyLine: "Life at Microsoft in the early 1990s.",
        rating: 5, authorIds: [10], genreIds: [4, 8]
    }
]);