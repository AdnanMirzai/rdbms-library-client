package se.kth.adnolle.rdbmslibraryclient.model;

import se.kth.adnolle.rdbmslibraryclient.model.exceptions.ConnectionException;
import se.kth.adnolle.rdbmslibraryclient.model.exceptions.InsertException;
import se.kth.adnolle.rdbmslibraryclient.model.exceptions.SelectException;
import se.kth.adnolle.rdbmslibraryclient.model.exceptions.UpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class DBmySQL implements IBooksDb {
    private Connection connection;

    @Override
    public boolean connect(String database) throws ConnectionException {
        String url = "jdbc:mysql://localhost:3306/" + database + "?serverTimezone=UTC";
        String user = "DB_clientApp";
        String password = "ABC.123";
        try {
            connection = DriverManager.getConnection(url, user, password);
            return true;
        } catch (SQLException e) {
            throw new ConnectionException("Could not connect to database: " + e.getMessage());
        }
    }

    //No need to check is connection is already closed because if closed, connection.close() is no-op.
    //No need to check for active transactions as we always roll back in each finally block.
    @Override
    public void disconnect() throws ConnectionException {
        if(connection != null) {
            try {
                connection.close();
            } catch(SQLException e) {
                throw new ConnectionException("Error when disconnecting: " + e.getMessage());
            }
        }
    }

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
