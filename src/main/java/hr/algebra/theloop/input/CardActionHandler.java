package hr.algebra.theloop.input;

import hr.algebra.theloop.cards.*;
import hr.algebra.theloop.controller.CardController;
import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.Player;
import javafx.scene.control.ChoiceDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardActionHandler {

    private final GameEngine gameEngine;

    public CardActionHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public boolean handleMovementCard(Player player, int cardIndex, Era targetEra, CardController cardController) {
        MovementCard movementCard = (MovementCard) cardController.getCard();
        Era currentEra = player.getCurrentEra();

        if (!movementCard.isValidTarget(currentEra, targetEra)) {
            return false;
        }

        boolean success = gameEngine.playCard(player, cardIndex, targetEra);

        if (success) {
            cardController.playCard();

            movementCard.executeMovement(gameEngine.getGameState(), player, targetEra);

            gameEngine.getMissionManager().checkAllMissions(
                    gameEngine.getGameState(), player, movementCard.getClass().getSimpleName());

            gameEngine.broadcastGameState("Used " + movementCard.getName(), player.getName());
        }

        return success;
    }

    public boolean handleDuplicateCard(Player player, int cardIndex, Era targetEra, CardController cardController) {
        List<Duplicate> duplicatesAtEra = gameEngine.getGameState().getDuplicatesAt(targetEra);

        if (duplicatesAtEra.isEmpty()) {
            return false;
        }

        if (duplicatesAtEra.size() == 1) {
            return executeDuplicateCard(player, cardIndex, targetEra, duplicatesAtEra.get(0), cardController);
        }

        showDuplicateSelectionDialog(duplicatesAtEra, targetEra, player, cardIndex, cardController);
        return true;
    }

    private void showDuplicateSelectionDialog(List<Duplicate> duplicates, Era era, Player player,
                                              int cardIndex, CardController cardController) {
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
                executeDuplicateCard(player, cardIndex, era, selectedDuplicate, cardController);
            }
        }
    }

    private boolean executeDuplicateCard(Player player, int cardIndex, Era era, Duplicate selectedDuplicate,
                                         CardController cardController) {
        Object card = cardController.getCard();

        boolean success = false;
        success = switch (card) {
            case PushDuplicateCard pushCard ->
                    pushCard.executeWithDuplicate(gameEngine.getGameState(), player, era, selectedDuplicate);
            case PullDuplicateCard pullCard ->
                    pullCard.executeWithDuplicate(gameEngine.getGameState(), player, era, selectedDuplicate);
            case DestroyDuplicateCard destroyCard ->
                    destroyCard.executeWithDuplicate(gameEngine.getGameState(), player, era, selectedDuplicate);
            default -> false;
        };

        if (success) {
            cardController.playCard();
            gameEngine.getMissionManager().checkAllMissions(
                    gameEngine.getGameState(), player, card.getClass().getSimpleName());

            gameEngine.broadcastGameState("Used " + cardController.getCard().getName(), player.getName());
        }

        return success;
    }

    public boolean handleRegularCard(Player player, int cardIndex, Era targetEra) {
        return gameEngine.playCard(player, cardIndex, targetEra);
    }

    public boolean isDuplicateCard(Object card) {
        return card instanceof PushDuplicateCard ||
                card instanceof PullDuplicateCard ||
                card instanceof DestroyDuplicateCard;
    }
}