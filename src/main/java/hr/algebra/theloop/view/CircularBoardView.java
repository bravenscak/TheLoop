package hr.algebra.theloop.view;

import hr.algebra.theloop.model.Era;
import javafx.animation.RotateTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class CircularBoardView extends Pane {

    // REDUCED SIZES for 750x750 window
    private static final double BOARD_WIDTH = 450;  // was 800
    private static final double BOARD_HEIGHT = 350; // was 600
    private static final double CENTER_X = BOARD_WIDTH / 2;
    private static final double CENTER_Y = BOARD_HEIGHT / 2;

    private Map<Era, EraView> eraViews;
    private Polygon drFooMachine;

    public CircularBoardView() {
        this.eraViews = new HashMap<>();
        setupBoard();
        createEras();
        createDrFooMachine();

        System.out.println("Compact CircularBoardView created: " + BOARD_WIDTH + "x" + BOARD_HEIGHT);
    }

    private void setupBoard() {
        setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);
        setMaxSize(BOARD_WIDTH, BOARD_HEIGHT);
        setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 80%, #16213e 0%, #0f3460 100%); " +
                "-fx-border-color: #eee; -fx-border-width: 1px;");
    }

    private void createEras() {
        // SMALLER RADII
        double innerRadius = 60;  // was 100
        double outerRadius = 140; // was 240
        double anglePerEra = Math.toRadians(360.0 / 7.0);

        for (Era era : Era.values()) {
            double startAngle = era.ordinal() * anglePerEra - Math.PI / 2;
            EraView eraView = new EraView(era, startAngle, anglePerEra, innerRadius, outerRadius);
            eraViews.put(era, eraView);
            getChildren().add(eraView);
        }

        System.out.println("Created " + eraViews.size() + " compact era views");
    }

    private void createDrFooMachine() {
        drFooMachine = new Polygon();
        // SMALLER ARROW
        drFooMachine.getPoints().addAll(new Double[]{
                0.0, -25.0,   // tip (was -40)
                10.0, -10.0,  // right wing (was 15, -15)
                3.0, -10.0,   // right inner (was 5, -15)
                3.0, 10.0,    // right body (was 5, 15)
                -3.0, 10.0,   // left body (was -5, 15)
                -3.0, -10.0,  // left inner (was -5, -15)
                -10.0, -10.0  // left wing (was -15, -15)
        });

        drFooMachine.setLayoutX(CENTER_X);
        drFooMachine.setLayoutY(CENTER_Y);
        drFooMachine.setFill(Color.web("#ff1493"));
        drFooMachine.setStroke(Color.WHITE);
        drFooMachine.setStrokeWidth(2); // was 3

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#ff1493"));
        glow.setRadius(6); // was 10
        drFooMachine.setEffect(glow);

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
            double halfEraAngle = anglePerEra / 2.0;
            double targetAngle = era.ordinal() * anglePerEra + halfEraAngle;

            RotateTransition rotation = new RotateTransition(Duration.millis(600), drFooMachine); // faster
            rotation.setToAngle(targetAngle);
            rotation.play();
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
            sector.setStrokeWidth(1); // was 2

            double midAngle = startAngle + angleSpan / 2;
            double textRadius = (innerRadius + outerRadius) / 2;
            double textX = CENTER_X + textRadius * Math.cos(midAngle);
            double textY = CENTER_Y + textRadius * Math.sin(midAngle);

            // SMALLER FONTS
            eraNameText = new Text(era.getDisplayName());
            eraNameText.setFill(Color.WHITE);
            eraNameText.setFont(Font.font("Arial", FontWeight.BOLD, 8)); // was 11
            eraNameText.setLayoutX(textX - eraNameText.getBoundsInLocal().getWidth() / 2);
            eraNameText.setLayoutY(textY - 3);

            riftsText = createResourceText("🔴0", textX - 12, textY + 6); // smaller offsets
            energyText = createResourceText("🟢1", textX + 3, textY + 6);

            vortexText = createResourceText("⚠️", textX, textY + 15); // simplified
            vortexText.setFill(Color.RED);
            vortexText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            vortexText.setVisible(false);

            playerText = createResourceText("👤", textX, textY + 25); // simplified, just icon
            playerText.setFill(Color.YELLOW);
            playerText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            playerText.setVisible(false);

            getChildren().addAll(sector, eraNameText, riftsText, energyText, vortexText, playerText);

            // Make era clickable with hover effect
            setOnMouseEntered(event -> {
                if (!hasVortex()) {
                    sector.setStroke(Color.LIGHTBLUE);
                    sector.setStrokeWidth(2);
                }
            });

            setOnMouseExited(event -> updateVisualEffects());

            setOnMouseClicked(event -> {
                System.out.println("🖱️ Era clicked: " + era.getDisplayName());
            });
        }

        private Text createResourceText(String text, double x, double y) {
            Text resourceText = new Text(text);
            resourceText.setFill(Color.WHITE);
            resourceText.setFont(Font.font("Arial", FontWeight.BOLD, 7)); // was 9
            resourceText.setLayoutX(x - resourceText.getBoundsInLocal().getWidth() / 2);
            resourceText.setLayoutY(y);
            return resourceText;
        }

        public void updateResources(int rifts, int energy, boolean hasVortex, boolean playerPresent) {
            riftsText.setText("🔴" + rifts);
            energyText.setText("🟢" + energy);
            vortexText.setVisible(hasVortex);
            playerText.setVisible(playerPresent);

            updateVisualEffects();
        }

        private void updateVisualEffects() {
            boolean hasVortex = vortexText.isVisible();
            boolean playerPresent = playerText.isVisible();
            int rifts = Integer.parseInt(riftsText.getText().substring(2));

            // LIGHTER effects for compact view
            sector.setEffect(null);

            if (hasVortex) {
                sector.setStroke(Color.RED);
                sector.setStrokeWidth(3); // was 5
            } else if (playerPresent) {
                sector.setStroke(Color.YELLOW);
                sector.setStrokeWidth(2); // was 4
            } else if (rifts >= 2) {
                sector.setStroke(Color.ORANGE);
                sector.setStrokeWidth(2); // was 3
            } else {
                sector.setStroke(Color.web("#333"));
                sector.setStrokeWidth(1);
            }
        }

        public Era getEra() {
            return era;
        }

        public boolean hasVortex() {
            return vortexText.isVisible();
        }
    }
}