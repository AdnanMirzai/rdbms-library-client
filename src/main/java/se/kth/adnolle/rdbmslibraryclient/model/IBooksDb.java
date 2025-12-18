package se.kth.adnolle.rdbmslibraryclient.model;

import se.kth.adnolle.rdbmslibraryclient.model.exceptions.*;
import java.util.List;

/**
 * This interface declares methods for querying a Books database.
 * Different implementations of this interface handles the connection and
 * queries to a specific DBMS and database, for example a MySQL or a MongoDB
 * database.
 * NB! The methods in the implementation must catch the SQL/MongoDBExceptions thrown
 * by the underlying driver, wrap in a Connection/Insert/SelectException and re-throw the
 * latter exception. This way the interface is the same for both implementations, because the
 * exception type in the method signatures is the same. More info in the mentioned exception classes.
 *
 * @author anderslm@kth.se
 */
public interface IBooksDb {

    /**
     * Connect to the database.
     * @param database url
     * @return true on successful connection
     */
    boolean connect(String database) throws ConnectionException;

    void disconnect() throws ConnectionException;

    List<Book> findBooksByTitle(String title) throws SelectException;
    List<Book> findBooksByIsbn(String isbn) throws SelectException;
    List<Book> findBooksByAuthorName(String name) throws SelectException;
    List<Book> findBooksByRating(String rating) throws SelectException;
    List<Book> findBooksByGenre(String genre) throws SelectException;

    void addBook(Book book, List<Author> authors, List<Genre> genres) throws InsertException;
    void rateBook(int bookId, int rating) throws UpdateException;
    List<Author> getAllAuthors() throws SelectException;
    List<Genre> getAllGenres() throws SelectException;

    boolean isConnected() throws ConnectionException;

    // TODO: Add abstract methods for all inserts, deletes and queries mentioned in the assignment
}