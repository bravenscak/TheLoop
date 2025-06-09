package hr.algebra.theloop.engine;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.cards.CardFactory;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.model.PlayerData;
import hr.algebra.theloop.utils.GameLogger;

import java.util.ArrayList;
import java.util.List;


public class PlayerManager {

    private final List<Player> players;
    private int currentPlayerIndex;

    public PlayerManager() {
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
    }

    public void addPlayer(String name, Era startingEra) {
        Player player = new Player(name, startingEra);
        players.add(player);
        giveStartingCards(player);
        GameLogger.gameFlow("Added player: " + name + " @ " + startingEra.getDisplayName());
    }

    private void giveStartingCards(Player player) {
        List<ArtifactCard> startingCards = CardFactory.createRandomStartingDeck();

        for (int i = 0; i < Math.min(3, startingCards.size()); i++) {
            ArtifactCard card = startingCards.get(i);
            card.ready();
            player.addCardToHand(card);
        }

        for (int i = 3; i < startingCards.size(); i++) {
            ArtifactCard card = startingCards.get(i);
            card.ready();
            player.addCardToDeck(card);
        }
    }

    public void setupInitialPlayer() {
        if (!players.isEmpty()) {
            getCurrentPlayer().setCurrentPlayer(true);
        }
    }

    public void endPlayerTurns() {
        for (Player player : players) {
            player.discardHand();
            player.rechargeBatteries();
            player.drawToFullHand();
        }
    }

    public void restorePlayersFromStates(List<PlayerData> playerStates, int savedPlayerIndex) {
        this.currentPlayerIndex = savedPlayerIndex;
        this.players.clear();

        for (PlayerData playerData : playerStates) {
            Player player = new Player(playerData.getName(), Era.DAWN_OF_TIME);
            playerData.restoreToPlayer(player);
            this.players.add(player);
        }

        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }

        if (!players.isEmpty()) {
            getCurrentPlayer().setCurrentPlayer(true);
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
}