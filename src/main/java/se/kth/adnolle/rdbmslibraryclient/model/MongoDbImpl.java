package se.kth.adnolle.rdbmslibraryclient.model;

import com.mongodb.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
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
    private MongoDatabase database;
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
            database = client.getDatabase(databaseName);

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
    public boolean isConnected() throws ConnectionException { return client != null; }

    @Override
    public List<Book> findBooksByTitle(String title) throws SelectException {
        List<Book> books = new ArrayList<>();
        try {
            Bson filter = Filters.regex("title", ".*" + title + ".*", "i");
            for(Document doc : bookCollection.find(filter)) {
                books.add(convertToBook(doc));
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
            for(Document doc : bookCollection.find(filter)) {
                books.add(convertToBook(doc));
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
            for(Document doc : bookCollection.find(filter)) {
                books.add(convertToBook(doc));
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
            for(Document doc : genreCollection.find(filter)) {
                genreIds.add(doc.getInteger("_id"));
            }

            if(genreIds.isEmpty()) return books;
            Bson bookFilter = Filters.in("genreIds", genreIds);
            for(Document bookDoc : bookCollection.find(bookFilter)) {
                books.add(convertToBook(bookDoc));
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
            Bson filter = Filters.regex("name", ".*" + name + ".*", "i"); //i = case insensitive
            for(Document doc : authorCollection.find(filter)) {
                authorIds.add(doc.getInteger("_id"));
            }

            if(authorIds.isEmpty()) return books;
            Bson authorFilter = Filters.in("authorIds", authorIds);
            for(Document bookDoc : bookCollection.find(authorFilter)) {
                books.add(convertToBook(bookDoc));
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

    }

    @Override
    public List<Author> getAllAuthors() throws SelectException {
        return List.of();
    }

    @Override
    public List<Genre> getAllGenres() throws SelectException {
        return List.of();
    }

    //Helpers
    private Book convertToBook(Document doc) throws MongoException {
        int bookId = doc.getInteger("_id");
        String isbn = doc.getString("isbn");
        String title = doc.getString("title");
        Date utilDate = doc.getDate("published");
        java.sql.Date published = (utilDate != null) ? new java.sql.Date(utilDate.getTime()) : null; //TODO: change in book class later so we don't have to use sql date
        String storyLine = doc.getString("storyLine");

        int rating;
        Integer ratingVal = doc.getInteger("rating");
        if(ratingVal == null) {
            rating = 0;
        }
        else rating = ratingVal;

        List<Integer> authorIds = doc.getList("authorIds", Integer.class);
        List<Integer> genreIds = doc.getList("genreIds", Integer.class);
        List<Author> authors = getAuthorsForBook(authorIds);
        List<Genre> genres = getGenresForBook(genreIds);
        return new Book(bookId,isbn,title,published,storyLine,rating,authors,genres);
    }

    private List<Author> getAuthorsForBook(List<Integer> authorIds) throws MongoException {
        List<Author> authors = new ArrayList<>();
        if(authorIds != null && !authorIds.isEmpty()) {
            for(Document doc : authorCollection.find(Filters.in("_id", authorIds))) {
                authors.add(convertToAuthor(doc));
            }
        }
        return authors;
    }

    private Author convertToAuthor(Document doc) {
        int auId = doc.getInteger("_id");
        String name = doc.getString("name");
        Date utilDOB = doc.getDate("dob");
        java.sql.Date DOB = (utilDOB != null) ? new java.sql.Date(utilDOB.getTime()) : null; //TODO: same here
        return new Author(auId, name, DOB);
    }

    private List<Genre> getGenresForBook(List<Integer> genreIds) throws MongoException {
        List<Genre> genres = new ArrayList<>();
        if(genreIds != null && !genreIds.isEmpty()) {
            for(Document doc : genreCollection.find(Filters.in("_id", genreIds))) {
                genres.add(convertToGenre(doc));
            }
        }
        return genres;
    }

    private Genre convertToGenre(Document doc) {
        int genreId = doc.getInteger("_id");
        String genre = doc.getString("genre");
        return new Genre(genreId,genre);
    }
}