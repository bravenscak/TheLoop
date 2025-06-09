package hr.algebra.theloop.engine;

import hr.algebra.theloop.cards.*;
import hr.algebra.theloop.model.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class GameEngine {
    private static final int MAX_DUPLICATES_IN_BAG = 28;

    private GameState gameState;
    private List<Player> players;
    private Random random;
    private int currentPlayerIndex;
    private boolean waitingForPlayerInput;
    private int duplicatesInBag;

    private final DrFooAI drFooAI;
    private final MissionManager missionManager;
    private final CardAcquisitionManager cardAcquisitionManager;

    public GameEngine() {
        this.gameState = new GameState();
        this.players = new ArrayList<>();
        this.random = new Random();
        this.currentPlayerIndex = 0;
        this.waitingForPlayerInput = false;
        this.duplicatesInBag = MAX_DUPLICATES_IN_BAG;

        this.drFooAI = new DrFooAI(random, this);
        this.missionManager = new MissionManager(random);
        this.cardAcquisitionManager = new CardAcquisitionManager(random);
    }

    public void addPlayer(String name, Era startingEra) {
        Player player = new Player(name, startingEra);
        players.add(player);
        giveStartingCards(player);
    }

    private void giveStartingCards(Player player) {
        List<ArtifactCard> allStartingCards = CardFactory.createRandomStartingDeck();

        for (int i = 0; i < Math.min(3, allStartingCards.size()); i++) {
            ArtifactCard card = allStartingCards.get(i);
            card.ready();
            player.addCardToHand(card);
        }

        for (int i = 3; i < allStartingCards.size(); i++) {
            ArtifactCard card = allStartingCards.get(i);
            card.ready();
            player.addCardToDeck(card);
        }
    }

    public void startGame() {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players added!");
        }

        cardAcquisitionManager.initializeAvailableCards(gameState);
        missionManager.initializeMissions(gameState);
        getCurrentPlayer().setCurrentPlayer(true);
        waitingForPlayerInput = true;
    }

    public void processTurn() {
        if (gameState.isGameOver()) return;

        drFooAI.executeDrFooPhase(gameState);
        cardAcquisitionManager.addRandomCardsToEras(gameState, 1);
        waitingForPlayerInput = true;
    }

    public boolean playCard(Player player, int cardIndex, Era targetEra) {
        if (gameState.isGameOver() || !waitingForPlayerInput) {
            return false;
        }

        List<ArtifactCard> hand = player.getHand();
        if (cardIndex < 0 || cardIndex >= hand.size()) {
            return false;
        }

        ArtifactCard card = hand.get(cardIndex);
        if (!card.canExecute(gameState, player)) {
            return false;
        }

        card.execute(gameState, player);
        card.exhaust();

        missionManager.checkAllMissions(gameState, player, card.getClass().getSimpleName());
        checkGameEndConditions();

        return true;
    }

    public boolean movePlayer(Player player, Era targetEra) {
        if (gameState.isGameOver() || !waitingForPlayerInput) {
            return false;
        }

        Era currentEra = player.getCurrentEra();
        if (!currentEra.isAdjacentTo(targetEra)) {
            return false;
        }

        if (player.canUseFreeBattery()) {
            player.useFreeBattery();
            player.moveToEra(targetEra);
        } else if (gameState.getEnergy(currentEra) > 0) {
            gameState.removeEnergy(currentEra, 1);
            player.moveToEra(targetEra);
        } else {
            return false;
        }

        missionManager.checkAllMissions(gameState, player, "Movement");
        return true;
    }

    public boolean acquireCard(Player player) {
        Era playerEra = player.getCurrentEra();
        ArtifactCard acquiredCard = cardAcquisitionManager.acquireCard(playerEra, player);
        return acquiredCard != null;
    }

    public boolean performLoop(Player player, CardDimension dimension) {
        if (!player.canPerformLoop(dimension, gameState)) {
            return false;
        }

        Era playerEra = player.getCurrentEra();
        int loopCost = player.getLoopsPerformedThisTurn() + 1;

        gameState.removeEnergy(playerEra, loopCost);

        for (ArtifactCard card : player.getHand()) {
            if (card.getDimension() == dimension && card.isExhausted()) {
                card.ready();
            }
        }

        player.setLoopsPerformedThisTurn(player.getLoopsPerformedThisTurn() + 1);
        return true;
    }

    public void endPlayerTurn() {
        if (!waitingForPlayerInput) return;

        System.out.println("🎮 === END PLAYER TURN ===");
        waitingForPlayerInput = false;

        for (Player player : players) {
            System.out.println("🃏 Processing turn end for: " + player.getName());

            player.discardHand();

            player.rechargeBatteries();
            player.drawToFullHand();

            player.printDeckState();
        }

        gameState.nextTurn();
        System.out.println("🎮 Turn ended. New turn: " + gameState.getTurnNumber());
    }

    public boolean spawnDuplicate(Era era) {
        if (duplicatesInBag <= 0) {
            return false;
        }

        Duplicate newDuplicate = new Duplicate(era);
        gameState.addDuplicate(era, newDuplicate);
        duplicatesInBag--;
        return true;
    }

    public void destroyDuplicate(Duplicate duplicate) {
        duplicatesInBag++;
    }

    private void checkGameEndConditions() {
        if (gameState.getTotalMissionsCompleted() >= 4) {
            gameState.endGame(GameResult.VICTORY);
            return;
        }

        if (gameState.getVortexCount() >= 3) {
            gameState.endGame(GameResult.DEFEAT_VORTEXES);
            return;
        }

        if (gameState.getCurrentCycle() > 3) {
            gameState.endGame(GameResult.DEFEAT_CYCLES);
            return;
        }
    }

    public int getTotalDuplicatesOnBoard() {
        int total = 0;
        for (Era era : Era.values()) {
            total += gameState.getDuplicateCount(era);
        }
        return total;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public boolean isGameOver() {
        return gameState.isGameOver();
    }

    public boolean isWaitingForPlayerInput() {
        return waitingForPlayerInput;
    }

    public int getDuplicatesInBag() {
        return duplicatesInBag;
    }
}