package se.kth.adnolle.rdbmslibraryclient.model;

import se.kth.adnolle.rdbmslibraryclient.model.exceptions.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLImpl implements IBooksDb {
    private Connection connection;

    @Override
    public boolean connect(String database) throws ConnectionException {
        try {
            if(connection != null && !connection.isClosed()) return true;
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        }

        String url = "jdbc:mysql://localhost:3306/" + database + "?serverTimezone=UTC";
        String user = "DB_clientApp";
        String password = "ABC.123";
        try {
            connection = DriverManager.getConnection(url, user, password);
            return true;
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
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
                throw new ConnectionException(e.getMessage());
            }
        }
    }

    public boolean isConnected() throws ConnectionException {
        if(connection != null) {
            try {
                return !connection.isClosed();
            } catch (SQLException e) {
                throw new ConnectionException();
            }
        }
        else return false;
    }

    @Override
    public List<Book> findBooksByTitle(String title) throws SelectException {
        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM T_Book WHERE title LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + title + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(convertToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new SelectException(e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> findBooksByIsbn(String isbn) throws SelectException {
        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM T_Book WHERE isbn = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(convertToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new SelectException(e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> findBooksByAuthorName(String name) throws SelectException {
        List<Book> books = new ArrayList<>();

        String sql =
                "SELECT B.* FROM T_Book AS B " +
                "JOIN T_BookAuthor AS BA ON B.bookId = BA.bookId " +
                "JOIN T_Author AS A ON BA.auId = A.auId " +
                "WHERE A.name LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(convertToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new SelectException(e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> findBooksByRating(String rating) throws SelectException {
        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM T_Book WHERE rating = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, rating);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(convertToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new SelectException(e.getMessage());
        }
        return books;
    }

    @Override
    public List<Book> findBooksByGenre(String genre) throws SelectException {
        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM T_Book WHERE rating = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, genre);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(convertToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new SelectException(e.getMessage());
        }
        return books;
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

    //Helpers
    private List<Author> getAuthorsForBook(int bookId) throws SQLException {
        List<Author> authors = new ArrayList<>();

        String sql =
                "SELECT * FROM T_Author AS A " +
                "JOIN T_BookAuthor AS BA ON A.auId = BA.auId " +
                "WHERE BA.bookId = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1,bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("auId");
                    String name = rs.getString("name");
                    Date dob = rs.getDate("DOB");
                    authors.add(new Author(id, name, dob));
                }
            }
        }
        return authors;
    }

    private List<Genre> getGenresForBook(int bookId) throws SQLException {
        List<Genre> genres = new ArrayList<>();

        String sql =
                "SELECT G.genreId, G.genre FROM T_Genre AS G " +
                "JOIN T_BookGenre AS BG ON G.genreId = BG.genreId " +
                "WHERE BG.bookId = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1,bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("genreId");
                    String genre = rs.getString("genre");
                    genres.add(new Genre(id, genre));
                }
            }
        }
        return genres;
    }

    private Book convertToBook(ResultSet rs) throws SQLException {
        int bookId = rs.getInt("bookId");
        String isbn = rs.getString("isbn");
        String title = rs.getString("title");
        Date published = rs.getDate("published");
        String storyLine = rs.getString("storyLine");
        int rating = rs.getInt("rating");
        List<Author> authors = getAuthorsForBook(bookId);
        List<Genre> genres = getGenresForBook(bookId);

        return new Book(bookId, isbn, title, published, storyLine, rating, authors, genres);
    }
}