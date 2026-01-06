package se.kth.adnolle.rdbmslibraryclient.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class ReviewDialog extends Dialog<ReviewData> {

    public ReviewDialog(String bookTitle) {
        this.setTitle("Review Book");
        this.setHeaderText("Write a review for: " + bookTitle);

        ButtonType okButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Integer> ratingBox = new ComboBox<>();
        ratingBox.getItems().addAll(1, 2, 3, 4, 5);
        ratingBox.setValue(5);

        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Write your thoughts here...");
        reviewArea.setPrefRowCount(3);
        reviewArea.setWrapText(true);

        grid.add(new Label("Rating:"), 0, 0);
        grid.add(ratingBox, 1, 0);
        grid.add(new Label("Review:"), 0, 1);
        grid.add(reviewArea, 1, 1);

        this.getDialogPane().setContent(grid);

        Platform.runLater(reviewArea::requestFocus);

        this.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new ReviewData(ratingBox.getValue(), reviewArea.getText());
            }
            return null;
        });
    }
}
