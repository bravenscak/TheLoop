module hr.algebra.theloop {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.desktop;

    exports hr.algebra.theloop;
    exports hr.algebra.theloop.controller;
    exports hr.algebra.theloop.model;
    exports hr.algebra.theloop.cards;
    exports hr.algebra.theloop.engine;
    exports hr.algebra.theloop.view;

    opens hr.algebra.theloop to javafx.fxml;
    opens hr.algebra.theloop.controller to javafx.fxml;
    opens hr.algebra.theloop.view to javafx.fxml;
}