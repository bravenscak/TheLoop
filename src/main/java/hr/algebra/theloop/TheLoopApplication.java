package hr.algebra.theloop;

import hr.algebra.theloop.model.PlayerMode;
import hr.algebra.theloop.utils.GameLogger;
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

            String playerModeStr = System.getProperty("playerMode", "SINGLE_PLAYER");
            PlayerMode playerMode = PlayerMode.valueOf(playerModeStr);

            String title = switch (playerMode) {
                case PLAYER_ONE -> "The Loop - Player 1 (Host)";
                case PLAYER_TWO -> "The Loop - Player 2 (Client)";
                case SINGLE_PLAYER -> "The Loop - Single Player";
            };

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (Exception e) {
            GameLogger.error("Failed to start application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        PlayerMode playerMode = PlayerMode.SINGLE_PLAYER;

        if (args.length > 0) {
            try {
                playerMode = PlayerMode.valueOf(args[0].toUpperCase());
                GameLogger.gameFlow("🎮 Starting in " + playerMode + " mode");
            } catch (IllegalArgumentException e) {
                GameLogger.error("❌ Invalid player mode: " + args[0]);
                GameLogger.error("Valid options: SINGLE_PLAYER, PLAYER_ONE, PLAYER_TWO");
                System.exit(1);
            }
        }

        System.setProperty("playerMode", playerMode.name());

        launch(args);
    }
}