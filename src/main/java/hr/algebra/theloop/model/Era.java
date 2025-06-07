package hr.algebra.theloop.model;


public enum Era {
    DAWN_OF_TIME("Dawn of Time", "#FFD700", "🌅"),
    MEDIEVAL("Medieval", "#8B4513", "🏰"),
    RENAISSANCE("Renaissance", "#228B22", "🎨"),
    INDUSTRY("Industry", "#2F4F4F", "🏭"),
    GLOBALIZATION("Globalization", "#4169E1", "🌍"),
    ROBOTS("Age of Robots", "#C0C0C0", "🤖"),
    END_OF_TIMES("End of Times", "#9932CC", "🌌");

    private final String displayName;
    private final String colorCode;
    private final String icon;

    Era(String displayName, String colorCode, String icon) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.icon = icon;
    }

    public Era getNext() {
        Era[] eras = values();
        return eras[(this.ordinal() + 1) % eras.length];
    }

    public Era getPrevious() {
        Era[] eras = values();
        return eras[(this.ordinal() - 1 + eras.length) % eras.length];
    }

    public Era getOpposite() {
        Era[] eras = values();
        int oppositeIndex = (this.ordinal() + eras.length / 2) % eras.length;
        return eras[oppositeIndex];
    }

    public boolean isAdjacentTo(Era other) {
        return this.getNext() == other || this.getPrevious() == other;
    }

    public int distanceTo(Era other) {
        Era[] eras = values();
        int distance1 = Math.abs(this.ordinal() - other.ordinal());
        int distance2 = eras.length - distance1;
        return Math.min(distance1, distance2);
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public String getIcon() { return icon; }

    @Override
    public String toString() {
        return displayName;
    }
}