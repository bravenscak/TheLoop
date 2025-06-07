package hr.algebra.theloop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TheLoopApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainGame.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 1000);

            scene.getStylesheets().add(getClass().getResource("/css/theloop-theme.css").toExternalForm());

            primaryStage.setTitle("The Loop - Time Agent Mission");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}