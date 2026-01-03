module se.kth.adnolle.rdbmslibraryclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;


    opens se.kth.adnolle.rdbmslibraryclient to javafx.base;
    opens se.kth.adnolle.rdbmslibraryclient.model to javafx.base;
    exports se.kth.adnolle.rdbmslibraryclient;
}