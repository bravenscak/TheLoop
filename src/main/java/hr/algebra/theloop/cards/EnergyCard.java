package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.utils.GameLogger;

import java.util.Objects;

public class EnergyCard extends ArtifactCard {

    public enum EnergyEffect {
        ADD_TO_CURRENT,
        ADD_TO_ADJACENT,
        STEAL_FROM_DR_FOO
    }

    private final int energyAmount;
    private final EnergyEffect effect;

    public EnergyCard(String name, int energyAmount) {
        super(name, "Add " + energyAmount + " energy to current era", CardDimension.STAR);
        this.energyAmount = energyAmount;
        this.effect = EnergyEffect.ADD_TO_CURRENT;
    }

    public EnergyCard(String name, int energyAmount, EnergyEffect effect) {
        super(name, generateDescription(energyAmount, effect), CardDimension.STAR);
        this.energyAmount = energyAmount;
        this.effect = effect;
    }

    private static String generateDescription(int amount, EnergyEffect effect) {
        return switch (effect) {
            case ADD_TO_CURRENT -> "Add " + amount + " energy to current era";
            case ADD_TO_ADJACENT -> "Add " + amount + " energy to adjacent eras";
            case STEAL_FROM_DR_FOO -> "Steal " + amount + " energy from Dr. Foo's era";
        };
    }

    @Override
    public void execute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();

        switch (effect) {
            case ADD_TO_CURRENT -> {
                gameState.addEnergy(playerEra, energyAmount);
                GameLogger.playerAction(player.getName(), "Added " + energyAmount + " energy to " + playerEra.getDisplayName());
            }
            case ADD_TO_ADJACENT -> {
                Era prevEra = playerEra.getPrevious();
                Era nextEra = playerEra.getNext();
                gameState.addEnergy(prevEra, energyAmount);
                gameState.addEnergy(nextEra, energyAmount);
                GameLogger.playerAction(player.getName(), "Added " + energyAmount + " energy to adjacent eras");
            }
            case STEAL_FROM_DR_FOO -> {
                Era drFooEra = gameState.getDrFooPosition();
                int availableEnergy = gameState.getEnergy(drFooEra);
                int actualStolen = Math.min(energyAmount, availableEnergy);

                if (actualStolen > 0) {
                    gameState.removeEnergy(drFooEra, actualStolen);
                    gameState.addEnergy(playerEra, actualStolen);
                    GameLogger.playerAction(player.getName(), "Stole " + actualStolen + " energy from Dr. Foo");
                }
            }
        }
        exhaust();
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        if (isExhausted()) {
            return false;
        }

        if (effect == EnergyEffect.STEAL_FROM_DR_FOO) {
            Era drFooEra = gameState.getDrFooPosition();
            return gameState.getEnergy(drFooEra) > 0;
        }

        return true;
    }

    public static EnergyCard createBasicEnergy() {
        return new EnergyCard("Basic Energy", 1);
    }

    public static EnergyCard createEnergyBoost() {
        return new EnergyCard("Energy Boost", 3);
    }

    public static EnergyCard createAdjacentEnergy() {
        return new EnergyCard("Adjacent Energy", 1, EnergyEffect.ADD_TO_ADJACENT);
    }

    public static EnergyCard createEnergySiphon() {
        return new EnergyCard("Energy Siphon", 2, EnergyEffect.STEAL_FROM_DR_FOO);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;

        EnergyCard that = (EnergyCard) obj;
        return energyAmount == that.energyAmount &&
                effect == that.effect;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), energyAmount, effect);
    }
}