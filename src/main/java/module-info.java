module hr.algebra.theloop {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.desktop;
    requires java.rmi;

    exports hr.algebra.theloop;

    exports hr.algebra.theloop.controller;

    exports hr.algebra.theloop.model;
    exports hr.algebra.theloop.missions;
    exports hr.algebra.theloop.utils;
    exports hr.algebra.theloop.thread;
    exports hr.algebra.theloop.persistence;
    exports hr.algebra.theloop.rmi;
    exports hr.algebra.theloop.config;

    exports hr.algebra.theloop.cards;

    exports hr.algebra.theloop.engine;

    exports hr.algebra.theloop.view;
    exports hr.algebra.theloop.ui;

    exports hr.algebra.theloop.input;

    opens hr.algebra.theloop to javafx.fxml;
    opens hr.algebra.theloop.controller to javafx.fxml;
    opens hr.algebra.theloop.view to javafx.fxml;
    opens hr.algebra.theloop.ui to javafx.fxml;
}