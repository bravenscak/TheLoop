package hr.algebra.theloop.model;

import hr.algebra.theloop.cards.ArtifactCard;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PlayerDeckManager implements Serializable {

    private List<ArtifactCard> hand;
    private List<ArtifactCard> deck;
    private List<ArtifactCard> discardPile;

    public PlayerDeckManager() {
        this.hand = new ArrayList<>();
        this.deck = new ArrayList<>();
        this.discardPile = new ArrayList<>();
    }

    public void addCardToHand(ArtifactCard card) {
        if (hand.size() < 3) {
            hand.add(card);
        }
    }

    public void addCardToDeck(ArtifactCard card) {
        deck.add(card);
    }

    public void drawToFullHand() {
        while (hand.size() < 3) {
            if (deck.isEmpty() && !discardPile.isEmpty()) {
                for (ArtifactCard card : discardPile) {
                    card.ready();
                }
                deck.addAll(discardPile);
                discardPile.clear();
                Collections.shuffle(deck);
            }

            if (deck.isEmpty()) break;

            ArtifactCard card = deck.remove(0);
            card.ready();
            hand.add(card);
        }
    }

    public void discardHand() {
        discardPile.addAll(hand);
        hand.clear();
    }

    public List<ArtifactCard> getReadyCards() {
        return hand.stream()
                .filter(card -> !card.isExhausted())
                .collect(Collectors.toList());
    }

    public List<ArtifactCard> getExhaustedCards() {
        return hand.stream()
                .filter(ArtifactCard::isExhausted)
                .collect(Collectors.toList());
    }

    public boolean hasPlayableCards(GameState gameState, Player player) {
        return hand.stream()
                .anyMatch(card -> !card.isExhausted() && card.canExecute(gameState, player));
    }

    public List<ArtifactCard> getPlayableCards(GameState gameState, Player player) {
        return hand.stream()
                .filter(card -> !card.isExhausted() && card.canExecute(gameState, player))
                .collect(Collectors.toList());
    }

    public int getHandSize() { return hand.size(); }
    public int getDeckSize() { return deck.size(); }
    public int getDiscardPileSize() { return discardPile.size(); }
}