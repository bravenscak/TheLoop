package hr.algebra.theloop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Duplicate implements Serializable {

    @EqualsAndHashCode.Include
    private String id;

    private Era spawnEra;
    private Era destroyEra;
    private Era currentEra;
    private int turnsActive;

    public Duplicate(Era spawnEra) {
        this.id = "DUP_" + UUID.randomUUID().toString().substring(0, 8);
        this.spawnEra = spawnEra;
        this.currentEra = spawnEra;
        this.destroyEra = calculateDestroyEra(spawnEra);
        this.turnsActive = 0;
    }

    public Duplicate(Era originalSpawnEra, Era newCurrentEra, int turnsActive) {
        this.id = "DUP_" + UUID.randomUUID().toString().substring(0, 8);
        this.spawnEra = originalSpawnEra;
        this.destroyEra = calculateDestroyEra(originalSpawnEra);
        this.currentEra = newCurrentEra;
        this.turnsActive = turnsActive;
    }

    public Duplicate(Duplicate other) {
        this.id = other.id;
        this.spawnEra = other.spawnEra;
        this.destroyEra = other.destroyEra; // ✅ Čuva originalni destroy era
        this.currentEra = other.currentEra;
        this.turnsActive = other.turnsActive;
    }

    private Era calculateDestroyEra(Era spawn) {
        return spawn.getOpposite();
    }

    public void moveTo(Era newEra) {
        this.currentEra = newEra;
    }

    public boolean pushTo(Era targetEra) {
        if (currentEra.isAdjacentTo(targetEra)) {
            this.currentEra = targetEra;
            return true;
        }
        return false;
    }

    public boolean canBeDestroyedAt(Era era) {
        return era.equals(destroyEra);
    }

    public boolean isAtDestructionEra() {
        return currentEra.equals(destroyEra);
    }

    public void ageTurn() {
        turnsActive++;
    }

    public int distanceToDestruction() {
        return currentEra.distanceTo(destroyEra);
    }

    public boolean isAdjacentToDestruction() {
        return currentEra.isAdjacentTo(destroyEra);
    }

    public String getDisplayName() {
        return "Dr. Foo #" + id.substring(4);
    }

    public String getIcon() {
        return "👤";
    }

    public String getMonocleDisplay() {
        return destroyEra.getIcon() + " " + destroyEra.getDisplayName();
    }

    @Override
    public String toString() {
        return String.format("Duplicate[%s: %s → %s → %s]",
                id, spawnEra.getDisplayName(),
                currentEra.getDisplayName(),
                destroyEra.getDisplayName());
    }
}