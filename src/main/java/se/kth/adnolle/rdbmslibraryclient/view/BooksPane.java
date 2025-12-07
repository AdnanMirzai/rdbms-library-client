package se.kth.adnolle.rdbmslibraryclient.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import se.kth.adnolle.rdbmslibraryclient.model.Book;
import se.kth.adnolle.rdbmslibraryclient.model.IBooksDb;
import se.kth.adnolle.rdbmslibraryclient.model.SearchMode;

public class BooksPane extends VBox {

    private TableView<Book> booksTable;
    private ObservableList<Book> booksInTable;
    private ComboBox<SearchMode> searchModeBox;
    private TextField searchField;
    private Button searchButton;
    private MenuBar menuBar;

    public BooksPane() {
        this.init();
    }

    void init() {
        booksInTable = FXCollections.observableArrayList();

    }

}
