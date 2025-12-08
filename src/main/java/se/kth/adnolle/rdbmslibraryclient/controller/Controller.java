package se.kth.adnolle.rdbmslibraryclient.controller;

import javafx.stage.Stage;
import se.kth.adnolle.rdbmslibraryclient.model.IBooksDb;
import se.kth.adnolle.rdbmslibraryclient.model.SearchMode;
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
    public void onConnectSelected() {

    }

    @Override
    public void onDisconnectSelected() {

    }

    @Override
    public void onSearchSelected(String text, SearchMode mode) {

    }

    @Override
    public void addBookSelected() {

    }

    @Override
    public void rateBookSelected() {

    }

    @Override
    public void DetailsSelected() {

    }
}