package hr.algebra.theloop.controller;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Mission;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.view.CircularBoardView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainGameController implements Initializable {

    @FXML private CircularBoardView circularBoard;
    @FXML private HBox playerHandBox;

    // Card controllers - injected by FXML
    @FXML private CardController card1Controller;
    @FXML private CardController card2Controller;
    @FXML private CardController card3Controller;

    @FXML private Label turnLabel;
    @FXML private Label drFooLocationLabel;
    @FXML private Label cycleLabel;
    @FXML private Label missionsLabel;
    @FXML private Label vortexLabel;
    @FXML private Label playerNameLabel;
    @FXML private Label playerLocationLabel;

    @FXML private Button endTurnButton;
    @FXML private Button loopButton;
    @FXML private ListView<String> missionsList;

    private GameEngine gameEngine;
    private boolean gameRunning;

    // Card selection state
    private CardController selectedCard = null;
    private int selectedCardIndex = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupGame();
        setupCardClickHandlers();
        setupEraClickHandlers(); // FIXED VERSION
        updateUI();

        System.out.println("MainGameController initialized with FIXED era mapping");
    }

    private void setupGame() {
        gameEngine = new GameEngine();
        gameEngine.addPlayer("Time Agent Bruno", Era.DAWN_OF_TIME);
        gameEngine.startGame();
        gameRunning = true;
    }

    private void setupCardClickHandlers() {
        if (card1Controller != null) {
            card1Controller.setClickHandler(() -> selectCard(card1Controller, 0));
        }
        if (card2Controller != null) {
            card2Controller.setClickHandler(() -> selectCard(card2Controller, 1));
        }
        if (card3Controller != null) {
            card3Controller.setClickHandler(() -> selectCard(card3Controller, 2));
        }
    }

    // COMPLETELY REWRITTEN ERA CLICK HANDLING
    private void setupEraClickHandlers() {
        if (circularBoard != null) {
            // DIRECT ERA MAPPING - no loops, explicit mapping
            CircularBoardView.EraView dawnView = circularBoard.getEraView(Era.DAWN_OF_TIME);
            if (dawnView != null) {
                dawnView.setOnMouseClicked(event -> {
                    System.out.println("🖱️ DAWN OF TIME clicked");
                    handleEraClick(Era.DAWN_OF_TIME);
                    event.consume(); // Prevent multiple events
                });
            }

            CircularBoardView.EraView medievalView = circularBoard.getEraView(Era.MEDIEVAL);
            if (medievalView != null) {
                medievalView.setOnMouseClicked(event -> {
                    System.out.println("🖱️ MEDIEVAL clicked");
                    handleEraClick(Era.MEDIEVAL);
                    event.consume();
                });
            }

            CircularBoardView.EraView renaissanceView = circularBoard.getEraView(Era.RENAISSANCE);
            if (renaissanceView != null) {
                renaissanceView.setOnMouseClicked(event -> {
                    System.out.println("🖱️ RENAISSANCE clicked");
                    handleEraClick(Era.RENAISSANCE);
                    event.consume();
                });
            }

            CircularBoardView.EraView industryView = circularBoard.getEraView(Era.INDUSTRY);
            if (industryView != null) {
                industryView.setOnMouseClicked(event -> {
                    System.out.println("🖱️ INDUSTRY clicked");
                    handleEraClick(Era.INDUSTRY);
                    event.consume();
                });
            }

            CircularBoardView.EraView globalizationView = circularBoard.getEraView(Era.GLOBALIZATION);
            if (globalizationView != null) {
                globalizationView.setOnMouseClicked(event -> {
                    System.out.println("🖱️ GLOBALIZATION clicked");
                    handleEraClick(Era.GLOBALIZATION);
                    event.consume();
                });
            }

            CircularBoardView.EraView robotsView = circularBoard.getEraView(Era.ROBOTS);
            if (robotsView != null) {
                robotsView.setOnMouseClicked(event -> {
                    System.out.println("🖱️ ROBOTS clicked");
                    handleEraClick(Era.ROBOTS);
                    event.consume();
                });
            }

            CircularBoardView.EraView endTimesView = circularBoard.getEraView(Era.END_OF_TIMES);
            if (endTimesView != null) {
                endTimesView.setOnMouseClicked(event -> {
                    System.out.println("🖱️ END OF TIMES clicked");
                    handleEraClick(Era.END_OF_TIMES);
                    event.consume();
                });
            }

            System.out.println("🔧 Era click handlers FIXED - explicit mapping for all 7 eras");
        }
    }

    private void selectCard(CardController cardController, int cardIndex) {
        if (!gameRunning || gameEngine.isGameOver() || !gameEngine.isWaitingForPlayerInput()) {
            System.out.println("❌ Cannot select card - not player turn");
            return;
        }

        // FIXED: Check if card can be played before selection
        if (cardController.isEmpty() || !cardController.canPlayCard()) {
            System.out.println("❌ Cannot select card - empty or exhausted");
            return;
        }

        // Deselect previous card
        if (selectedCard != null) {
            selectedCard.setSelected(false);
        }

        // Select new card
        if (selectedCard == cardController) {
            // Deselect if clicking same card
            selectedCard = null;
            selectedCardIndex = -1;
            System.out.println("🃏 Card deselected");
        } else {
            selectedCard = cardController;
            selectedCardIndex = cardIndex;
            cardController.setSelected(true);

            ArtifactCard card = cardController.getCard();
            if (card != null) {
                System.out.println("🃏 Selected: " + card.getName() + " - Click era to play!");
            }
        }
    }

    private void handleEraClick(Era era) {
        Player currentPlayer = gameEngine.getCurrentPlayer();

        if (!gameRunning || gameEngine.isGameOver()) {
            return;
        }

        if (!gameEngine.isWaitingForPlayerInput()) {
            System.out.println("❌ Not player turn - click 'End Turn' first");
            return;
        }

        if (selectedCard != null && selectedCardIndex >= 0) {
            // Play selected card on this era
            playCardOnEra(currentPlayer, selectedCardIndex, era);
        } else {
            // Try to move to this era
            attemptMovement(currentPlayer, era);
        }
    }

    private void playCardOnEra(Player player, int cardIndex, Era targetEra) {
        boolean success = gameEngine.playCard(player, cardIndex, targetEra);

        if (success) {
            // Deselect card
            if (selectedCard != null) {
                selectedCard.setSelected(false);
                selectedCard = null;
                selectedCardIndex = -1;
            }

            updateUI();
            System.out.println("✅ Card played successfully!");

            if (gameEngine.isGameOver()) {
                gameRunning = false;
                endTurnButton.setDisable(true);
            }
        } else {
            System.out.println("❌ Failed to play card");
        }
    }

    private void attemptMovement(Player player, Era targetEra) {
        if (player.getCurrentEra().equals(targetEra)) {
            System.out.println("ℹ️ Already at " + targetEra.getDisplayName());
            return;
        }

        boolean success = gameEngine.movePlayer(player, targetEra);

        if (success) {
            updateUI();
            System.out.println("✅ Moved to " + targetEra.getDisplayName());
        } else {
            System.out.println("❌ Cannot move to " + targetEra.getDisplayName());
        }
    }

    @FXML
    private void endTurn() {
        if (!gameRunning || gameEngine.isGameOver()) {
            return;
        }

        if (gameEngine.isWaitingForPlayerInput()) {
            // End player actions phase
            gameEngine.endPlayerTurn();
            System.out.println("🔄 Player turn ended");
        } else {
            // Start next Dr. Foo phase
            System.out.println("🎮 Processing Dr. Foo turn...");
            gameEngine.processTurn();
        }

        updateUI();

        if (gameEngine.isGameOver()) {
            gameRunning = false;
            endTurnButton.setDisable(true);
        }
    }

    @FXML
    private void performLoop() {
        if (!gameEngine.isWaitingForPlayerInput()) {
            System.out.println("❌ Not player turn");
            return;
        }

        // TODO: Implement LOOP dialog with dimension selection
        System.out.println("🔄 LOOP button clicked - dialog not implemented yet");
    }

    @FXML
    private void saveGame() {
        System.out.println("💾 Save game - not implemented yet");
    }

    @FXML
    private void loadGame() {
        System.out.println("📁 Load game - not implemented yet");
    }

    @FXML
    private void newGame() {
        System.out.println("🆕 New game started!");
        setupGame();
        selectedCard = null;
        selectedCardIndex = -1;
        updateUI();
        endTurnButton.setDisable(false);
        gameRunning = true;
    }

    private void updateUI() {
        updateStatusLabels();
        updateBoard();
        updatePlayerHand();
        updateMissions();
        updateButtons();
    }

    private void updateStatusLabels() {
        GameState state = gameEngine.getGameState();

        turnLabel.setText("Turn: " + state.getTurnNumber());
        drFooLocationLabel.setText("Dr. Foo @ " + state.getDrFooPosition().getDisplayName());
        cycleLabel.setText("Cycle: " + state.getCurrentCycle() + "/3");
        missionsLabel.setText("Missions: " + state.getTotalMissionsCompleted() + "/4");
        vortexLabel.setText("Vortexes: " + state.getVortexCount() + "/4");
    }

    private void updateBoard() {
        if (circularBoard == null) {
            return;
        }

        GameState state = gameEngine.getGameState();
        Player currentPlayer = gameEngine.getCurrentPlayer();

        // Update all eras
        for (Era era : Era.values()) {
            boolean playerHere = era.equals(currentPlayer.getCurrentEra());

            circularBoard.updateEra(era,
                    state.getRifts(era),
                    state.getEnergy(era),
                    state.hasVortex(era),
                    playerHere);
        }

        // Point Dr. Foo machine at current era
        circularBoard.pointDrFooAt(state.getDrFooPosition());
    }

    private void updatePlayerHand() {
        Player currentPlayer = gameEngine.getCurrentPlayer();
        List<ArtifactCard> hand = currentPlayer.getHand();

        // Update player info
        playerNameLabel.setText(currentPlayer.getName());
        playerLocationLabel.setText("@ " + currentPlayer.getCurrentEra().getDisplayName());

        // Update card displays
        updateCardController(card1Controller, hand, 0);
        updateCardController(card2Controller, hand, 1);
        updateCardController(card3Controller, hand, 2);

        System.out.println("🃏 Hand updated: " + hand.size() + " cards");
    }

    private void updateCardController(CardController controller, List<ArtifactCard> hand, int index) {
        if (controller == null) return;

        if (hand.size() > index) {
            controller.setCard(hand.get(index));
        } else {
            controller.setEmpty();
        }
    }

    private void updateMissions() {
        if (missionsList == null) return;

        missionsList.getItems().clear();

        GameState state = gameEngine.getGameState();
        List<Mission> missions = state.getActiveMissions();

        if (missions.isEmpty()) {
            missionsList.getItems().add("No active missions");
        } else {
            for (Mission mission : missions) {
                missionsList.getItems().add(mission.toString());
            }
        }
    }

    private void updateButtons() {
        if (gameEngine.isGameOver()) {
            endTurnButton.setDisable(true);
            loopButton.setDisable(true);
        } else {
            endTurnButton.setDisable(false);

            // FIXED: Proper button text based on game state
            if (gameEngine.isWaitingForPlayerInput()) {
                endTurnButton.setText("End Player Turn");
                loopButton.setDisable(false);
            } else {
                endTurnButton.setText("Next Turn");
                loopButton.setDisable(true);
            }
        }
    }
}