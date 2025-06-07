package hr.algebra.theloop.engine;

import hr.algebra.theloop.cards.energy.EnergyCard;
import hr.algebra.theloop.cards.movement.MovementCard;
import hr.algebra.theloop.cards.rift.RiftCard;
import hr.algebra.theloop.model.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class GameEngine {
    private GameState gameState;
    private List<Player> players;
    private Random random;
    private int currentPlayerIndex;

    public GameEngine() {
        this.gameState = new GameState();
        this.players = new ArrayList<>();
        this.random = new Random();
        this.currentPlayerIndex = 0;
    }

    public void addPlayer(String name, Era startingEra) {
        Player player = new Player(name, startingEra);
        players.add(player);
        giveStartingCards(player);
    }

    private void giveStartingCards(Player player) {
        player.addCardToHand(new EnergyCard("Basic Energy", Era.DAWN_OF_TIME, 1));
        player.addCardToHand(new MovementCard("Basic Move", Era.MEDIEVAL, 1));
        player.addCardToHand(new RiftCard("Basic Repair", Era.RENAISSANCE, 1));
    }

    public void startGame() {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players added!");
        }

        players.get(0).setCurrentPlayer(true);
        System.out.println("🎮 THE LOOP GAME STARTED!");
        System.out.println("Players: " + players.size());
        printGameStatus();
    }

    public void processTurn() {
        if (gameState.isGameOver()) {
            return;
        }

        drFooPhase();

        for (Player player : players) {
            if (gameState.isGameOver()) break;
            playerPhase(player);
        }

        endTurnPhase();
    }

    private void drFooPhase() {
        System.out.println("\n--- DR. FOO PHASE ---");

        Era oldPosition = gameState.getDrFooPosition();
        gameState.moveDrFoo();
        Era newPosition = gameState.getDrFooPosition();

        int riftsToAdd = 2 + gameState.getDuplicateCount(newPosition);
        gameState.addRifts(newPosition, riftsToAdd);

        System.out.println("Dr. Foo moves: " + oldPosition.getDisplayName() + " → " + newPosition.getDisplayName());
        System.out.println("Added " + riftsToAdd + " rifts to " + newPosition.getDisplayName());

        if (gameState.getRifts(newPosition) >= 3) {
            System.out.println("⚠️ VORTEX created!");
        }
    }

    private void playerPhase(Player player) {
        System.out.println("\n--- " + player.getName().toUpperCase() + "'S TURN ---");
        player.rechargeBatteries();

        var readyCards = player.getReadyCards();
        if (!readyCards.isEmpty()) {
            var card = readyCards.get(0);
            if (card.canExecute(gameState, player)) {
                card.execute(gameState, player);
                player.playCard(card);
            }
        }

        if (gameState.getEnergy(player.getCurrentEra()) < 2) {
            Era next = player.getCurrentEra().getNext();
            if (gameState.getEnergy(next) > gameState.getEnergy(player.getCurrentEra())) {
                player.moveToEra(next);
                System.out.println("🚶 " + player.getName() + " moved to " + next.getDisplayName() + " for energy");
            }
        }
    }

    private void endTurnPhase() {
        gameState.nextTurn();

        if (gameState.getVortexCount() >= 3) {
            gameState.endGame(GameResult.DEFEAT_VORTEXES);
        }

        for (Player player : players) {
            player.fillHandToThree();
        }

        printGameStatus();
    }

    public void printGameStatus() {
        System.out.println("\n=== GAME STATUS ===");
        System.out.println(gameState.toString());

        for (Player player : players) {
            System.out.println("  " + player.toString());
        }

        System.out.println("Vortexes: " + gameState.getVortexCount() + "/3");

        if (gameState.isGameOver()) {
            System.out.println("\n" + gameState.getGameResult().getMessage());
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public boolean isGameOver() {
        return gameState.isGameOver();
    }
}