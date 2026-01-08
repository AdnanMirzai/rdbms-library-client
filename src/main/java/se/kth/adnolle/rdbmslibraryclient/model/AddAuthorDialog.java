package se.kth.adnolle.rdbmslibraryclient.model;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import se.kth.adnolle.rdbmslibraryclient.model.Author;
import java.sql.Date;

public class AddAuthorDialog extends Dialog<Author> {

    public AddAuthorDialog() {
        this.setTitle("Add New Author");
        this.setHeaderText("Enter author details");

        ButtonType okButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("Date of Birth (Optional)");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("DOB:"), 0, 1);
        grid.add(dobPicker, 1, 1);

        this.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        this.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Date sqlDate = (dobPicker.getValue() != null) ? Date.valueOf(dobPicker.getValue()) : null;
                // pass ID 0 temporarily. The DB/Controller will assign the real ID.
                return new Author(0, nameField.getText(), sqlDate);
            }
            return null;
        });
    }
}