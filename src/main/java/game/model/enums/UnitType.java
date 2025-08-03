package game.model.enums;

/**
 * Defines the different types of units available in the game.
 * Each unit type has specific stats and a visual representation.
 */
public enum UnitType {
    SCOUT("Sc", 0xFFFFFFFF, 12, 8),
    SETTLER("St", 0xFFFFCC00, 8, 6);

    public final String symbol;
    public final int unitColor;
    public final int maxMovementBudget;
    public final int maxVisibilityBudget;

    /**
     * Constructor for a UnitType.
     * @param sym The text symbol used to represent the unit on the map (e.g., "Sc").
     * @param col The ARGB color code for the unit's symbol.
     * @param maxMovement The maximum number of movement points the unit starts with.
     * @param maxVision The maximum sight range of the unit.
     */
    UnitType(String sym, int col, int maxMovement, int maxVision) {
        this.symbol = sym;
        this.unitColor = col;
        this.maxMovementBudget = maxMovement;
        this.maxVisibilityBudget = maxVision;
    }
}
