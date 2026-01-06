package se.kth.adnolle.rdbmslibraryclient.model;

import se.kth.adnolle.rdbmslibraryclient.model.exceptions.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MySQLImpl implements IBooksDb {
    private Connection connection;

    @Override
    public boolean connect(String database, String username, String password) throws ConnectionException {
        try {
            if(connection != null && !connection.isClosed()) return true;
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        }

        String url = "jdbc:mysql://localhost:3306/" + database + "?UseClientEnc=UTF8";
        try {
            connection = DriverManager.getConnection(url, username, password);
            return true;
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    @Override
    public User login(String username, String password) throws LoginException {
        String query = "SELECT userId, username FROM T_User WHERE username = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("userId"), rs.getString("username"));
                } else {
                    throw new LoginException("Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            throw new LoginException("Login database error: " + e.getMessage());
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

    // BASE QUERY: Calculates Average Rating on the fly
    // We use LEFT JOIN so books without reviews still show up (rating will be 0 or null)
    private static final String BASE_SELECT =
            "SELECT B.bookId, B.isbn, B.title, B.published, B.storyLine, " +
                    "ROUND(AVG(R.rating)) as rating " +
                    "FROM T_Book B " +
                    "LEFT JOIN T_Review R ON B.bookId = R.bookId ";

    private static final String GROUP_BY = " GROUP BY B.bookId";

    @Override
    public List<Book> getAllBooks() throws SelectException {
        String sql = BASE_SELECT + GROUP_BY;
        return executeQuery(sql, null);
    }

    @Override
    public List<Book> findBooksByTitle(String title) throws SelectException {
        String sql = BASE_SELECT + " WHERE B.title LIKE ? " + GROUP_BY;
        return executeQuery(sql, "%" + title + "%");
    }

    @Override
    public List<Book> findBooksByIsbn(String isbn) throws SelectException {
        String sql = BASE_SELECT + " WHERE B.isbn = ? " + GROUP_BY;
        return executeQuery(sql, isbn);
    }

    @Override
    public List<Book> findBooksByAuthorName(String name) throws SelectException {
        String sql = BASE_SELECT +
                " JOIN T_BookAuthor BA ON B.bookId = BA.bookId " +
                " JOIN T_Author A ON BA.auId = A.auId " +
                " WHERE A.name LIKE ? " + GROUP_BY;
        return executeQuery(sql, "%" + name + "%");
    }

    @Override
    public List<Book> findBooksByGenre(String genre) throws SelectException {
        String sql = BASE_SELECT +
                " JOIN T_BookGenre BG ON B.bookId = BG.bookId " +
                " JOIN T_Genre G ON BG.genreId = G.genreId " +
                " WHERE G.genre LIKE ? " + GROUP_BY;
        return executeQuery(sql, "%" + genre + "%");
    }

    @Override
    public List<Book> findBooksByRating(String rating) throws SelectException {
        // HAVING clause is required because we are filtering on an aggregate function (AVG)
        String sql = BASE_SELECT + GROUP_BY + " HAVING rating = ?";
        return executeQuery(sql, rating);
    }

    @Override
    public void addBook(Book book, List<Author> authors, List<Genre> genres, int addedBy) throws InsertException {
        // We do NOT insert 'rating' here anymore
        String insertBook = "INSERT INTO T_Book (isbn, title, published, storyLine, addedBy) VALUES (?,?,?,?,?)";
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

                stmt.setInt(5, addedBy);

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
    public void reviewBook(int bookId, int rating, String reviewText, User user) throws UpdateException {
        // Simply insert/update the review.
        // We do NOT update T_Book, because the rating is calculated dynamically in the SELECT queries.
        String upsertReview = "INSERT INTO T_Review (bookId, userId, rating, reviewDate) VALUES (?, ?, ?, CURDATE()) " +
                "ON DUPLICATE KEY UPDATE rating = VALUES(rating), reviewDate = VALUES(reviewDate)";

        try (PreparedStatement stmt = connection.prepareStatement(upsertReview)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, user.getUserId());
            stmt.setInt(3, rating);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new UpdateException("Failed to add review: " + e.getMessage());
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
        // This 'rating' now comes from the calculated field in the SQL query
        int rating = rs.getInt("rating");
        List<Author> authors = getAuthorsForBook(bookId);
        List<Genre> genres = getGenresForBook(bookId);

        return new Book(bookId, isbn, title, published, storyLine, rating, authors, genres);
    }

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

    private List<Book> executeQuery(String sql, String parameter) throws SelectException {
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if(parameter != null) stmt.setString(1, parameter);

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