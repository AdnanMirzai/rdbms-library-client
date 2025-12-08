package se.kth.adnolle.rdbmslibraryclient;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.kth.adnolle.rdbmslibraryclient.controller.Controller;
import se.kth.adnolle.rdbmslibraryclient.model.IBooksDb;
import se.kth.adnolle.rdbmslibraryclient.model.IBooksDbMockImpl;
import se.kth.adnolle.rdbmslibraryclient.view.BooksPane;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        IBooksDb database = new IBooksDbMockImpl();
        BooksPane view = new BooksPane();
        Controller controller = new Controller(primaryStage, database, view);
        // Will add real database connection here later

        Scene scene = new Scene(view, 800, 600);
        primaryStage.setTitle("Books Database Client");

        primaryStage.setOnCloseRequest(event -> {
            try {
                database.disconnect();
            } catch (Exception _) {}
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}