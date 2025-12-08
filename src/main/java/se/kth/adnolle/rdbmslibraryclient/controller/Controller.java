package se.kth.adnolle.rdbmslibraryclient.controller;

import javafx.stage.Stage;
import static javafx.scene.control.Alert.AlertType.*;
import java.util.List;
import java.util.Optional;

import se.kth.adnolle.rdbmslibraryclient.model.*;
import se.kth.adnolle.rdbmslibraryclient.model.exceptions.InsertException;
import se.kth.adnolle.rdbmslibraryclient.view.BooksPane;
import se.kth.adnolle.rdbmslibraryclient.view.IViewListener;

public class Controller implements IViewListener {

    private final Stage primaryStage;
    private final IBooksDb database;
    private final BooksPane view;

    public Controller(Stage primaryStage, IBooksDb database, BooksPane view) {
        this.primaryStage = primaryStage;
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

        try {
            List<Book> result = switch (mode) {
                case Title -> database.findBooksByTitle(searchFor);
                case ISBN -> database.findBooksByIsbn(searchFor);
                case Author -> database.findBooksByAuthorName(searchFor);
                case Genre -> database.findBooksByGenre(searchFor);
                case Rating -> database.findBooksByRating(searchFor);
            };
            if(result == null || result.isEmpty()) {
                view.showAlertAndWait("No results found.", INFORMATION);
            }
            else view.displayBooks(result);

        } catch (Exception e) {
            view.showAlertAndWait("Database error.", ERROR);
        }

    }

    @Override
    public void onConnectSelected() {

    }

    @Override
    public void onDisconnectSelected() {

    }

    @Override
    public void onAddBookSelected() {
        try{
            List<Author> authors = database.getAllAuthors();
            List<Genre> genres = database.getAllGenres();
            Optional<Book> createdBook = view.showAddBookDialog(authors, genres);
            if(createdBook.isPresent()) {
                try {
                    database.addBook(createdBook.get(), createdBook.get().getAuthors(), createdBook.get().getGenres());
                } catch (Exception e) {
                    view.showAlertAndWait("Input correct information", ERROR);
                }
            }
        } catch (Exception e) {
            view.showAlertAndWait("Database error.", ERROR);
        }
    }

    @Override
    public void onRateBookSelected() {

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