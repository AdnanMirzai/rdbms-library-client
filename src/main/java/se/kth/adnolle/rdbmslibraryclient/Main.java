package se.kth.adnolle.rdbmslibraryclient;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.kth.adnolle.rdbmslibraryclient.controller.Controller;
import se.kth.adnolle.rdbmslibraryclient.model.IBooksDb;
import se.kth.adnolle.rdbmslibraryclient.model.MongoDbImpl;
import se.kth.adnolle.rdbmslibraryclient.view.BooksPane;
import se.kth.adnolle.rdbmslibraryclient.view.IViewListener;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        IBooksDb database = new MongoDbImpl();
        //IBooksDb database = new MySQLImpl();
        BooksPane view = new BooksPane();
        IViewListener controller = new Controller(database, view);

        Scene scene = new Scene(view, 800, 600);
        primaryStage.setTitle("Books Database Client");

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            controller.onExitSelected();
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}