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
     * Constructs a new Unit.
     *
     * @param type The UnitType of this unit.
     * @param q    The initial q-coordinate.
     * @param r    The initial r-coordinate.
     */
    public Unit(UnitType type, int q, int r) {
        this.type = type;
        this.q = q;
        this.r = r;
        this.currentMovementBudget = type.maxMovementBudget;
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
    }

    /**
     * Provides a string representation of the Unit, useful for debugging.
     *
     * @return A string describing the unit's type and position.
     */
    @Override
    public String toString() {
        return "Unit[" + type.name() + " at " + q + "," + r + "]";
    }
}
