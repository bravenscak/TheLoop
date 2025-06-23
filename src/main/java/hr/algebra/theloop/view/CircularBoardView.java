package hr.algebra.theloop.view;

import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import javafx.animation.RotateTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.util.EnumMap;
import java.util.List;

public class CircularBoardView extends Pane {

    private static final double BOARD_WIDTH = 600;
    private static final double BOARD_HEIGHT = 500;
    private static final double CENTER_X = BOARD_WIDTH / 2;
    private static final double CENTER_Y = BOARD_HEIGHT / 2;

    private EnumMap<Era, SimpleEraView> eraViews;
    private Polygon drFooMachine;

    public CircularBoardView() {
        this.eraViews = new EnumMap<>(Era.class);
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
        double eraWidth = 120;
        double eraHeight = 90;
        double radius = 180;

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
        drFooMachine.getPoints().addAll(
                0.0, -25.0, 10.0, -10.0, 3.0, -10.0, 3.0, 10.0,
                -3.0, 10.0, -3.0, -10.0, -10.0, -10.0
        );

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

    public void updateEra(Era era, int rifts, int energy, List<Duplicate> duplicates,
                          boolean hasVortex, boolean playerPresent) {
        SimpleEraView eraView = eraViews.get(era);
        if (eraView != null) {
            int duplicateCount = duplicates != null ? duplicates.size() : 0;
            eraView.updateResources(rifts, energy, duplicateCount, duplicates, hasVortex, playerPresent);
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

    public List<Duplicate> getDuplicatesAt(Era era) {
        SimpleEraView eraView = eraViews.get(era);
        return eraView != null ? eraView.getCurrentDuplicates() : null;
    }
}