package se.kth.adnolle.rdbmslibraryclient.model;

import se.kth.adnolle.rdbmslibraryclient.model.exceptions.*;

import java.util.List;

/**
 * This interface declares methods for querying, adding to or updating a Books database.
 * Different implementations of this interface handles the connection and
 * queries to a specific DBMS and database, for example a MySQL or a MongoDB database.
 * <p>All methods throw specific exceptions which
 * encapsulate the underlying database errors.</p>
 *
 * @author adnolle@kth.se
 */
public interface IBooksDb {

    List<Review> getReviewsForBook(int bookId) throws SelectException;

    /**
     * Connect to the database.
     *
     * @param database name of database to connect to
     * @return true on successful connection
     * @throws ConnectionException If a connection error occurs.
     */
    boolean connect(String database, String username, String password) throws ConnectionException;

    User login(String username, String password) throws LoginException;

    /**
     * Closes the connection to the database.
     *
     * @throws ConnectionException If an error occurs while closing the connection.
     */
    void disconnect() throws ConnectionException;

    /**
     * Checks if there is an active connection to the database.
     *
     * @return true if connected.
     * @throws ConnectionException If the status could not be determined.
     */
    boolean isConnected() throws ConnectionException;

    /**
     * Searches for all books if the search string is empty
     *
     * @return All books
     * @throws SelectException
     */
    List<Book> getAllBooks() throws SelectException;


    /**
     * Searches for books matching the given title.
     * Partial matches are supported.
     *
     * @param title The title to search for.
     * @return A list of matching Book objects, or an empty list if no matches found.
     * @throws SelectException If an error occurs during the database query.
     */
    List<Book> findBooksByTitle(String title) throws SelectException;

    /**
     * Searches for books matching the given isbn.
     *
     * @param isbn to search for. Exact isbn.
     * @return A list of matching Book objects, or an empty list if no matches found.
     * @throws SelectException If an error occurs during the database query.
     */
    List<Book> findBooksByIsbn(String isbn) throws SelectException;

    /**
     * Searches for books matching the given Author name.
     * Partial matches are supported.
     *
     * @param name The Author name to search for.
     * @return A list of matching Book objects, or an empty list if no matches found.
     * @throws SelectException If an error occurs during the database query.
     */
    List<Book> findBooksByAuthorName(String name) throws SelectException;

    /**
     * Searches for books matching the given rating.
     *
     * @param rating to search for. Exact rating.
     * @return A list of matching Book objects, or an empty list if no matches found.
     * @throws SelectException If an error occurs during the database query.
     */
    List<Book> findBooksByRating(String rating) throws SelectException;

    /**
     * Searches for books matching the given genre.
     * Partial matches are supported.
     *
     * @param genre The genre to search for.
     * @return A list of matching Book objects, or an empty list if no matches found.
     * @throws SelectException If an error occurs during the database query.
     */
    List<Book> findBooksByGenre(String genre) throws SelectException;

    /**
     * Adds a new book along with its relations BookAuthors and BookGenres to the database.
     * Transaction is used in this method, if any part fails, the entire operation is rolled back.
     *
     * @param book    The Book object containing info about book.
     * @param authors A list of Author objects associated with the book.
     * @param genres  A list of Genre objects associated with the book.
     * @throws InsertException If the book could not be added.
     */
    void addBook(Book book, List<Author> authors, List<Genre> genres, int addedBy) throws InsertException;

    /**
     * Updates the rating for a specific book.
     *
     * @param bookId The unique ID of the book to rate.
     * @param rating The new rating (1-5).
     * @throws UpdateException If the update fails.
     */
    void reviewBook(int bookId, int rating, String reviewText, User user) throws UpdateException;

    /**
     * Retrieves all available authors from the database.
     *
     * @return A list of all Author objects.
     * @throws SelectException If an error occurs during retrieval.
     */
    List<Author> getAllAuthors() throws SelectException;

    /**
     * Retrieves all available genres from the database.
     *
     * @return A list of all Genre objects.
     * @throws SelectException If an error occurs during retrieval.
     */
    List<Genre> getAllGenres() throws SelectException;

    void deleteBook(int bookId) throws DeleteException;

    void addAuthor(Author author, int addedBy) throws InsertException;
}