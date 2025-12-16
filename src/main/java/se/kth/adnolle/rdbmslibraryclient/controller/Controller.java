package se.kth.adnolle.rdbmslibraryclient.controller;

import static javafx.scene.control.Alert.AlertType.*;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import se.kth.adnolle.rdbmslibraryclient.model.*;
import se.kth.adnolle.rdbmslibraryclient.model.exceptions.*;
import se.kth.adnolle.rdbmslibraryclient.view.BooksPane;
import se.kth.adnolle.rdbmslibraryclient.view.IViewListener;

public class Controller implements IViewListener {

    private final IBooksDb database;
    private final BooksPane view;

    public Controller(IBooksDb database, BooksPane view) {
        this.database = database;
        this.view = view;
        view.setViewListener(this);
    }

    @Override
    public void onSearchSelected(String searchFor, SearchMode mode) {
        if(searchFor == null || searchFor.isEmpty()) {
            view.showAlertAndWait("Enter a search string!", WARNING);
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
                Platform.runLater(() -> view.showAlertAndWait("Database fetch data error.", ERROR));
            }
        };
        Thread worker = new Thread(task);
        worker.start();
    }

    @Override
    public void onConnectSelected() {

    }

    @Override
    public void onDisconnectSelected() {

    }

    @Override
    public void onAddBookSelected() {
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
                Platform.runLater(() -> view.showAlertAndWait("Database fetch data error.", ERROR));
            }
        };
        Thread fetchWorker = new Thread(task);
        fetchWorker.start();
    }

    private void addBookToDB(Book createdBook) {
        Runnable task2 = () -> {
            try {
                database.addBook(createdBook, createdBook.getAuthors(), createdBook.getGenres());
            } catch (InsertException e) {
                Platform.runLater(() -> view.showAlertAndWait("Database insert error", ERROR));
            }
        };
        Thread saveWorker = new Thread(task2); //No race condition, we start this when we already have data
        saveWorker.start();
    }

    @Override
    public void onRateBookSelected(Book selectedBook) {
        if(selectedBook != null) {
            Optional<Integer> rating = view.showRateBookDialog(selectedBook);
            if(rating.isPresent()) {
                Runnable task = () -> {
                    try {
                        database.rateBook(selectedBook.getBookId(), rating.get());
                    }
                    catch (Exception e) {
                        Platform.runLater(() -> view.showAlertAndWait("Database error.", ERROR));
                    }
                };
                Thread rateWorker = new Thread(task);
                rateWorker.start();
            }
        }
        else view.showAlertAndWait("Select a book to rate", INFORMATION);
    }

    @Override
    public void onExitSelected() {
        try {
            database.disconnect();
        } catch (ConnectionException e) {
            view.showAlertAndWait("Failed to disconnect from database: " + e.getMessage(), ERROR);
        }
    }

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
}