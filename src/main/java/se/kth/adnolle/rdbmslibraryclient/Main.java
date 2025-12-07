package se.kth.adnolle.rdbmslibraryclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        Button stopUI = new Button("Stop UI");
        Button exit = new Button("Exit");
        stopUI.setOnAction(actionEvent -> {
            try{
                Thread.sleep(10000);
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        exit.setOnAction(actionEvent -> Platform.exit());
        root.getChildren().addAll(stopUI, exit);
        Scene scene = new Scene(root, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Con");
        primaryStage.show();

    }
}
