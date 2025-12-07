module se.kth.adnolle.rdbmslibraryclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;


    opens se.kth.adnolle.rdbmslibraryclient to javafx.fxml;
    exports se.kth.adnolle.rdbmslibraryclient;
}