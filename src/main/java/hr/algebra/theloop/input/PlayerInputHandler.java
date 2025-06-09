// 🛠️ JAVAFX DUPLICATE SELECTION - PROPER IMPLEMENTATION
// Two-phase approach: 1) Show dialog, 2) Execute action

// 1. UPDATE PlayerInputHandler.java - Add duplicate selection support
package hr.algebra.theloop.input;

import hr.algebra.theloop.cards.MovementCard;
import hr.algebra.theloop.cards.PushDuplicateCard;
import hr.algebra.theloop.cards.PullDuplicateCard;
import hr.algebra.theloop.cards.DestroyDuplicateCard;
import hr.algebra.theloop.controller.CardController;
import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.Player;
import javafx.scene.control.ChoiceDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerInputHandler {

    private final GameEngine gameEngine;
    private CardController selectedCard = null;
    private int selectedCardIndex = -1;

    public PlayerInputHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public boolean selectCard(CardController cardController, int cardIndex) {

        if (!canAcceptInput()) {
            return false;
        }

        if (cardController.isEmpty() || !cardController.canPlayCard()) {
            return false;
        }

        if (selectedCard != null) {
            selectedCard.setSelected(false);
        }

        if (selectedCard == cardController) {
            clearSelection();
        } else {
            selectedCard = cardController;
            selectedCardIndex = cardIndex;
            cardController.setSelected(true);

            // 🛠️ ADD VISUAL FEEDBACK FOR DIFFERENT CARD TYPES
            if (cardController.getCard() instanceof MovementCard) {
                System.out.println("🎯 Movement card selected - click target era!");
            } else if (isDuplicateCard(cardController.getCard())) {
                System.out.println("🎯 Duplicate card selected - click era with duplicates!");
            }
        }

        return true;
    }

    public boolean handleEraClick(Era era) {
        if (!canAcceptInput()) {
            return false;
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();

        if (hasSelectedCard()) {
            // 🛠️ CHECK CARD TYPE AND HANDLE APPROPRIATELY
            if (selectedCard.getCard() instanceof MovementCard) {
                return handleMovementCard(currentPlayer, selectedCardIndex, era);
            } else if (isDuplicateCard(selectedCard.getCard())) {
                return handleDuplicateCard(currentPlayer, selectedCardIndex, era);
            } else {
                // Regular card play
                return playCardOnEra(currentPlayer, selectedCardIndex, era);
            }
        } else {
            return attemptRegularMovement(currentPlayer, era);
        }
    }

    // 🛠️ NEW METHOD: Check if card is duplicate-related
    private boolean isDuplicateCard(Object card) {
        return card instanceof PushDuplicateCard ||
                card instanceof PullDuplicateCard ||
                card instanceof DestroyDuplicateCard;
    }

    // 🛠️ NEW METHOD: Handle duplicate card with selection dialog
    private boolean handleDuplicateCard(Player player, int cardIndex, Era targetEra) {
        List<Duplicate> duplicatesAtEra = gameEngine.getGameState().getDuplicatesAt(targetEra);

        if (duplicatesAtEra.isEmpty()) {
            System.out.println("❌ No duplicates at " + targetEra.getDisplayName());
            return false;
        }

        // If only one duplicate, execute immediately
        if (duplicatesAtEra.size() == 1) {
            return executeDuplicateCard(player, cardIndex, targetEra, duplicatesAtEra.get(0));
        }

        // Multiple duplicates - show selection dialog
        showDuplicateSelectionDialog(duplicatesAtEra, targetEra, player, cardIndex);
        return true; // Dialog shown, execution will happen in callback
    }

    // 🛠️ NEW METHOD: Show JavaFX duplicate selection dialog
    private void showDuplicateSelectionDialog(List<Duplicate> duplicates, Era era, Player player, int cardIndex) {
        System.out.println("🤔 Multiple duplicates at " + era.getDisplayName() + " - showing selection dialog...");

        List<String> choices = new ArrayList<>();
        for (int i = 0; i < duplicates.size(); i++) {
            Duplicate dup = duplicates.get(i);
            String choice = String.format("%d. %s → destroy @ %s (age: %d)",
                    i + 1,
                    dup.getDisplayName(),
                    dup.getDestroyEra().getDisplayName(),
                    dup.getTurnsActive());
            choices.add(choice);
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Select Duplicate");
        dialog.setHeaderText("Choose which duplicate to manipulate at " + era.getDisplayName() + ":");
        dialog.setContentText("Select duplicate:");

        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(400);

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String selectedChoice = result.get();
            int selectedIndex = choices.indexOf(selectedChoice);
            if (selectedIndex >= 0 && selectedIndex < duplicates.size()) {
                Duplicate selectedDuplicate = duplicates.get(selectedIndex);
                System.out.println("✅ Selected: " + selectedDuplicate.getDisplayName());

                executeDuplicateCard(player, cardIndex, era, selectedDuplicate);
            } else {
                System.out.println("❌ Invalid selection");
                clearSelection();
            }
        } else {
            System.out.println("❌ Duplicate selection cancelled");
            clearSelection();
        }
    }

    private boolean executeDuplicateCard(Player player, int cardIndex, Era era, Duplicate selectedDuplicate) {
        Object card = selectedCard.getCard();

        boolean success = false;
        if (card instanceof PushDuplicateCard) {
            success = ((PushDuplicateCard) card).executeWithDuplicate(gameEngine.getGameState(), player, era, selectedDuplicate);
        } else if (card instanceof PullDuplicateCard) {
            success = ((PullDuplicateCard) card).executeWithDuplicate(gameEngine.getGameState(), player, era, selectedDuplicate);
        } else if (card instanceof DestroyDuplicateCard) {
            success = ((DestroyDuplicateCard) card).executeWithDuplicate(gameEngine.getGameState(), player, era, selectedDuplicate);
        }

        if (success) {
            selectedCard.playCard();
            clearSelection();

            gameEngine.getMissionManager().checkAllMissions(
                    gameEngine.getGameState(), player, card.getClass().getSimpleName());
        }

        return success;
    }

    private boolean handleMovementCard(Player player, int cardIndex, Era targetEra) {
        MovementCard movementCard = (MovementCard) selectedCard.getCard();
        Era currentEra = player.getCurrentEra();

        if (!movementCard.isValidTarget(currentEra, targetEra)) {
            System.out.println("❌ Invalid target era for " + movementCard.getName());
            return false;
        }

        boolean success = movementCard.executeMovement(gameEngine.getGameState(), player, targetEra);

        if (success) {
            selectedCard.playCard();
            clearSelection();

            gameEngine.getMissionManager().checkAllMissions(
                    gameEngine.getGameState(), player, movementCard.getClass().getSimpleName());

            System.out.println("✅ Movement executed: " + currentEra.getDisplayName() + " → " + targetEra.getDisplayName());
        }

        return success;
    }

    private boolean playCardOnEra(Player player, int cardIndex, Era targetEra) {
        boolean success = gameEngine.playCard(player, cardIndex, targetEra);

        if (success) {
            clearSelection();
        }

        return success;
    }

    private boolean attemptRegularMovement(Player player, Era targetEra) {
        if (player.getCurrentEra().equals(targetEra)) {
            return false; // Already there
        }

        return gameEngine.movePlayer(player, targetEra);
    }

    public boolean performLoop() {
        if (!canAcceptInput()) {
            return false;
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();

        boolean hasExhaustedCards = currentPlayer.getHand().stream()
                .anyMatch(card -> card.isExhausted());

        if (!hasExhaustedCards) {
            System.out.println("❌ No exhausted cards to LOOP");
            return false;
        }

        int loopCost = currentPlayer.getLoopsPerformedThisTurn() + 1;
        Era playerEra = currentPlayer.getCurrentEra();
        int availableEnergy = gameEngine.getGameState().getEnergy(playerEra);

        if (availableEnergy < loopCost) {
            System.out.println("❌ Not enough energy for LOOP! Need " + loopCost + ", have " + availableEnergy);
            return false;
        }

        gameEngine.getGameState().removeEnergy(playerEra, loopCost);

        int readiedCards = 0;
        for (var card : currentPlayer.getHand()) {
            if (card.isExhausted()) {
                card.ready();
                readiedCards++;
            }
        }

        currentPlayer.setLoopsPerformedThisTurn(currentPlayer.getLoopsPerformedThisTurn() + 1);

        System.out.println("🔄 LOOP performed! Readied " + readiedCards + " cards for " + loopCost + " energy");
        System.out.println("   Energy remaining: " + gameEngine.getGameState().getEnergy(playerEra));

        return true;
    }

    public boolean endPlayerTurn() {
        if (!gameEngine.isWaitingForPlayerInput()) {
            return false;
        }

        clearSelection();
        gameEngine.endPlayerTurn();
        return true;
    }

    public boolean processNextTurn() {
        if (gameEngine.isWaitingForPlayerInput()) {
            return false;
        }

        gameEngine.processTurn();
        return true;
    }

    public void clearSelection() {
        if (selectedCard != null) {
            selectedCard.setSelected(false);
            selectedCard = null;
            selectedCardIndex = -1;
        }
    }

    private boolean canAcceptInput() {
        return !gameEngine.isGameOver() && gameEngine.isWaitingForPlayerInput();
    }

    public boolean hasSelectedCard() {
        return selectedCard != null && selectedCardIndex >= 0;
    }

    public CardController getSelectedCard() {
        return selectedCard;
    }

    public int getSelectedCardIndex() {
        return selectedCardIndex;
    }
}