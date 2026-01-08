package se.kth.adnolle.rdbmslibraryclient.view;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import se.kth.adnolle.rdbmslibraryclient.model.Author;
import se.kth.adnolle.rdbmslibraryclient.model.Book;
import se.kth.adnolle.rdbmslibraryclient.model.Review;

import java.util.List;

public class BookDetailsDialog extends Dialog<Void> {

    public BookDetailsDialog(Book book, List<Review> reviews) {
        this.setTitle("Book Details");
        this.setHeaderText(book.getTitle());
        this.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab authorTab = new Tab("Authors");
        ListView<Author> authorList = new ListView<>();
        authorList.getItems().addAll(book.getAuthors());
        authorTab.setContent(authorList);

        Tab reviewTab = new Tab("Reviews");
        ListView<Review> reviewList = new ListView<>();

        if (reviews.isEmpty()) {
            reviewList.setPlaceholder(new Label("No reviews yet."));
        } else {
            reviewList.getItems().addAll(reviews);
            reviewList.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Review item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String stars = "★".repeat(item.getRating()) + "☆".repeat(5 - item.getRating());
                        String header = String.format("%s  |  %s  |  %s", stars, item.getUsername(), item.getDate());
                        setText(header + "\n\n" + (item.getText() == null ? "" : item.getText()));
                        setWrapText(true);
                        setPrefWidth(0);
                    }
                }
            });
        }
        reviewTab.setContent(reviewList);

        tabPane.getTabs().addAll(authorTab, reviewTab);

        BorderPane content = new BorderPane(tabPane);
        content.setPrefSize(400, 300);
        this.getDialogPane().setContent(content);
    }
}