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
    private CardController selectedCard = null;
    private int selectedCardIndex = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupGame();
        setupCardClickHandlers();
        setupEraClickHandlers();
        updateUI();
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

    private void setupEraClickHandlers() {
        if (circularBoard != null) {
            for (Era era : Era.values()) {
                CircularBoardView.SimpleEraView eraView = circularBoard.getEraView(era);
                if (eraView != null) {
                    eraView.setOnMouseClicked(event -> {
                        handleEraClick(era);
                        event.consume();
                    });
                }
            }
        }
    }

    private void selectCard(CardController cardController, int cardIndex) {
        if (!gameRunning || gameEngine.isGameOver() || !gameEngine.isWaitingForPlayerInput()) {
            return;
        }

        if (cardController.isEmpty() || !cardController.canPlayCard()) {
            return;
        }

        if (selectedCard != null) {
            selectedCard.setSelected(false);
        }

        if (selectedCard == cardController) {
            selectedCard = null;
            selectedCardIndex = -1;
        } else {
            selectedCard = cardController;
            selectedCardIndex = cardIndex;
            cardController.setSelected(true);
        }
    }

    private void handleEraClick(Era era) {
        Player currentPlayer = gameEngine.getCurrentPlayer();

        if (!gameRunning || gameEngine.isGameOver() || !gameEngine.isWaitingForPlayerInput()) {
            return;
        }

        if (selectedCard != null && selectedCardIndex >= 0) {
            playCardOnEra(currentPlayer, selectedCardIndex, era);
        } else {
            attemptMovement(currentPlayer, era);
        }
    }

    private void playCardOnEra(Player player, int cardIndex, Era targetEra) {
        boolean success = gameEngine.playCard(player, cardIndex, targetEra);

        if (success) {
            if (selectedCard != null) {
                selectedCard.setSelected(false);
                selectedCard = null;
                selectedCardIndex = -1;
            }
            updateUI();

            if (gameEngine.isGameOver()) {
                gameRunning = false;
                endTurnButton.setDisable(true);
            }
        }
    }

    private void attemptMovement(Player player, Era targetEra) {
        if (player.getCurrentEra().equals(targetEra)) {
            return;
        }

        boolean success = gameEngine.movePlayer(player, targetEra);
        if (success) {
            updateUI();
        }
    }

    @FXML
    private void endTurn() {
        if (!gameRunning || gameEngine.isGameOver()) {
            return;
        }

        if (gameEngine.isWaitingForPlayerInput()) {
            if (selectedCard != null) {
                selectedCard.setSelected(false);
                selectedCard = null;
                selectedCardIndex = -1;
            }

            gameEngine.endPlayerTurn();
        } else {
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
        if (!gameRunning || gameEngine.isGameOver() || !gameEngine.isWaitingForPlayerInput()) {
            return;
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();

        boolean hasExhaustedCards = currentPlayer.getHand().stream()
                .anyMatch(card -> card.isExhausted());

        if (!hasExhaustedCards) {
            System.out.println("❌ No exhausted cards to LOOP");
            return;
        }

        int loopCost = currentPlayer.getLoopsPerformedThisTurn() + 1;
        Era playerEra = currentPlayer.getCurrentEra();
        int availableEnergy = gameEngine.getGameState().getEnergy(playerEra);

        if (availableEnergy < loopCost) {
            System.out.println("❌ Not enough energy for LOOP! Need " + loopCost + ", have " + availableEnergy);
            return;
        }

        gameEngine.getGameState().removeEnergy(playerEra, loopCost);

        int readiedCards = 0;
        for (ArtifactCard card : currentPlayer.getHand()) {
            if (card.isExhausted()) {
                card.ready();
                readiedCards++;
            }
        }

        currentPlayer.setLoopsPerformedThisTurn(currentPlayer.getLoopsPerformedThisTurn() + 1);

        System.out.println("🔄 LOOP performed! Readied " + readiedCards + " cards for " + loopCost + " energy");
        System.out.println("   Energy remaining: " + gameEngine.getGameState().getEnergy(playerEra));

        updatePlayerHand();
        updateUI();
    }

    @FXML
    private void saveGame() {
    }

    @FXML
    private void loadGame() {
    }

    @FXML
    private void newGame() {
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

        for (Era era : Era.values()) {
            boolean playerHere = era.equals(currentPlayer.getCurrentEra());
            circularBoard.updateEra(era, state.getRifts(era), state.getEnergy(era),
                    state.hasVortex(era), playerHere);
        }

        circularBoard.pointDrFooAt(state.getDrFooPosition());
    }

    private void updatePlayerHand() {
        Player currentPlayer = gameEngine.getCurrentPlayer();
        List<ArtifactCard> hand = currentPlayer.getHand();

        playerNameLabel.setText(currentPlayer.getName());
        playerLocationLabel.setText("@ " + currentPlayer.getCurrentEra().getDisplayName());

        updateCardController(card1Controller, hand, 0);
        updateCardController(card2Controller, hand, 1);
        updateCardController(card3Controller, hand, 2);
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