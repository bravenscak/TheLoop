package hr.algebra.theloop.cards.energy;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.cards.CardDimension;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

public class EnergyCard extends ArtifactCard {
    private final int energyAmount;

    public EnergyCard(String name, Era originalEra, int energyAmount) {
        super(name, "Add " + energyAmount + " energy to current era", originalEra, CardDimension.STAR);
        this.energyAmount = energyAmount;
    }

    @Override
    public void execute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        gameState.addEnergy(playerEra, energyAmount);
        exhaust();
        System.out.println("⚡ " + getName() + ": Added " + energyAmount + " energy to " + playerEra.getDisplayName());
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        return !isExhausted();
    }
}