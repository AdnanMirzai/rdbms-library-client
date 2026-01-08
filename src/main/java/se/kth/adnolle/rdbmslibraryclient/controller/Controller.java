package se.kth.adnolle.rdbmslibraryclient.controller;

import javafx.application.Platform;
import se.kth.adnolle.rdbmslibraryclient.model.*;
import se.kth.adnolle.rdbmslibraryclient.model.exceptions.ConnectionException;
import se.kth.adnolle.rdbmslibraryclient.model.exceptions.InsertException;
import se.kth.adnolle.rdbmslibraryclient.model.exceptions.LoginException;
import se.kth.adnolle.rdbmslibraryclient.model.exceptions.SelectException;
import se.kth.adnolle.rdbmslibraryclient.view.BooksPane;
import se.kth.adnolle.rdbmslibraryclient.view.IViewListener;
import se.kth.adnolle.rdbmslibraryclient.view.LoginCredentials;
import se.kth.adnolle.rdbmslibraryclient.view.ReviewData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.*;


/**
 * Controller handles all communication between View and the Model.
 * It implements {@link IViewListener} to respond to user events generated in the UI.
 *
 * <p><b>Threading:</b>
 * To ensure a responsive user interface, all long-running database operations like
 * (Select, Insert, Update) are executed on separate background threads.
 * Results and UI updates are dispatched back to the JavaFX Application Thread
 * using {@code Platform.runLater}.</p>
 *
 * <p><b>Exception Handling:</b>
 * Exceptions thrown by the model are caught within the background tasks. Error messages are displayed to the user via the View.</p>
 *
 * @author adnolle@kth.se
 */
public class Controller implements IViewListener {

    private final IBooksDb database;
    private final BooksPane view;

    private User currentUser = null;

    private String lastSearchTerm = "";
    private SearchMode lastSearchMode = SearchMode.Title;

    public Controller(IBooksDb database, BooksPane view) {
        this.database = database;
        this.view = view;
        view.setViewListener(this);
    }

    /**
     * Initiates search for books.
     * <p>This method creates a new background thread to query the database.
     * If a {@link SelectException} occurs, an error alert is shown.</p>
     *
     * @param searchFor The search string.
     * @param mode      The search criterion (Title, ISBN, etc.).
     */
    @Override
    public void onSearchSelected(String searchFor, SearchMode mode) {
        if (!isDBConnected()) {
            view.showAlertAndWait("Not connected to database!", INFORMATION);
            return;
        }

        this.lastSearchTerm = searchFor;
        this.lastSearchMode = mode;

        Runnable task = () -> {
            try {
                List<Book> result;

                if (searchFor == null || searchFor.trim().isEmpty()) {
                    result = database.getAllBooks();
                } else {
                    result = switch (mode) {
                        case Title -> database.findBooksByTitle(searchFor);
                        case ISBN -> database.findBooksByIsbn(searchFor);
                        case Author -> database.findBooksByAuthorName(searchFor);
                        case Genre -> database.findBooksByGenre(searchFor);
                        case Rating -> database.findBooksByRating(searchFor);
                    };
                }

                if (result == null || result.isEmpty()) {
                    Platform.runLater(() -> {
                        view.showAlertAndWait("No results found.", INFORMATION);
                        view.displayBooks(new ArrayList<>());
                    });
                } else {
                    Platform.runLater(() -> view.displayBooks(result));
                }
            } catch (SelectException e) {
                Platform.runLater(() -> view.showAlertAndWait("Database fetch data error: " + e.getMessage(), ERROR));
            }
        };
        Thread worker = new Thread(task);
        worker.start();
    }

    /**
     * Attempts to connect to the database.
     * Updates the connection indicator in the view upon success.
     * Displays an error alert if a {@link ConnectionException} occurs.
     */
    @Override
    public void onConnectSelected() {
        try {
            if (database.connect("LibraryDB", "DB_clientApp", "ABC.123")) {
                view.showAlertAndWait("Connected!", INFORMATION);
                view.setConnectionIndicator(true);
            }
        } catch (ConnectionException e) {
            view.showAlertAndWait("Failed to connect to database: " + e.getMessage(), ERROR);
        }
    }

    /**
     * Disconnects from the database.
     * Updates the connection indicator in the view.
     * Displays an error alert if a {@link ConnectionException} occurs.
     */
    @Override
    public void onDisconnectSelected() {
        try {
            database.disconnect();
            view.showAlertAndWait("Disconnected!", INFORMATION);
            view.setConnectionIndicator(false);
        } catch (ConnectionException e) {
            view.showAlertAndWait("Failed to disconnect from database: " + e.getMessage(), ERROR);
        }
    }

    @Override
    public void onLoginSelected() {
        if (!isDBConnected()) return;

        Optional<LoginCredentials> credentials = view.showLoginDialog();
        credentials.ifPresent(creds -> {
            Runnable task = () -> {
                try {
                    User user = database.login(creds.username(), creds.password());

                    if (user == null) {
                        throw new LoginException("Invalid username or password.");
                    }

                    Platform.runLater(() -> {
                        this.currentUser = user;
                        view.showAlertAndWait("Welcome " + user.getUsername(), INFORMATION);
                    });
                } catch (LoginException e) {
                    Platform.runLater(() -> view.showAlertAndWait("Login failed: " + e.getMessage(), ERROR));
                }
            };
            new Thread(task).start();
        });
    }

    @Override
    public void onLogoutSelected() {
        this.currentUser = null;
        view.showAlertAndWait("Logged out.", INFORMATION);
    }

    /**
     * Handles the flow for adding a new book.
     * Starts a background thread to fetch available Authors and Genres from the DB.
     * Updates the UI to show the Add Book Dialog with the fetched data.
     * If the user confirms the dialog, save the data.
     */
    @Override
    public void onAddBookSelected() {
        if (currentUser == null) {
            view.showAlertAndWait("You must log in to add books.", WARNING);
            return;
        }

        if (!isDBConnected()) {
            view.showAlertAndWait("Not connected to database!", INFORMATION);
            return;
        }
        Runnable task = () -> {
            try {
                List<Author> authors = database.getAllAuthors();
                List<Genre> genres = database.getAllGenres();
                Platform.runLater(() -> {
                    Optional<Book> createdBook = view.showAddBookDialog(authors, genres);
                    if (createdBook.isPresent())
                        addBookToDB(createdBook.get());
                });
            } catch (SelectException e) {
                Platform.runLater(() -> view.showAlertAndWait("Database fetch error: " + e.getMessage(), ERROR));
            }
        };
        Thread fetchWorker = new Thread(task);
        fetchWorker.start();
    }

    private void addBookToDB(Book createdBook) {
        Runnable task2 = () -> {
            try {
                database.addBook(createdBook, createdBook.getAuthors(), createdBook.getGenres(), currentUser.getUserId());
            } catch (InsertException e) {
                Platform.runLater(() -> view.showAlertAndWait("Database insert error: " + e.getMessage(), ERROR));
            }
        };
        Thread saveWorker = new Thread(task2);
        saveWorker.start();
    }

    /**
     * Handles the process of rating a selected book.
     * Displays a dialog for rating input,
     * then creates a background thread to perform the update in the database.
     *
     * @param selectedBook The selected book to be rated.
     */
    @Override
    public void onReviewBookSelected(Book selectedBook) {
        if (currentUser == null) {
            view.showAlertAndWait("You must log in to rate books.", WARNING);
            return;
        }

        if (!isDBConnected()) {
            view.showAlertAndWait("Not connected to database!", INFORMATION);
            return;
        }

        if (selectedBook != null) {
            Optional<ReviewData> result = view.showReviewDialog(selectedBook);

            result.ifPresent(review -> {
                int rating = review.rating();
                String text = review.text();

                Runnable task = () -> {
                    try {
                        database.reviewBook(selectedBook.getBookId(), rating, text, currentUser);

                        Platform.runLater(() -> {
                            onSearchSelected(lastSearchTerm, lastSearchMode);
                            view.showAlertAndWait("Review submitted!", INFORMATION);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> view.showAlertAndWait("Database error: " + e.getMessage(), ERROR));
                    }
                };
                Thread rateWorker = new Thread(task);
                rateWorker.start();
            });
        } else view.showAlertAndWait("Select a book to rate", INFORMATION);
    }

    /**
     * Shuts down the application.
     * Ensures the database connection is closed before exiting the JavaFX platform.
     */
    @Override
    public void onExitSelected() {
        try {
            if (database != null) database.disconnect();
        } catch (ConnectionException e) {
            view.showAlertAndWait("Failed to disconnect from database: " + e.getMessage(), ERROR);
        } finally {
            javafx.application.Platform.exit();
        }
    }

    /**
     * Displays detailed information about the authors of the selected book.
     * Works on the existing data in the Book object and does not require a DB query.
     *
     * @param selectedBook The selected book to show author details for.
     */
    /*@Override
    public void onAuthorDetailsSelected(Book selectedBook) {
        if (selectedBook != null && !selectedBook.getAuthors().isEmpty()) {
            List<Author> authors = selectedBook.getAuthors();
            StringBuilder builder = new StringBuilder();
            for (Author author : authors) {
                builder.append(author.toString()).append("\n\n");
            }
            String headerInfo = "'" + selectedBook.getTitle() + "'" + " Authors:";
            view.showAlertAndWait(builder.toString(), INFORMATION, "Author Information", headerInfo);
        } else view.showAlertAndWait("No book selected", ERROR);
    }*/

    /**
     * Checks if the database connection is active.
     * catches {@link ConnectionException} and alerts the user if the check fails.
     *
     * @return true if connected, else false.
     */
    private boolean isDBConnected() {
        try {
            return database.isConnected();
        } catch (ConnectionException e) {
            view.showAlertAndWait(e.getMessage(), ERROR);
        }
        return false;
    }

    /**
     * Called when double-clicking a book row.
     * Fetches reviews asynchronously and shows the Details Dialog (reviews and author info).
     */
    @Override
    public void onBookDetailsSelected(Book selectedBook) {
        if(selectedBook == null) return;

        if(!isDBConnected()) {
            view.showAlertAndWait("Not connected to database. Cannot fetch reviews.", ERROR);
            return;
        }

        Runnable task = () -> {
            try {
                List<Review> reviews = database.getReviewsForBook(selectedBook.getBookId());

                Platform.runLater(() -> {
                    view.showBookDetails(selectedBook, reviews);
                });
            } catch (SelectException e) {
                Platform.runLater(() -> view.showAlertAndWait("Failed to load details: " + e.getMessage(), ERROR));
            }
        };
        new Thread(task).start();
    }
}