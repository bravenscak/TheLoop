package hr.algebra.theloop.view;

import hr.algebra.theloop.model.Era;
import javafx.animation.RotateTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class CircularBoardView extends Pane {

    private static final double BOARD_WIDTH = 450;
    private static final double BOARD_HEIGHT = 350;
    private static final double CENTER_X = BOARD_WIDTH / 2;
    private static final double CENTER_Y = BOARD_HEIGHT / 2;

    private Map<Era, SimpleEraView> eraViews;
    private Polygon drFooMachine;

    public CircularBoardView() {
        this.eraViews = new HashMap<>();
        setupBoard();
        createSimpleEras();
        createDrFooMachine();
    }

    private void setupBoard() {
        setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);
        setMaxSize(BOARD_WIDTH, BOARD_HEIGHT);
        setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 80%, #16213e 0%, #0f3460 100%); " +
                "-fx-border-color: #eee; -fx-border-width: 1px;");
    }

    private void createSimpleEras() {
        double eraWidth = 80;
        double eraHeight = 60;
        double radius = 120;

        Era[] eras = Era.values();

        for (int i = 0; i < eras.length; i++) {
            Era era = eras[i];
            double angle = (i * Math.PI * 2 / 7) - Math.PI / 2;

            double x = CENTER_X + radius * Math.cos(angle) - eraWidth / 2;
            double y = CENTER_Y + radius * Math.sin(angle) - eraHeight / 2;

            SimpleEraView eraView = new SimpleEraView(era, x, y, eraWidth, eraHeight);
            eraViews.put(era, eraView);
            getChildren().add(eraView);
        }
    }

    private void createDrFooMachine() {
        drFooMachine = new Polygon();
        drFooMachine.getPoints().addAll(new Double[]{
                0.0, -25.0, 10.0, -10.0, 3.0, -10.0, 3.0, 10.0,
                -3.0, 10.0, -3.0, -10.0, -10.0, -10.0
        });

        drFooMachine.setLayoutX(CENTER_X);
        drFooMachine.setLayoutY(CENTER_Y);
        drFooMachine.setFill(Color.web("#ff1493"));
        drFooMachine.setStroke(Color.WHITE);
        drFooMachine.setStrokeWidth(2);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#ff1493"));
        glow.setRadius(6);
        drFooMachine.setEffect(glow);

        getChildren().add(drFooMachine);
    }

    public void updateEra(Era era, int rifts, int energy, boolean hasVortex, boolean playerPresent) {
        SimpleEraView eraView = eraViews.get(era);
        if (eraView != null) {
            eraView.updateResources(rifts, energy, hasVortex, playerPresent);
        }
    }

    public void pointDrFooAt(Era era) {
        if (drFooMachine != null) {
            double anglePerEra = 360.0 / 7.0;
            double targetAngle = era.ordinal() * anglePerEra;

            RotateTransition rotation = new RotateTransition(Duration.millis(600), drFooMachine);
            rotation.setToAngle(targetAngle);
            rotation.play();
        }
    }

    public SimpleEraView getEraView(Era era) {
        return eraViews.get(era);
    }

    public static class SimpleEraView extends Pane {
        private final Era era;
        private final Rectangle background;
        private final Text nameText;
        private final Text riftsText;
        private final Text energyText;

        public SimpleEraView(Era era, double x, double y, double width, double height) {
            this.era = era;

            setLayoutX(x);
            setLayoutY(y);
            setPrefSize(width, height);

            background = new Rectangle(width, height);
            background.setFill(Color.web(era.getColorCode()));
            background.setStroke(Color.WHITE);
            background.setStrokeWidth(2);
            background.setArcWidth(10);
            background.setArcHeight(10);

            nameText = new Text(era.getDisplayName());
            nameText.setFill(Color.WHITE);
            nameText.setFont(Font.font("Arial", FontWeight.BOLD, 9));
            nameText.setLayoutX(5);
            nameText.setLayoutY(15);

            riftsText = new Text("🔴0");
            riftsText.setLayoutX(5);
            riftsText.setLayoutY(35);

            energyText = new Text("🟢1");
            energyText.setLayoutX(35);
            energyText.setLayoutY(35);

            getChildren().addAll(background, nameText, riftsText, energyText);

            setOnMouseClicked(event -> {
                System.out.println("🖱️ " + era.getDisplayName() + " clicked");
                event.consume();
            });
        }

        public void updateResources(int rifts, int energy, boolean hasVortex, boolean playerPresent) {
            riftsText.setText("🔴" + rifts);
            energyText.setText("🟢" + energy);

            if (hasVortex) {
                background.setStroke(Color.RED);
                background.setStrokeWidth(4);
            } else if (playerPresent) {
                background.setStroke(Color.YELLOW);
                background.setStrokeWidth(3);
            } else {
                background.setStroke(Color.WHITE);
                background.setStrokeWidth(2);
            }
        }

        public Era getEra() {
            return era;
        }
    }
}