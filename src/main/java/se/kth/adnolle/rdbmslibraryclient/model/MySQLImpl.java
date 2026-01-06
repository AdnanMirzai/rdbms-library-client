package se.kth.adnolle.rdbmslibraryclient.model;

import se.kth.adnolle.rdbmslibraryclient.model.exceptions.*;
import java.sql.*;
import java.time.LocalDate;
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

        String url = "jdbc:mysql://localhost:3306/" + database + "?UseClientEnc=UTF8";
        String user = "DB_clientApp";
        String password = "ABC.123";
        try {
            connection = DriverManager.getConnection(url, user, password);
            return true;
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

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

    @Override
    public boolean isConnected() throws ConnectionException {
        if(connection != null) {
            try {
                return !connection.isClosed();
            } catch (SQLException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
        else return false;
    }

    @Override
    public List<Book> getAllBooks() throws SelectException {
        return executeQuery("SELECT * FROM T_Book WHERE title LIKE ?", "%");
    }


    @Override
    public List<Book> findBooksByTitle(String title) throws SelectException {
        return executeQuery("SELECT * FROM T_Book WHERE title LIKE ?", "%" + title + "%");
    }

    @Override
    public List<Book> findBooksByIsbn(String isbn) throws SelectException {
        return executeQuery("SELECT * FROM T_Book WHERE isbn = ?", isbn);
    }

    @Override
    public List<Book> findBooksByAuthorName(String name) throws SelectException {
        String sql = "SELECT B.* FROM T_Book AS B " +
                "JOIN T_BookAuthor AS BA ON B.bookId = BA.bookId " +
                "JOIN T_Author AS A ON BA.auId = A.auId " +
                "WHERE A.name LIKE ?";
        return executeQuery(sql, "%" + name + "%");
    }

    @Override
    public List<Book> findBooksByRating(String rating) throws SelectException {
        return executeQuery("SELECT * FROM T_Book WHERE rating = ?", rating);
    }

    @Override
    public List<Book> findBooksByGenre(String genre) throws SelectException {
        String sql = "SELECT B.* FROM T_Book AS B " +
                "JOIN T_BookGenre AS BG ON B.bookId = BG.bookId " +
                "JOIN T_Genre AS G ON BG.genreId = G.genreId " +
                "WHERE G.genre LIKE ?";
        return executeQuery(sql, "%" + genre + "%");
    }

    @Override
    public void addBook(Book book, List<Author> authors, List<Genre> genres) throws InsertException {
        String insertBook = "INSERT INTO T_Book (isbn, title, published, storyLine, rating) VALUES (?,?,?,?,?)";
        int bookId = -1;
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(insertBook, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, book.getIsbn());
                stmt.setString(2, book.getTitle());
                stmt.setDate(3, book.getPublished());
                if (book.getStoryLine() != null && !book.getStoryLine().isEmpty())
                    stmt.setString(4, book.getStoryLine());
                else stmt.setNull(4, Types.VARCHAR);

                if (book.getRating() != 0) stmt.setInt(5, book.getRating());
                else stmt.setNull(5, Types.NULL);

                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                while (rs.next()) {
                    bookId = rs.getInt(1);
                }
            }
            addBookAuthor(authors, bookId);
            addBookGenre(genres, bookId);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch(SQLException re) {
                throw new InsertException(re.getMessage());
            }
            throw new InsertException(e.getMessage());
        }
        finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ae) {
                System.err.println("In finally block, could not setAutoCommit to true!" + ae.getMessage());
            }
        }
    }

    @Override
    public void reviewBook(int bookId, int rating) throws UpdateException {
        String sql = "UPDATE T_Book SET rating = ? WHERE bookId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rating);
            stmt.setInt(2,bookId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new UpdateException(e.getMessage());
        }
    }

    @Override
    public List<Author> getAllAuthors() throws SelectException {
        List<Author> authors = new ArrayList<>();

        String sql = "SELECT * FROM T_Author";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                int auId = rs.getInt("auId");
                String name = rs.getString("name");
                Date DOB = rs.getDate("dob");
                authors.add(new Author(auId, name, DOB));
            }
        } catch (SQLException e) {
            throw new SelectException(e.getMessage());
        }
        return authors;
    }

    @Override
    public List<Genre> getAllGenres() throws SelectException {
        List<Genre> genres = new ArrayList<>();

        String sql = "SELECT * FROM T_Genre";
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                int genreId = rs.getInt("genreId");
                String genre = rs.getString("genre");
                genres.add(new Genre(genreId, genre));
            }
        } catch (SQLException e) {
            throw new SelectException(e.getMessage());
        }
        return genres;
    }

    //Private helper methods

    /**
     * Helper method to get all authors for a book.
     * Executes a JOIN query between T_Author and table T_BookAuthor.
     * @param bookId The unique ID of the book.
     * @return A list of Author objects associated with the book.
     * @throws SQLException If the SQL query fails.
     */
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

    /**
     * Helper method to get all genres for a book.
     * Executes a JOIN query between T_Genre and table T_BookGenre.
     * @param bookId The unique ID of the book.
     * @return A list of Author objects associated with the book.
     * @throws SQLException If the SQL query fails.
     */
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

    /**
     * Converts the current row of a ResultSet to a Book object.
     * @param rs The ResultSet positioned at the current row.
     * @return A fully populated Book object.
     * @throws SQLException If a database access error occurs or the column labels are invalid.
     */
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

    /**
     * Helper method to link authors to a newly created book.
     * Inserts rows into the table T_BookAuthor.
     * @param authors The list of authors to associate with the book.
     * @param bookId The ID of the newly created book.
     * @throws SQLException If the insertion fails.
     */
    private void addBookAuthor(List<Author> authors, int bookId) throws SQLException {
        String insertBookAuthor = "INSERT INTO T_BookAuthor (auId, bookId) VALUES (?,?)";
        try (PreparedStatement auStmt = connection.prepareStatement(insertBookAuthor)) {
            for (Author author : authors) {
                auStmt.setInt(1, author.getAuId());
                auStmt.setInt(2, bookId);
                auStmt.executeUpdate();
            }
        }
    }

    /**
     * Helper method to link genres to a newly created book.
     * Inserts rows into the table T_BookGenre.
     * @param genres The list of genres to associate with the book.
     * @param bookId The ID of the newly created book.
     * @throws SQLException If the insertion fails.
     */
    private void addBookGenre(List<Genre> genres, int bookId) throws SQLException {
        String insertBookGenre = "INSERT INTO T_BookGenre (genreId, bookId) VALUES (?,?)";
        try (PreparedStatement genStmt = connection.prepareStatement(insertBookGenre)) {
            for (Genre genre : genres) {
                genStmt.setInt(1, genre.getGenreId());
                genStmt.setInt(2, bookId);
                genStmt.executeUpdate();
            }
        }
    }

    private void insertBookReview(int bookId, int userId, int rating, String reviewText, LocalDate localDate) {
        String insertBookReview = ""
    }

    /**
     * Generic helper to execute a SELECT query with one String parameter.
     * @param sql The SQL query with one '?' placeholder.
     * @param parameter The string value to put in the placeholder.
     * @return A list of found Books.
     * @throws SelectException If the query fails.
     */
    private List<Book> executeQuery(String sql, String parameter) throws SelectException {
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, parameter);
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
}