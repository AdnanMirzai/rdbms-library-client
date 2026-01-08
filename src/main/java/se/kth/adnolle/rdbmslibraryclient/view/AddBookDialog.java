package se.kth.adnolle.rdbmslibraryclient.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import se.kth.adnolle.rdbmslibraryclient.model.Author;
import se.kth.adnolle.rdbmslibraryclient.model.Book;
import se.kth.adnolle.rdbmslibraryclient.model.Genre;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddBookDialog extends Dialog<Book> {

    private final TextField titleField;
    private final TextField isbnField;
    private final DatePicker publishedField;
    private final TextArea storyLineArea;
    private final ComboBox<Integer> ratingBox;
    private final List<CheckBox> authorCheckBox;
    private final List<CheckBox> genreCheckBox;
    private final List<Author> availableAuthors;
    private final List<Genre> availableGenres;

    public AddBookDialog(List<Author> authors, List<Genre> genres) {
        this.availableAuthors = authors;
        this.availableGenres = genres;
        setTitle("Add New Book");
        setHeaderText("Enter book details:");

        //form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        titleField = new TextField();
        titleField.setPromptText("Book title");
        grid.add(new Label("Title"), 0, 0);
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

        Label authorsLabel = new Label("Select Authors:");
        VBox authorsBox = new VBox(5);
        authorCheckBox = new ArrayList<>();
        for (Author author : authors) {
            CheckBox cb = new CheckBox(author.getName());
            cb.setUserData(author);
            authorCheckBox.add(cb);
            authorsBox.getChildren().add(cb);
        }
        ScrollPane authorScroll = new ScrollPane(authorsBox);
        authorScroll.setPrefHeight(100);
        grid.add(authorsLabel, 0, 5);
        grid.add(authorScroll, 1, 5);

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

        //Buttons
        ButtonType addButtonType = new ButtonType("Add Book", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        Button addButton = (Button) getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        //Listen textField
        titleField.textProperty().addListener((_, _, _) -> validateUserInput(addButton));

        //Listen isbnField
        isbnField.textProperty().addListener((_, _, _) -> validateUserInput(addButton));


        for (CheckBox cb : genreCheckBox) {
            cb.selectedProperty().addListener((_, _, _) -> validateUserInput(addButton));
        }

        for (CheckBox cb : authorCheckBox) {
            cb.selectedProperty().addListener((_, _, _) -> validateUserInput(addButton));
        }

        setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return createBookFromInput();
            }
            return null;
        });
    }

    //check if the fields are empty or incorrect ISBN-format
    private void validateUserInput(Button addButton) {
        boolean validISBN = isbnField.getText().matches("\\d{13}");
        boolean validTitle = !titleField.getText().trim().isEmpty();
        boolean validAuthor = authorCheckBox.stream().anyMatch(CheckBox::isSelected);
        boolean validGenre = genreCheckBox.stream().anyMatch(CheckBox::isSelected);
        addButton.setDisable(!validTitle || !validGenre || !validAuthor || !validISBN);
    }

    private Book createBookFromInput() {
        String title = titleField.getText().trim();
        String isbn = isbnField.getText().trim();
        LocalDate localDate = publishedField.getValue();
        Date published = Date.valueOf(localDate);
        String storyLine = storyLineArea.getText().trim();
        Integer rating = ratingBox.getValue();

        List<Author> selectedAuthors = new ArrayList<>();
        for (CheckBox cb : authorCheckBox) {
            if (cb.isSelected()) {
                selectedAuthors.add((Author) cb.getUserData());
            }
        }

        List<Genre> selectedGenres = new ArrayList<>();
        for (CheckBox cb : genreCheckBox) {
            if (cb.isSelected()) {
                selectedGenres.add((Genre) cb.getUserData());
            }
        }
        return new Book(isbn, title, published, storyLine, rating, selectedAuthors, selectedGenres, "");
    }

    public List<Author> getSelectedAuthors() {
        List<Author> selected = new ArrayList<>();
        for (CheckBox cb : authorCheckBox) {
            if (cb.isSelected()) {
                selected.add((Author) cb.getUserData());
            }
        }
        return selected;
    }

    public List<Genre> getSelectedGenres() {
        List<Genre> selected = new ArrayList<>();
        for (CheckBox cb : genreCheckBox) {
            if (cb.isSelected()) {
                selected.add((Genre) cb.getUserData());
            }
        }
        return selected;
    }
}