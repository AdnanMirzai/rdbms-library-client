package se.kth.adnolle.rdbmslibraryclient.model;

import com.mongodb.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import com.mongodb.client.*;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;

import se.kth.adnolle.rdbmslibraryclient.model.exceptions.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoDbImpl implements IBooksDb {
    private MongoClient client;
    private MongoCollection<Document> genreCollection;
    private MongoCollection<Document> authorCollection;
    private MongoCollection<Document> bookCollection;
    private MongoCollection<Document> counterCollection;

    @Override
    public boolean connect(String databaseName) throws ConnectionException {
        if(client != null) return true;

        try{
            client = MongoClients.create(
                    MongoClientSettings.builder().
                            applyConnectionString(new ConnectionString(
                                    "mongodb://DB_clientApp:ABC.123@127.0.0.1:27017/" +
                                            databaseName + "?authSource=" + databaseName)).build());
            MongoDatabase database = client.getDatabase(databaseName);

            counterCollection = database.getCollection("counters");
            genreCollection = database.getCollection("genres");
            authorCollection = database.getCollection("authors");
            bookCollection = database.getCollection("books");
            return true;
        } catch (MongoException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    @Override
    public void disconnect() throws ConnectionException {
        if(client != null) {
            try {
                client.close();
                client = null;
            } catch(MongoException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
    }

    @Override
    public boolean isConnected() { return client != null; }

    @Override
    public List<Book> findBooksByTitle(String title) throws SelectException {
        List<Book> books = new ArrayList<>();
        try {
            Bson filter = Filters.regex("title", ".*" + title + ".*", "i");
            try(MongoCursor<Document> cursor = bookCollection.find(filter).iterator()) {
                while(cursor.hasNext()) {
                    books.add(convertToBook(cursor.next()));
                }
            }
        } catch(MongoException e) {
            throw new SelectException(e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> findBooksByIsbn(String isbn) throws SelectException {
        List<Book> books = new ArrayList<>();
        try {
            Bson filter = Filters.eq("isbn", isbn);
            try(MongoCursor<Document> cursor = bookCollection.find(filter).iterator()) {
                while(cursor.hasNext()) {
                    books.add(convertToBook(cursor.next()));
                }
            }
        } catch(MongoException e) {
            throw new SelectException(e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> findBooksByRating(String rating) throws SelectException {
        List<Book> books = new ArrayList<>();
        try {
            int ratingInt = Integer.parseInt(rating);
            Bson filter = Filters.eq("rating", ratingInt);
            try(MongoCursor<Document> cursor = bookCollection.find(filter).iterator()) {
                while(cursor.hasNext()) {
                    books.add(convertToBook(cursor.next()));
                }
            }
        } catch(NumberFormatException e) {
            return books;
        } catch(MongoException e) {
            throw new SelectException(e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> findBooksByGenre(String genre) throws SelectException {
        List<Book> books = new ArrayList<>();
        List<Integer> genreIds = new ArrayList<>();
        try {
            Bson filter = Filters.regex("genre", ".*" + genre + ".*", "i");
            try(MongoCursor<Document> cursor = genreCollection.find(filter).iterator()) {
                while(cursor.hasNext()) {
                    genreIds.add(cursor.next().getInteger("_id"));
                }
            }
            if(genreIds.isEmpty()) return books;

            Bson bookFilter = Filters.in("genreIds", genreIds);
            try(MongoCursor<Document> cursor = bookCollection.find(bookFilter).iterator()) {
                while(cursor.hasNext()) {
                    books.add(convertToBook(cursor.next()));
                }
            }
        } catch (MongoException e) {
            throw new SelectException(e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> findBooksByAuthorName(String name) throws SelectException {
        List<Book> books = new ArrayList<>();
        List<Integer> authorIds = new ArrayList<>();
        try {
            Bson auNameFilter = Filters.regex("name", ".*" + name + ".*", "i"); //i = case insensitive
            try(MongoCursor<Document> cursor = authorCollection.find(auNameFilter).iterator()) {
                while(cursor.hasNext()) {
                    authorIds.add(cursor.next().getInteger("_id"));
                }
            }
            if(authorIds.isEmpty()) return books;

            Bson BookAuthorFilter = Filters.in("authorIds", authorIds);
            try(MongoCursor<Document> cursor = bookCollection.find(BookAuthorFilter).iterator()) {
                while(cursor.hasNext()) {
                    books.add(convertToBook(cursor.next()));
                }
            }
        } catch (MongoException e) {
            throw new SelectException(e.getMessage());
        }
        return books;
    }

    @Override
    public void rateBook(int bookId, int rating) throws UpdateException {
        try {
            Bson filter = Filters.eq("_id", bookId);
            Bson update = Updates.set("rating", rating);
            UpdateResult result = bookCollection.updateOne(filter,update);
            if(result.getMatchedCount() == 0) throw new UpdateException("Book does not exist!" + bookId);
        } catch (MongoException e) {
            throw new UpdateException(e.getMessage());
        }
    }

    @Override
    public void addBook(Book book, List<Author> authors, List<Genre> genres) throws InsertException {
        int bookId = getNextId("bookId");
        List<Integer> authorIds = new ArrayList<>();
        List<Integer> genreIds = new ArrayList<>();

        if(authors != null && !authors.isEmpty()) {
            for(Author author : authors) {
                authorIds.add(author.getAuId());
            }
        }
        if(genres != null && !genres.isEmpty()) {
            for(Genre genre : genres) {
                genreIds.add(genre.getGenreId());
            }
        }

        Document newBook = new Document("_id", bookId).append("isbn", book.getIsbn()).append("title", book.getTitle())
                .append("published", book.getPublished()).append("storyLine", book.getStoryLine())
                .append("rating", book.getRating()).append("authorIds", authorIds).append("genreIds", genreIds);

        try {
            bookCollection.insertOne(newBook);
        } catch (MongoException e) {
            throw new InsertException(e.getMessage());
        }
    }

    @Override
    public List<Author> getAllAuthors() throws SelectException {
        List<Author> authors = new ArrayList<>();
        try {
            try(MongoCursor<Document> cursor = authorCollection.find().iterator()) {
                while(cursor.hasNext()) {
                    authors.add(convertToAuthor(cursor.next()));
                }
            }
        } catch(MongoException e) {
            throw new SelectException(e.getMessage());
        }
        return authors;
    }

    @Override
    public List<Genre> getAllGenres() throws SelectException {
        List<Genre> genres = new ArrayList<>();
        try {
            try(MongoCursor<Document> cursor = genreCollection.find().iterator()) {
                while(cursor.hasNext()) {
                    genres.add(convertToGenre(cursor.next()));
                }
            }
        } catch (MongoException e) {
            throw new SelectException(e.getMessage());
        }
        return genres;
    }

    //Helpers
    /**
     * Handels auto-increment by atomically updating a counter document.
     * @param seqName The name of the sequence to increment.
     * @return The next ID for the sequence.
     * @throws MongoException If the document cannot be found or updated.
     */
    private int getNextId(String seqName) throws MongoException {
        Document doc = counterCollection.findOneAndUpdate(
                Filters.eq("_id", seqName),
                Updates.inc("seq",1),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
        if(doc != null) {
            return doc.getInteger("seq");
        } else throw new MongoException("Unable to get next ID for sequence: " + seqName);
    }

    /**
     * Converts MongoDB BSON Document into a Book object.
     * @param doc BSON Document from books collection.
     * @return Book object.
     * @throws MongoException If needed fields are missing.
     */
    private Book convertToBook(Document doc) throws MongoException {
        int bookId = doc.getInteger("_id");
        String isbn = doc.getString("isbn");
        String title = doc.getString("title");
        Date utilDate = doc.getDate("published");
        java.sql.Date published = (utilDate != null) ? new java.sql.Date(utilDate.getTime()) : null;
        String storyLine = doc.getString("storyLine");
        Integer rating = doc.getInteger("rating");

        List<Integer> authorIds = doc.getList("authorIds", Integer.class);
        List<Integer> genreIds = doc.getList("genreIds", Integer.class);
        List<Author> authors = getAuthorsForBook(authorIds);
        List<Genre> genres = getGenresForBook(genreIds);
        return new Book(bookId,isbn,title,published,storyLine,rating,authors,genres);
    }

    /**
     * @param authorIds A list of integer author IDs.
     * @return A list of Author objects.
     * @throws MongoException If the database query fails.
     */
    private List<Author> getAuthorsForBook(List<Integer> authorIds) throws MongoException {
        List<Author> authors = new ArrayList<>();
        if(authorIds != null && !authorIds.isEmpty()) {
            try(MongoCursor<Document> cursor = authorCollection.find(Filters.in("_id", authorIds)).iterator()) {
                while(cursor.hasNext()) {
                    authors.add(convertToAuthor(cursor.next()));
                }
            }
        }
        return authors;
    }

    /**
     * @param doc BSON Document from authors collection.
     * @return An Author object.
     */
    private Author convertToAuthor(Document doc) {
        int auId = doc.getInteger("_id");
        String name = doc.getString("name");
        Date utilDOB = doc.getDate("dob");
        java.sql.Date DOB = (utilDOB != null) ? new java.sql.Date(utilDOB.getTime()) : null;
        return new Author(auId, name, DOB);
    }

    /**
     * @param genreIds A list of integer genre IDs.
     * @return A list of Genre objects.
     * @throws MongoException If the database query fails.
     */
    private List<Genre> getGenresForBook(List<Integer> genreIds) throws MongoException {
        List<Genre> genres = new ArrayList<>();
        if(genreIds != null && !genreIds.isEmpty()) {
            try(MongoCursor<Document> cursor = genreCollection.find(Filters.in("_id", genreIds)).iterator()) {
                while(cursor.hasNext()) {
                    genres.add(convertToGenre(cursor.next()));
                }
            }
        }
        return genres;
    }

    /**
     * @param doc BSON Document from genres collection.
     * @return A Genre object.
     */
    private Genre convertToGenre(Document doc) {
        int genreId = doc.getInteger("_id");
        String genre = doc.getString("genre");
        return new Genre(genreId,genre);
    }
}