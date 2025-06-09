package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.io.Serializable;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class ArtifactCard implements Serializable {

    @EqualsAndHashCode.Include
    @NonNull protected String name;

    @NonNull protected String description;
    @NonNull protected CardDimension dimension;

    protected boolean exhausted;
    protected String flavorText;
    protected int cost;

    public ArtifactCard(String name, String description, CardDimension dimension) {
        this.name = name;
        this.description = description;
        this.dimension = dimension;
        this.exhausted = false;
        this.cost = 0;
    }

    public abstract void execute(GameState gameState, Player player);

    public abstract boolean canExecute(GameState gameState, Player player);

    public void exhaust() {
        this.exhausted = true;
    }

    public void ready() {
        this.exhausted = false;
    }

    public boolean isExhausted() {
        return exhausted;
    }

    public boolean isReady() {
        return !exhausted;
    }

    public String getStyleClass() {
        return dimension.getCssClass() + (exhausted ? " exhausted" : " ready");
    }

    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Name: ").append(name).append("\n");
        info.append("Dimension: ").append(dimension.getDisplayName()).append("\n");
        info.append("Description: ").append(description).append("\n");

        if (cost > 0) {
            info.append("Energy Cost: ").append(cost).append("\n");
        }

        if (flavorText != null && !flavorText.isEmpty()) {
            info.append("\"").append(flavorText).append("\"");
        }

        return info.toString();
    }

    public boolean canBeLooped() {
        return dimension.canLoop() && exhausted;
    }

    public String getShortSummary() {
        return name + " (" + dimension.getIcon() + ")";
    }

    @Override
    public String toString() {
        String status = exhausted ? "[Exhausted]" : "[Ready]";
        return name + " " + status + " - " + description;
    }
}