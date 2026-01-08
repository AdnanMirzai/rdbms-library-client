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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import se.kth.adnolle.rdbmslibraryclient.model.Author;
import se.kth.adnolle.rdbmslibraryclient.model.Book;
import se.kth.adnolle.rdbmslibraryclient.model.Genre;
import se.kth.adnolle.rdbmslibraryclient.model.SearchMode;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public class BooksPane extends VBox {

    private IViewListener controller;
    private TableView<Book> booksTable;
    private ObservableList<Book> booksInTable;
    private ComboBox<SearchMode> searchModeBox;
    private TextField searchField;
    private Button searchButton;
    private MenuBar menuBar;
    private Circle connectionIndicator;

    public BooksPane() {
        this.init();
    }

    public void setViewListener(IViewListener controller) {
        this.controller = controller;
    }

    public void showAlertAndWait(String msg, Alert.AlertType type) {
        // types: INFORMATION, WARNING et c.
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    public void showAlertAndWait(String msg, Alert.AlertType type, String title, String header) {
        Alert alert = new Alert(type, msg);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    public void setConnectionIndicator(boolean isConnected) {
        if (isConnected) {
            connectionIndicator.setFill(Color.GREEN);
        } else connectionIndicator.setFill(Color.RED);
    }

    public void displayBooks(List<Book> result) {
        booksInTable.clear();
        booksInTable.addAll(result);
    }

    public Optional<Book> showAddBookDialog(List<Author> authors, List<Genre> genres) {
        AddBookDialog addBookDialog = new AddBookDialog(authors, genres);
        return addBookDialog.showAndWait();
    }

    public Optional<ReviewData> showReviewDialog(Book selectedBook) {
        ReviewDialog dialog = new ReviewDialog(selectedBook.getTitle());
        return dialog.showAndWait();
    }

    private void initSearchView() {
        searchField = new TextField();
        searchField.setPromptText("Search for...");
        searchModeBox = new ComboBox<>();
        searchModeBox.getItems().addAll(SearchMode.values());
        searchModeBox.setValue(SearchMode.Title);
        searchButton = new Button("Search");

        // event handling (dispatch to controller)
        searchButton.setOnAction(_ -> {
            String searchFor = searchField.getText();
            SearchMode mode = searchModeBox.getValue();
            controller.onSearchSelected(searchFor, mode);
        });
    }

    private void initBooksTable() {
        booksTable = new TableView<>();
        booksTable.setEditable(false); // don't allow user updates (yet)
        booksTable.setPlaceholder(new Label("No rows to display"));

        // define columns
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        TableColumn<Book, Date> publishedCol = new TableColumn<>("Published");
        TableColumn<Book, Integer> ratingCol = new TableColumn<>("Rating");
        TableColumn<Book, List<Genre>> genreCol = new TableColumn<>("Genres");

        //noinspection unchecked
        booksTable.getColumns().addAll(titleCol, isbnCol, publishedCol, ratingCol, genreCol);
        // give title column some extra space
        titleCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.3));
        isbnCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.2));
        genreCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.3));

        // define how to fill data for each cell,
        // get values from Book properties
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        publishedCol.setCellValueFactory(new PropertyValueFactory<>("published"));
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genres"));

        // When double-click on row, show books author details
        booksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !booksTable.getSelectionModel().isEmpty()) {
                Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
                controller.onAuthorDetailsSelected(selectedBook);
            }
        });

        // associate the table view with the data
        booksTable.setItems(booksInTable);
    }

    private void initMenus() {
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        MenuItem connectItem = new MenuItem("Connect");
        MenuItem disconnectItem = new MenuItem("Disconnect");
        fileMenu.getItems().addAll(exitItem, connectItem, disconnectItem);

        Menu manageMenu = new Menu("Manage");
        MenuItem addItem = new MenuItem("Add");
        MenuItem rateItem = new MenuItem("Rate");
        MenuItem removeItem = new MenuItem("Remove");
        MenuItem updateItem = new MenuItem("Update");
        manageMenu.getItems().addAll(addItem, rateItem, removeItem, updateItem);

        Menu userMenu = new Menu("User");
        MenuItem loginItem = new MenuItem("Login");
        MenuItem logoutItem = new MenuItem("Logout");
        userMenu.getItems().addAll(loginItem, logoutItem);

        Menu statusMenu = new Menu("Connection");
        connectionIndicator = new Circle(6, Color.RED);
        statusMenu.setGraphic(connectionIndicator);

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, manageMenu, statusMenu, userMenu);


        menuBar.getMenus().add(1, userMenu);

        exitItem.setOnAction(_ -> controller.onExitSelected());
        connectItem.setOnAction(_ -> controller.onConnectSelected());
        disconnectItem.setOnAction(_ -> controller.onDisconnectSelected());

        loginItem.setOnAction(_ -> controller.onLoginSelected());
        logoutItem.setOnAction(_ -> controller.onLogoutSelected());

        addItem.setOnAction(_ -> controller.onAddBookSelected());
        rateItem.setOnAction(_ -> {
            Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
            controller.onReviewBookSelected(selectedBook);
        });
    }

    private void init() {
        booksInTable = FXCollections.observableArrayList();

        // init views and event handlers
        initBooksTable();
        initSearchView();
        initMenus();

        FlowPane bottomPane = new FlowPane();
        bottomPane.setHgap(10);
        bottomPane.setPadding(new Insets(10, 10, 10, 10));
        bottomPane.getChildren().addAll(searchModeBox, searchField, searchButton);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(booksTable);
        mainPane.setBottom(bottomPane);
        mainPane.setPadding(new Insets(10, 10, 10, 10));

        this.getChildren().addAll(menuBar, mainPane);
        VBox.setVgrow(mainPane, Priority.ALWAYS);
    }

    public Optional<LoginCredentials> showLoginDialog() {
        LoginDialog dialog = new LoginDialog();
        return dialog.showAndWait();
    }

}