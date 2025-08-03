package game.model;

/**
 * AttackTarget.java
 *
 * PURPOSE:
 * Represents a potential attack target with optimal positioning information.
 * Contains the target unit and the most efficient hex to attack from.
 *
 * DESIGN PRINCIPLES:
 * - Immutable Value Object: Once created, cannot be modified
 * - Single Responsibility: Stores attack target data only
 * - Data Transfer Object: Carries attack information between systems
 */
public class AttackTarget {
    
    private final Unit targetUnit;
    private final Hex optimalAttackHex;
    private final int movementCostToReach;
    
    /**
     * Creates a new attack target.
     * 
     * @param targetUnit The enemy unit that can be attacked
     * @param optimalAttackHex The hex to move to for optimal attack positioning
     * @param movementCostToReach The movement cost to reach the optimal attack position
     */
    public AttackTarget(Unit targetUnit, Hex optimalAttackHex, int movementCostToReach) {
        this.targetUnit = targetUnit;
        this.optimalAttackHex = optimalAttackHex;
        this.movementCostToReach = movementCostToReach;
    }
    
    /**
     * Gets the enemy unit that can be attacked.
     * 
     * @return The target unit
     */
    public Unit getTargetUnit() {
        return targetUnit;
    }
    
    /**
     * Gets the optimal hex to attack from.
     * This hex minimizes movement cost while ensuring the attacker
     * has enough movement budget remaining to perform the attack.
     * 
     * @return The hex to move to for attacking
     */
    public Hex getOptimalAttackHex() {
        return optimalAttackHex;
    }
    
    /**
     * Gets the movement cost to reach the optimal attack position.
     * This cost includes the movement to reach the attack hex but
     * does not include the attack action itself (which costs 1 movement).
     * 
     * @return The movement cost to reach attack position
     */
    public int getMovementCostToReach() {
        return movementCostToReach;
    }
    
    /**
     * Gets the total movement cost including the attack action.
     * This is the movement cost to reach the attack position plus 1
     * for the attack action itself.
     * 
     * @return The total movement cost for the complete attack action
     */
    public int getTotalMovementCost() {
        return movementCostToReach + 1; // +1 for the attack action
    }
    
    @Override
    public String toString() {
        return String.format("AttackTarget{target: %s at (%d,%d), attackFrom: (%d,%d), cost: %d}", 
                           targetUnit.type, targetUnit.q, targetUnit.r,
                           optimalAttackHex.q, optimalAttackHex.r, 
                           movementCostToReach);
    }
}
