package game.model;

import game.model.enums.UnitCategory;

/**
 * UnitType.java
 *
 * PURPOSE:
 * Defines the different types of units available in the game with their
 * complete stats including combat attributes, movement, and visual properties.
 *
 * DESIGN PRINCIPLES:
 * - Complete Definition: Each unit type contains all necessary attributes
 * - Category-Based: Uses UnitCategory to determine combat capabilities
 * - Extensible: Easy to add new unit types with different attributes
 */
public enum UnitType {
    // Civilian units
    SETTLER("St", 0xFFFFCC00, UnitCategory.CIVILIAN, 8, 6, 0, 100),
    
    // Land melee units  
    SCOUT("Sc", 0xFFFFFFFF, UnitCategory.LAND_MELEE, 12, 8, 15, 80),
    WARRIOR("Wr", 0xFFFF6666, UnitCategory.LAND_MELEE, 6, 4, 25, 120);

    public final String symbol;
    public final int unitColor;
    public final UnitCategory category;
    public final int maxMovementBudget;
    public final int maxVisibilityBudget;
    public final int attackStrength;
    public final int maxHealth;

    /**
     * Constructor for a UnitType.
     * @param sym The text symbol used to represent the unit on the map (e.g., "Sc").
     * @param col The ARGB color code for the unit's symbol.
     * @param cat The category determining combat capabilities.
     * @param maxMovement The maximum number of movement points the unit starts with.
     * @param maxVision The maximum sight range of the unit.
     * @param attack The attack strength for combat calculations.
     * @param health The maximum health points.
     */
    UnitType(String sym, int col, UnitCategory cat, int maxMovement, int maxVision, int attack, int health) {
        this.symbol = sym;
        this.unitColor = col;
        this.category = cat;
        this.maxMovementBudget = maxMovement;
        this.maxVisibilityBudget = maxVision;
        this.attackStrength = attack;
        this.maxHealth = health;
    }
}
