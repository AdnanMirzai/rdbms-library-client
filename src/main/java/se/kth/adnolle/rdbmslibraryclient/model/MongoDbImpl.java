package se.kth.adnolle.rdbmslibraryclient.model;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import org.bson.Document;
import com.mongodb.client.*;
import com.mongodb.client.MongoDatabase;
import se.kth.adnolle.rdbmslibraryclient.model.exceptions.*;

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
                                    "mongodb://DB_clientApp:ABC.123@localhost:27017/" +
                                            databaseName + "?authSource=" + databaseName)).build());
            database = client.getDatabase("LibraryDB");

            counterCollection = database.getCollection("counter");
            genreCollection = database.getCollection("genres");
            authorCollection = database.getCollection("authors");
            bookCollection = database.getCollection("books");
            return true;
        } catch (Exception e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    @Override
    public void disconnect() throws ConnectionException {
        if(client != null) {
            try {
                client.close();
                client = null;
            } catch(Exception e) {
                throw new ConnectionException(e.getMessage());
            }
        }
    }

    @Override
    public boolean isConnected() throws ConnectionException { return client != null; }

    @Override
    public List<Book> findBooksByTitle(String title) throws SelectException {
        return List.of();
    }

    @Override
    public List<Book> findBooksByIsbn(String isbn) throws SelectException {
        return List.of();
    }

    @Override
    public List<Book> findBooksByAuthorName(String name) throws SelectException {
        return List.of();
    }

    @Override
    public List<Book> findBooksByRating(String rating) throws SelectException {
        return List.of();
    }

    @Override
    public List<Book> findBooksByGenre(String genre) throws SelectException {
        return List.of();
    }

    @Override
    public void addBook(Book book, List<Author> authors, List<Genre> genres) throws InsertException {

    }

    @Override
    public void rateBook(int bookId, int rating) throws UpdateException {

    }

    @Override
    public List<Author> getAllAuthors() throws SelectException {
        return List.of();
    }

    @Override
    public List<Genre> getAllGenres() throws SelectException {
        return List.of();
    }
}