package hr.algebra.theloop.view;

import hr.algebra.theloop.model.Era;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

public class CircularBoardView extends Pane {

    private static final double BOARD_WIDTH = 800;
    private static final double BOARD_HEIGHT = 600;
    private static final double CENTER_X = BOARD_WIDTH / 2;
    private static final double CENTER_Y = BOARD_HEIGHT / 2;

    private Map<Era, EraView> eraViews;
    private Polygon drFooMachine;

    public CircularBoardView() {
        this.eraViews = new HashMap<>();
        setupBoard();
        createEras();
        createDrFooMachine();
    }

    private void setupBoard() {
        setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);
        setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 80%, #16213e 0%, #0f3460 100%); " +
                "-fx-border-color: #eee; -fx-border-width: 2px;");
    }

    private void createEras() {
        double innerRadius = 100;
        double outerRadius = 240;
        double anglePerEra = Math.toRadians(360.0 / 7.0);

        for (Era era : Era.values()) {
            double startAngle = era.ordinal() * anglePerEra - Math.PI / 2;
            EraView eraView = new EraView(era, startAngle, anglePerEra, innerRadius, outerRadius);
            eraViews.put(era, eraView);
            getChildren().add(eraView);
        }
    }

    private void createDrFooMachine() {
        drFooMachine = new Polygon();
        drFooMachine.getPoints().addAll(new Double[]{
                0.0, -40.0, 20.0, -10.0, 10.0, -10.0, 10.0, 20.0, -10.0, 20.0, -10.0, -10.0, -20.0, -10.0
        });
        drFooMachine.setLayoutX(CENTER_X);
        drFooMachine.setLayoutY(CENTER_Y);
        drFooMachine.setFill(Color.web("#ff1493"));
        drFooMachine.setStroke(Color.WHITE);
        drFooMachine.setStrokeWidth(2);
        getChildren().add(drFooMachine);
    }

    public void updateEra(Era era, int rifts, int energy, boolean hasVortex, boolean playerPresent) {
        EraView eraView = eraViews.get(era);
        if (eraView != null) {
            eraView.updateResources(rifts, energy, hasVortex, playerPresent);
        }
    }

    public void pointDrFooAt(Era era) {
        if (drFooMachine != null) {
            double anglePerEra = 360.0 / 7.0;
            double midAngleOffset = anglePerEra / 2;
            double targetAngle = era.ordinal() * anglePerEra - 90 + midAngleOffset;
            drFooMachine.setRotate(targetAngle);
        }
    }

    public EraView getEraView(Era era) {
        return eraViews.get(era);
    }

    public static class EraView extends Pane {
        private final Era era;
        private final Polygon sector;
        private final Text eraNameText;
        private final Text riftsText;
        private final Text energyText;
        private final Text vortexText;
        private final Text playerText;

        public EraView(Era era, double startAngle, double angleSpan, double innerRadius, double outerRadius) {
            this.era = era;

            sector = new Polygon();
            double endAngle = startAngle + angleSpan;

            // A→B→C→D ring sector points
            double ax = CENTER_X + innerRadius * Math.cos(startAngle);
            double ay = CENTER_Y + innerRadius * Math.sin(startAngle);
            double bx = CENTER_X + innerRadius * Math.cos(endAngle);
            double by = CENTER_Y + innerRadius * Math.sin(endAngle);
            double cx = CENTER_X + outerRadius * Math.cos(endAngle);
            double cy = CENTER_Y + outerRadius * Math.sin(endAngle);
            double dx = CENTER_X + outerRadius * Math.cos(startAngle);
            double dy = CENTER_Y + outerRadius * Math.sin(startAngle);

            sector.getPoints().addAll(ax, ay, bx, by, cx, cy, dx, dy);
            sector.setFill(Color.web(era.getColorCode()));
            sector.setStroke(Color.web("#333"));
            sector.setStrokeWidth(2);

            // Text positioning
            double midAngle = startAngle + angleSpan / 2;
            double textRadius = (innerRadius + outerRadius) / 2;
            double textX = CENTER_X + textRadius * Math.cos(midAngle);
            double textY = CENTER_Y + textRadius * Math.sin(midAngle);

            eraNameText = new Text(era.getDisplayName());
            eraNameText.setFill(Color.WHITE);
            eraNameText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            eraNameText.setLayoutX(textX - eraNameText.getBoundsInLocal().getWidth() / 2);
            eraNameText.setLayoutY(textY);

            riftsText = createResourceText("🔴0", textX - 15, textY + 15);
            energyText = createResourceText("🟢1", textX + 5, textY + 15);
            vortexText = createResourceText("⚠️", textX, textY - 15);
            vortexText.setVisible(false);
            playerText = createResourceText("🕵️", textX, textY + 30);
            playerText.setVisible(false);

            getChildren().addAll(sector, eraNameText, riftsText, energyText, vortexText, playerText);
            setOnMouseClicked(event -> System.out.println("Era clicked: " + era.getDisplayName()));
        }

        private Text createResourceText(String text, double x, double y) {
            Text resourceText = new Text(text);
            resourceText.setFill(Color.WHITE);
            resourceText.setFont(Font.font("Arial", FontWeight.BOLD, 9));
            resourceText.setLayoutX(x);
            resourceText.setLayoutY(y);
            return resourceText;
        }

        public void updateResources(int rifts, int energy, boolean hasVortex, boolean playerPresent) {
            riftsText.setText("🔴" + rifts);
            energyText.setText("🟢" + energy);
            vortexText.setVisible(hasVortex);
            playerText.setVisible(playerPresent);

            if (hasVortex) {
                sector.setStroke(Color.RED);
                sector.setStrokeWidth(4);
            } else if (rifts >= 2) {
                sector.setStroke(Color.ORANGE);
                sector.setStrokeWidth(3);
            } else {
                sector.setStroke(Color.web("#333"));
                sector.setStrokeWidth(2);
            }
        }

        public Era getEra() {
            return era;
        }
    }
}