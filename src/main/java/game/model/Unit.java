package game.model;

/**
 * Unit.java
 *
 * PURPOSE:
 * A pure data class representing a single unit on the game map.
 * It holds the unit's state, including its type, position, and current
 * movement points. It contains no complex game logic or rendering code.
 *
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Its only job is to store the state of a unit.
 * - Data Class: Acts as a simple container for data. All complex logic
 * (pathfinding, visibility) is handled by manager classes.
 */
public class Unit {

    /**
     * The type of this unit (e.g., SCOUT, SETTLER), which defines its base stats.
     * This is final and cannot be changed after creation.
     */
    public final UnitType type;

    /**
     * The civilization that owns this unit.
     * This is final and cannot be changed after creation.
     */
    public final Civilization owner;

    /**
     * The current axial q-coordinate of the unit on the hex grid.
     */
    public int q;

    /**
     * The current axial r-coordinate of the unit on the hex grid.
     */
    public int r;

    /**
     * The remaining movement points for this unit in the current turn.
     */
    public int currentMovementBudget;

    /**
     * The maximum health points for this unit (from UnitType).
     */
    public final int maxHealth;

    /**
     * The current health points of this unit.
     */
    public int currentHealth;

    /**
     * The attack strength of this unit (from UnitType).
     */
    public final int attackStrength;

    /**
     * Whether this unit has attacked this turn (can only attack once per turn).
     */
    public boolean hasAttackedThisTurn;

    /**
     * Constructs a new Unit.
     *
     * @param type The UnitType of this unit.
     * @param q    The initial q-coordinate.
     * @param r    The initial r-coordinate.
     * @param owner The civilization that owns this unit.
     */
    public Unit(UnitType type, int q, int r, Civilization owner) {
        this.type = type;
        this.q = q;
        this.r = r;
        this.owner = owner;
        this.currentMovementBudget = type.maxMovementBudget;
        this.maxHealth = type.maxHealth;
        this.currentHealth = type.maxHealth; // Start at full health
        this.attackStrength = type.attackStrength;
        this.hasAttackedThisTurn = false; // Can attack initially
    }

    /**
     * Updates the unit's position to new coordinates.
     *
     * @param q The new q-coordinate.
     * @param r The new r-coordinate.
     */
    public void setPosition(int q, int r) {
        this.q = q;
        this.r = r;
    }

    /**
     * Resets the unit's movement budget to its maximum value.
     * This is typically called at the beginning of a new turn.
     */
    public void refreshMovement() {
        this.currentMovementBudget = type.maxMovementBudget;
        this.hasAttackedThisTurn = false; // Reset attack status for new turn
    }

    /**
     * Applies damage to this unit and returns whether it died.
     * 
     * @param damage The amount of damage to apply
     * @return true if the unit died from this damage, false otherwise
     */
    public boolean takeDamage(int damage) {
        currentHealth = Math.max(0, currentHealth - damage);
        return isDead();
    }

    /**
     * Checks if this unit is dead (health <= 0).
     * 
     * @return true if the unit has no health remaining
     */
    public boolean isDead() {
        return currentHealth <= 0;
    }

    /**
     * Gets the health percentage for UI display.
     * 
     * @return A value between 0.0 and 1.0 representing health percentage
     */
    public float getHealthPercentage() {
        return (float) currentHealth / maxHealth;
    }

    /**
     * Checks if this unit can attack other units.
     * 
     * @return true if this unit type can initiate attacks
     */
    public boolean canAttack() {
        return type.category.canAttack();
    }

    /**
     * Checks if this unit can counterattack when attacked.
     * 
     * @return true if this unit type can counterattack
     */
    public boolean canCounterattack() {
        return type.category.canCounterattack();
    }

    /**
     * Marks this unit as having attacked this turn.
     * After attacking, the unit cannot attack again until the next turn.
     */
    public void markAsAttacked() {
        this.hasAttackedThisTurn = true;
    }

    /**
     * Checks if this unit can still attack this turn.
     * 
     * @return true if the unit can attack (hasn't attacked yet and can attack)
     */
    public boolean canAttackThisTurn() {
        return canAttack() && !hasAttackedThisTurn;
    }

    /**
     * Provides a string representation of the Unit, useful for debugging.
     *
     * @return A string describing the unit's type, position, and health.
     */
    @Override
    public String toString() {
        return "Unit[" + type.name() + " at " + q + "," + r + " HP:" + currentHealth + "/" + maxHealth + "]";
    }
}
