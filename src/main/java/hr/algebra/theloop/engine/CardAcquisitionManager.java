package hr.algebra.theloop.engine;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.cards.CardFactory;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CardAcquisitionManager {

    private final Random random;
    private final Map<Era, List<ArtifactCard>> availableCards;
    private final int maxCardsPerEra = 2;

    public CardAcquisitionManager(Random random) {
        this.random = random;
        this.availableCards = new ConcurrentHashMap<>();

        for (Era era : Era.values()) {
            availableCards.put(era, new ArrayList<>());
        }
    }

    public void initializeAvailableCards(GameState gameState) {
        for (Era era : Era.values()) {
            if (!gameState.hasVortex(era)) {
                ArtifactCard newCard = CardFactory.createRandomCard();
                availableCards.get(era).add(newCard);
                System.out.println("📋 Initial card at " + era.getDisplayName() + ": " + newCard.getName());
            }
        }
    }

    public void addRandomCardsToEras(GameState gameState, int cardsPerTurn) {
        List<Era> availableEras = new ArrayList<>();
        for (Era era : Era.values()) {
            if (!gameState.hasVortex(era) && availableCards.get(era).size() < maxCardsPerEra) {
                availableEras.add(era);
            }
        }

        if (availableEras.isEmpty()) {
            return;
        }

        for (int i = 0; i < cardsPerTurn && !availableEras.isEmpty(); i++) {
            Era randomEra = availableEras.get(random.nextInt(availableEras.size()));
            ArtifactCard newCard = CardFactory.createRandomCard(); // No era parameter!

            availableCards.get(randomEra).add(newCard);
            System.out.println("📋 New card available at " + randomEra.getDisplayName() + ": " + newCard.getName());

            if (availableCards.get(randomEra).size() >= maxCardsPerEra) {
                availableEras.remove(randomEra);
            }
        }
    }

    public ArtifactCard acquireCard(Era era, Player player) {
        List<ArtifactCard> cardsAtEra = availableCards.get(era);

        if (cardsAtEra.isEmpty()) {
            return null; // No cards available
        }

        ArtifactCard acquiredCard = cardsAtEra.remove(0);
        player.addCardToDeck(acquiredCard);

        return acquiredCard;
    }

    public List<ArtifactCard> getAvailableCardsAt(Era era) {
        return new ArrayList<>(availableCards.get(era));
    }

    public boolean hasAvailableCards(Era era) {
        return !availableCards.get(era).isEmpty();
    }

    public int getTotalAvailableCards() {
        return availableCards.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    public void removeCardsFromVortexEra(Era era) {
        List<ArtifactCard> cardsToRemove = availableCards.get(era);
        if (!cardsToRemove.isEmpty()) {
            System.out.println("🌀 Vortex destroyed " + cardsToRemove.size() + " cards at " + era.getDisplayName());
            cardsToRemove.clear();
        }
    }

    public Map<Era, List<String>> getCardAcquisitionInfo() {
        Map<Era, List<String>> info = new HashMap<>();

        for (Era era : Era.values()) {
            List<String> cardNames = availableCards.get(era).stream()
                    .map(card -> card.getName() + " (" + card.getDimension().getIcon() + ")")
                    .toList();
            info.put(era, cardNames);
        }

        return info;
    }

    public void addCardToEra(Era era, ArtifactCard card) {
        if (availableCards.get(era).size() < maxCardsPerEra) {
            availableCards.get(era).add(card);
        }
    }

    public void clearAllCards() {
        for (Era era : Era.values()) {
            availableCards.get(era).clear();
        }
    }

    public void addPremiumCardsEvent() {
        System.out.println("🌟 PREMIUM CARDS EVENT!");

        for (Era era : Era.values()) {
            if (availableCards.get(era).size() < maxCardsPerEra) {
                ArtifactCard premiumCard = createPremiumCard();
                availableCards.get(era).add(premiumCard);
                System.out.println("✨ Premium card at " + era.getDisplayName() + ": " + premiumCard.getName());
            }
        }
    }

    private ArtifactCard createPremiumCard() {
        int cardType = random.nextInt(3);

        return switch (cardType) {
            case 0 -> hr.algebra.theloop.cards.EnergyCard.createEnergyBoost();
            case 1 -> hr.algebra.theloop.cards.RiftCard.createQuantumEraser();
            case 2 -> hr.algebra.theloop.cards.MovementCard.createQuantumLeap();
            default -> CardFactory.createRandomCard();
        };
    }
}