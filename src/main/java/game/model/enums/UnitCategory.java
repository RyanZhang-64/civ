package game.model.enums;

/**
 * UnitCategory.java
 *
 * PURPOSE:
 * Defines the five main categories of units with their combat capabilities.
 * Determines how units interact in combat and what actions they can perform.
 *
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Each category defines specific combat behaviors
 * - Encapsulation: Combat capabilities are encapsulated within each category
 * - Extensibility: Easy to add new categories or modify existing ones
 */
public enum UnitCategory {
    CIVILIAN(false, false, false),        // Cannot attack, vulnerable to instant death
    LAND_MELEE(true, false, true),        // Can attack adjacent, can counterattack
    LAND_RANGED(true, true, true),        // Can attack at range, can counterattack
    NAVAL_MELEE(true, false, true),       // Can attack adjacent (water), can counterattack
    NAVAL_RANGED(true, true, true);       // Can attack at range (water), can counterattack
    
    private final boolean canAttack;
    private final boolean canRangedAttack;
    private final boolean canCounterattack;
    
    UnitCategory(boolean canAttack, boolean canRangedAttack, boolean canCounterattack) {
        this.canAttack = canAttack;
        this.canRangedAttack = canRangedAttack;
        this.canCounterattack = canCounterattack;
    }
    
    public boolean canAttack() { return canAttack; }
    public boolean canRangedAttack() { return canRangedAttack; }
    public boolean canCounterattack() { return canCounterattack; }
    
    /**
     * Determines if this unit type is instantly killed by melee attacks.
     */
    public boolean isVulnerableToInstantDeath() {
        return this == CIVILIAN;
    }
}
