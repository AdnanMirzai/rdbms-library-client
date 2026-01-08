package se.kth.adnolle.rdbmslibraryclient.view;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import se.kth.adnolle.rdbmslibraryclient.model.Author;
import se.kth.adnolle.rdbmslibraryclient.model.Book;
import se.kth.adnolle.rdbmslibraryclient.model.Genre;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddBookDialog extends Dialog<Book> {

    private TextField titleField;
    private TextField isbnField;
    private DatePicker publishedField;
    private TextArea storyLineArea;
    private ComboBox<Integer> ratingBox;

    private ListView<Author> authorsListView;

    private List<CheckBox> genreCheckBox;

    private Runnable onAddAuthorCallback;

    public AddBookDialog(ObservableList<Author> authors, List<Genre> genres) {
        setTitle("Add New Book");
        setHeaderText("Enter book details:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        titleField = new TextField();
        titleField.setPromptText("Book title");
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);

        isbnField = new TextField();
        isbnField.setPromptText("13 digits");
        grid.add(new Label("ISBN:"), 0, 1);
        grid.add(isbnField, 1, 1);

        publishedField = new DatePicker();
        publishedField.setValue(LocalDate.now());
        grid.add(new Label("Published:"), 0, 2);
        grid.add(publishedField, 1, 2);

        storyLineArea = new TextArea();
        storyLineArea.setPromptText("StoryLine");
        storyLineArea.setPrefRowCount(3);
        grid.add(new Label("Story:"), 0, 3);
        grid.add(storyLineArea, 1, 3);

        ratingBox = new ComboBox<>();
        ratingBox.getItems().addAll(1, 2, 3, 4, 5);
        ratingBox.setValue(3);
        grid.add(new Label("Rating:"), 0, 4);
        grid.add(ratingBox, 1, 4);

        Label authorsLabel = new Label("Authors:");
        Button addAuthorBtn = new Button("+");
        addAuthorBtn.setTooltip(new Tooltip("Create new Author"));

        addAuthorBtn.setOnAction(e -> {
            if (onAddAuthorCallback != null) onAddAuthorCallback.run();
        });

        HBox authorHeader = new HBox(10, authorsLabel, addAuthorBtn);

        authorsListView = new ListView<>(authors);
        authorsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        authorsListView.setPrefHeight(100);

        grid.add(authorHeader, 0, 5);
        grid.add(authorsListView, 1, 5);

        Label genresLabel = new Label("Select Genres:");
        VBox genresBox = new VBox(5);
        genreCheckBox = new ArrayList<>();
        for (Genre genre : genres) {
            CheckBox cb = new CheckBox(genre.getGenre());
            cb.setUserData(genre);
            genreCheckBox.add(cb);
            genresBox.getChildren().add(cb);
        }
        ScrollPane genresScroll = new ScrollPane(genresBox);
        genresScroll.setPrefHeight(100);
        grid.add(genresLabel, 0, 6);
        grid.add(genresScroll, 1, 6);

        getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Add Book", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        Button addButton = (Button) getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        titleField.textProperty().addListener((_, _, _) -> validateUserInput(addButton));
        isbnField.textProperty().addListener((_, _, _) -> validateUserInput(addButton));

        for (CheckBox cb : genreCheckBox) {
            cb.selectedProperty().addListener((_, _, _) -> validateUserInput(addButton));
        }

        authorsListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Author>) c -> {
            validateUserInput(addButton);
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return createBookFromInput();
            }
            return null;
        });
    }

    public void setOnAddAuthor(Runnable handler) {
        this.onAddAuthorCallback = handler;
    }

    private Book createBookFromInput() {
        String title = titleField.getText();
        String isbn = isbnField.getText();
        Date date = Date.valueOf(publishedField.getValue());
        String story = storyLineArea.getText();
        int rating = ratingBox.getValue();

        List<Author> selectedAuthors = new ArrayList<>(authorsListView.getSelectionModel().getSelectedItems());

        List<Genre> selectedGenres = new ArrayList<>();
        for (CheckBox cb : genreCheckBox) {
            if (cb.isSelected()) {
                selectedGenres.add((Genre) cb.getUserData());
            }
        }

        return new Book(0, isbn, title, date, story, rating, selectedAuthors, selectedGenres, "");
    }

    private void validateUserInput(Button addButton) {
        boolean hasTitle = !titleField.getText().trim().isEmpty();
        boolean hasIsbn = !isbnField.getText().trim().isEmpty(); // Add regex check here if needed
        boolean hasAuthor = !authorsListView.getSelectionModel().getSelectedItems().isEmpty();

        boolean hasGenre = false;
        for(CheckBox cb : genreCheckBox) {
            if(cb.isSelected()) {
                hasGenre = true;
                break;
            }
        }

        addButton.setDisable(!(hasTitle && hasIsbn && hasAuthor && hasGenre));
    }
}