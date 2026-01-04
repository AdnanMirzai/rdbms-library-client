package se.kth.adnolle.rdbmslibraryclient.controller;

import static javafx.scene.control.Alert.AlertType.*;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import se.kth.adnolle.rdbmslibraryclient.model.*;
import se.kth.adnolle.rdbmslibraryclient.model.exceptions.*;
import se.kth.adnolle.rdbmslibraryclient.view.BooksPane;
import se.kth.adnolle.rdbmslibraryclient.view.IViewListener;

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
 * @author adnolle@kth.se
 */
public class Controller implements IViewListener {

    private final IBooksDb database;
    private final BooksPane view;

    public Controller(IBooksDb database, BooksPane view) {
        this.database = database;
        this.view = view;
        view.setViewListener(this);
    }

    /**
     * Initiates search for books.
     * <p>This method creates a new background thread to query the database.
     * If a {@link SelectException} occurs, an error alert is shown.</p>
     * @param searchFor The search string.
     * @param mode The search criterion (Title, ISBN, etc.).
     */
    @Override
    public void onSearchSelected(String searchFor, SearchMode mode) {
        if(searchFor == null || searchFor.isEmpty()) {
            view.showAlertAndWait("Enter a search string!", WARNING);
            return;
        }
        if(!isDBConnected()) {
            view.showAlertAndWait("Not connected to database!", INFORMATION);
            return;
        }

        Runnable task = () -> {
            try {
                List<Book> result = switch (mode) {
                    case Title -> database.findBooksByTitle(searchFor);
                    case ISBN -> database.findBooksByIsbn(searchFor);
                    case Author -> database.findBooksByAuthorName(searchFor);
                    case Genre -> database.findBooksByGenre(searchFor);
                    case Rating -> database.findBooksByRating(searchFor);
                };
                if(result == null || result.isEmpty()) {
                    Platform.runLater(() -> view.showAlertAndWait("No results found.", INFORMATION));
                }
                else Platform.runLater(() -> view.displayBooks(result));
            }
            catch (SelectException e) {
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
            if(database.connect("LibraryDB")) {
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

    /**
     * Handles the flow for adding a new book.
     * Starts a background thread to fetch available Authors and Genres from the DB.
     * Updates the UI to show the Add Book Dialog with the fetched data.
     * If the user confirms the dialog, save the data.
     */
    @Override
    public void onAddBookSelected() {
        if(!isDBConnected()) {
            view.showAlertAndWait("Not connected to database!", INFORMATION);
            return;
        }
        Runnable task = () -> {
            try {
                List<Author> authors = database.getAllAuthors();
                List<Genre> genres = database.getAllGenres();
                Platform.runLater(() -> {
                    Optional<Book> createdBook = view.showAddBookDialog(authors, genres);
                    if(createdBook.isPresent())
                        addBookToDB(createdBook.get());
                });
            } catch (SelectException e) {
                Platform.runLater(() -> view.showAlertAndWait("Database fetch error: " + e.getMessage(), ERROR));
            }
        };
        Thread fetchWorker = new Thread(task);
        fetchWorker.start();
    }

    //No race condition, we start this when we already have data
    private void addBookToDB(Book createdBook) {
        Runnable task2 = () -> {
            try {
                database.addBook(createdBook, createdBook.getAuthors(), createdBook.getGenres());
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
     * @param selectedBook The selected book to be rated.
     */
    @Override
    public void onRateBookSelected(Book selectedBook) {
        if(!isDBConnected()) {
            view.showAlertAndWait("Not connected to database!", INFORMATION);
            return;
        }
        if(selectedBook != null) {
            Optional<Integer> rating = view.showRateBookDialog(selectedBook);
            if(rating.isPresent()) {
                Runnable task = () -> {
                    try {
                        database.rateBook(selectedBook.getBookId(), rating.get());
                    }
                    catch (Exception e) {
                        Platform.runLater(() -> view.showAlertAndWait("Database error: " + e.getMessage(), ERROR));
                    }
                };
                Thread rateWorker = new Thread(task);
                rateWorker.start();
            }
        }
        else view.showAlertAndWait("Select a book to rate", INFORMATION);
    }

    /**
     * Shuts down the application.
     * Ensures the database connection is closed before exiting the JavaFX platform.
     */
    @Override
    public void onExitSelected() {
        try {
            if(database != null) database.disconnect();
        } catch (ConnectionException e) {
            view.showAlertAndWait("Failed to disconnect from database: " + e.getMessage(), ERROR);
        } finally {
            javafx.application.Platform.exit();
        }
    }

    /**
     * Displays detailed information about the authors of the selected book.
     * Works on the existing data in the Book object and does not require a DB query.
     * @param selectedBook The selected book to show author details for.
     */
    @Override
    public void onAuthorDetailsSelected(Book selectedBook) {
        if(selectedBook != null && !selectedBook.getAuthors().isEmpty()) {
            List<Author> authors = selectedBook.getAuthors();
            StringBuilder builder = new StringBuilder();
            for(Author author : authors) {
                builder.append(author.toString()).append("\n\n");
            }
            String headerInfo = "'" + selectedBook.getTitle() + "'" + " Authors:";
            view.showAlertAndWait(builder.toString(), INFORMATION, "Author Information", headerInfo);
        }
        else view.showAlertAndWait("No book selected", ERROR);
    }

    /**
     * Checks if the database connection is active.
     * catches {@link ConnectionException} and alerts the user if the check fails.
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
}