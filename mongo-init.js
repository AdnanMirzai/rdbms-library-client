db = db.getSiblingDB('LibraryDB');


db.createRole({
    role: "clientAppRole",
    privileges: [
        // BOOKS: Full control (Read, Add, Rate/Update, Delete)
        {
            resource: { db: "LibraryDB", collection: "books" },
            actions: ["find", "insert", "update", "remove"]
        },
        // REVIEWS: Full control
        {
            resource: { db: "LibraryDB", collection: "reviews" },
            actions: ["find", "insert", "update", "remove"]
        },
        // AUTHORS: Read and Add (No delete/update implemented in UI)
        {
            resource: { db: "LibraryDB", collection: "authors" },
            actions: ["find", "insert"]
        },
        // GENRES: Read only (Client selects from list, never creates)
        {
            resource: { db: "LibraryDB", collection: "genres" },
            actions: ["find"]
        },
        // USERS: Read only (For login check) - CLIENT CANNOT EDIT USERS
        {
            resource: { db: "LibraryDB", collection: "users" },
            actions: ["find"]
        },
        // COUNTERS: Read and Update (for auto-increment) - No Delete/Insert
        {
            resource: { db: "LibraryDB", collection: "counters" },
            actions: ["find", "update"]
        }
    ],
    roles: []
});

//client user
db.createUser({
    user: "DB_clientApp",
    pwd: "ABC.123",
    roles: [{ role: "clientAppRole", db: "LibraryDB" }]
});

// 2. Create Counters
db.createCollection("counters");
db.counters.insertMany([
    { _id: "bookId", seq: 13 },
    { _id: "genreId", seq: 8 },
    { _id: "auId", seq: 10 },
    { _id: "userId", seq: 3 } // New counter for users
]);

// 3. Create Users (Actual App Users)
db.createCollection("users");
db.users.insertMany([
    { _id: 1, username: "admin", password: "secret" },
    { _id: 2, username: "booklover99", password: "password" },
    { _id: 3, username: "student_bob", password: "12345" }
]);

// 4. Create Reviews Collection
// We create a Compound Index to ensure ONE review per user per book
db.createCollection("reviews");
db.reviews.createIndex({ bookId: 1, userId: 1 }, { unique: true });

// Seed reviews (Matches the SQL data)
db.reviews.insertMany([
    { bookId: 1, userId: 2, rating: 4, reviewText: "Great world building, but slow start.", reviewDate: ISODate("2023-01-15") },
    { bookId: 2, userId: 2, rating: 5, reviewText: "Cannot put it down!", reviewDate: ISODate("2023-01-20") },
    { bookId: 6, userId: 3, rating: 1, reviewText: "Too scary for me.", reviewDate: ISODate("2023-02-10") }
]);

// 5. Genres
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

// 6. Authors (Added 'addedBy' field pointing to admin)
db.createCollection("authors");
db.authors.insertMany([
    { _id: 1, name: "Andrzej Sapkowski", dob: null, addedBy: 1 },
    { _id: 2, name: "George R. R. Martin", dob: null, addedBy: 1 },
    { _id: 3, name: "J. K. Rowling", dob: null, addedBy: 1 },
    { _id: 4, name: "C. S. Lewis", dob: null, addedBy: 1 },
    { _id: 5, name: "Catherine Ricardo", dob: ISODate("1999-11-11"), addedBy: 1 },
    { _id: 6, name: "Susan Ullman", dob: ISODate("1999-11-11"), addedBy: 1 },
    { _id: 7, name: "Kazuo Ishiguro", dob: ISODate("1999-11-11"), addedBy: 1 },
    { _id: 8, name: "Margaret Atwood", dob: ISODate("1999-11-11"), addedBy: 1 },
    { _id: 9, name: "Douglas Stuart", dob: ISODate("1999-11-11"), addedBy: 1 },
    { _id: 10, name: "Douglas Coupland", dob: ISODate("1999-11-11"), addedBy: 1 }
]);

// 7. Books
// Added 'addedBy': 1
// Kept 'rating' as a CACHED field (calculated from reviews).
// e.g. Book 1 has one review of 4, so rating is 4.
// e.g. Book 6 has one review of 1, so rating is 1.
db.createCollection("books");
db.books.insertMany([
    {
        _id: 1, isbn: "1000800091730", title: "The Witcher",
        published: ISODate("1999-09-01"), storyLine: "En monsterjägare letar efter sitt öde.",
        rating: 4, authorIds: [1], genreIds: [1], addedBy: 1
    },
    {
        _id: 2, isbn: "9780553103540", title: "A Game of Thrones",
        published: ISODate("1996-08-06"), storyLine: "Vinter kommer och alla bråkar om en stol.",
        rating: 5, authorIds: [2], genreIds: [1], addedBy: 1
    },
    {
        _id: 3, isbn: "9780747532743", title: "Lord of the rings",
        published: ISODate("1997-06-26"), storyLine: "En ring att styra dem alla.",
        rating: 5, authorIds: [3], genreIds: [1], addedBy: 1
    },
    {
        _id: 4, isbn: "9780064471190", title: "The Lion, the Witch and the Wardrobe",
        published: ISODate("1950-10-16"), storyLine: "Fyra barn hittar ett land i en garderob.",
        rating: 3, authorIds: [4], genreIds: [1, 2], addedBy: 1
    },
    {
        _id: 5, isbn: "1234567890123", title: "Databases Illuminated",
        published: ISODate("2018-01-01"), storyLine: "A comprehensive guide to database systems.",
        rating: 5, authorIds: [5, 6], genreIds: [3], addedBy: 1
    },
    {
        _id: 6, isbn: "2345678901234", title: "Dark Databases",
        published: ISODate("1990-01-01"), storyLine: "A thriller about database security.",
        rating: 1, authorIds: [5], genreIds: [3, 4], addedBy: 1
    },
    {
        _id: 7, isbn: "4567890123456", title: "The buried giant",
        published: ISODate("2015-03-01"), storyLine: "A fantasy novel set in post-Arthurian Britain.",
        rating: null, authorIds: [7], genreIds: [4, 5], addedBy: 1
    },
    {
        _id: 8, isbn: "5678901234567", title: "Never let me go",
        published: ISODate("2005-01-01"), storyLine: "A dystopian science fiction novel.",
        rating: null, authorIds: [7], genreIds: [5, 6], addedBy: 1
    },
    {
        _id: 9, isbn: "6789012345678", title: "The remains of the day",
        published: ISODate("1989-05-01"), storyLine: "A story of an English butler in post-war England.",
        rating: null, authorIds: [7], genreIds: [5, 7], addedBy: 1
    },
    {
        _id: 10, isbn: "2345678900000", title: "Alias Grace",
        published: ISODate("1996-09-01"), storyLine: "Based on true events about a convicted murderess.",
        rating: 2, authorIds: [8], genreIds: [5, 7], addedBy: 1
    },
    {
        _id: 11, isbn: "3456789111111", title: "The handmaids tale",
        published: ISODate("1985-01-01"), storyLine: "A dystopian novel about a totalitarian society.",
        rating: 3, authorIds: [8], genreIds: [4, 6], addedBy: 1
    },
    {
        _id: 12, isbn: "3456789012222", title: "Shuggie Bain",
        published: ISODate("2020-01-01"), storyLine: "A story of growing up in 1980s Glasgow.",
        rating: 4, authorIds: [9], genreIds: [5, 8], addedBy: 1
    },
    {
        _id: 13, isbn: "3456789123333", title: "Microserfs",
        published: ISODate("1995-01-01"), storyLine: "Life at Microsoft in the early 1990s.",
        rating: 5, authorIds: [10], genreIds: [4, 8], addedBy: 1
    }
]);