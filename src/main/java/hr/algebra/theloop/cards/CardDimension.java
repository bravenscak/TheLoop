package hr.algebra.theloop.cards;

public enum CardDimension {
    SPIRAL("Spiral", "Movement & Navigation", "#FFD700", "🌀", true),
    STAR("Star", "Energy & Power", "#00BFFF", "⭐", true),
    STRIPE("Stripe", "Rift & Combat", "#FF6347", "🔥", true),
    BLACK_HOLE("Black Hole", "Special Effects", "#8A2BE2", "🕳️", false);

    private final String name;
    private final String description;
    private final String colorCode;
    private final String icon;
    private final boolean canLoop;

    CardDimension(String name, String description, String colorCode, String icon, boolean canLoop) {
        this.name = name;
        this.description = description;
        this.colorCode = colorCode;
        this.icon = icon;
        this.canLoop = canLoop;
    }

    public boolean canLoop() {
        return canLoop;
    }

    public String getDisplayName() {
        return name;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getCssClass() {
        return "card-" + name.toLowerCase().replace(" ", "-");
    }

    @Override
    public String toString() {
        return name;
    }
}