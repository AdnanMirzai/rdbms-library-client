package se.kth.adnolle.rdbmslibraryclient.model;

import se.kth.adnolle.rdbmslibraryclient.model.exceptions.*;
import java.util.List;

/**
 * This interface declares methods for querying a Books database.
 * Different implementations of this interface handles the connection and
 * queries to a specific DBMS and database, for example a MySQL or a MongoDB
 * database.
 * <p>All methods throw specific checked exceptions (subclasses of IOException)
 * o encapsulate the underlying SQL or database errors.</p>
 *
 * @author adnolle@kth.se
 */
public interface IBooksDb {

    /**
     * Connect to the database.
     * @param database name of database to connect to
     * @return true on successful connection
     * @throws ConnectionException If a connection error occurs.
     */
    boolean connect(String database) throws ConnectionException;

    /**
     * Closes the connection to the database.
     * @throws ConnectionException If an error occurs while closing the connection.
     */
    void disconnect() throws ConnectionException;

    /**
     * Checks if there is an active connection to the database.
     * @return true if connected.
     * @throws ConnectionException If the status could not be determined.
     */
    boolean isConnected() throws ConnectionException;


    List<Book> findBooksByTitle(String title) throws SelectException;
    List<Book> findBooksByIsbn(String isbn) throws SelectException;
    List<Book> findBooksByAuthorName(String name) throws SelectException;
    List<Book> findBooksByRating(String rating) throws SelectException;
    List<Book> findBooksByGenre(String genre) throws SelectException;

    void addBook(Book book, List<Author> authors, List<Genre> genres) throws InsertException;
    void rateBook(int bookId, int rating) throws UpdateException;
    List<Author> getAllAuthors() throws SelectException;
    List<Genre> getAllGenres() throws SelectException;
}