package game.core;

import game.GameConfig;
import game.model.enums.UnitCategory;
import game.model.CombatResult;
import game.model.Hex;
import game.model.Unit;

/**
 * CombatManager.java
 *
 * PURPOSE:
 * Handles all combat resolution logic including damage calculation,
 * counterattacks, unit death, and tile occupation.
 *
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Manages only combat mechanics
 * - Validation: Ensures combat legality before execution
 * - Event-Driven: Provides detailed combat results for logging and UI
 */
public class CombatManager {
    
    private UnitManager unitManager; // For clearing selections when units die
    
    public CombatManager() {
        // Constructor for future dependency injection if needed
    }
    
    /**
     * Sets the unit manager for clearing selections when units die.
     */
    public void setUnitManager(UnitManager unitManager) {
        this.unitManager = unitManager;
    }
    
    /**
     * Executes a melee combat between attacker and defender.
     * Handles damage calculation, counterattack, death, and tile occupation.
     *
     * @param attacker The attacking unit
     * @param defender The defending unit
     * @param attackFromHex The hex the attacker moves to for the attack
     * @return CombatResult containing the outcome of the battle
     */
    public CombatResult executeMeleeAttack(Unit attacker, Unit defender, Hex attackFromHex) {
        // Validate combat legality
        if (!canExecuteCombat(attacker, defender)) {
            return CombatResult.invalid("Combat not allowed between these units");
        }
        
        // Move attacker to attack position (consume movement)
        moveUnitToHex(attacker, attackFromHex);
        attacker.currentMovementBudget -= GameConfig.ATTACK_MOVEMENT_COST;
        
        // Calculate damage
        int attackerDamage = calculateDamage(attacker, defender);
        int counterattackDamage = 0;
        
        // Apply attacker's damage to defender
        boolean defenderDied = defender.takeDamage(attackerDamage);
        
        // Handle counterattack if defender survives and can counterattack
        boolean attackerDied = false;
        if (!defenderDied && defender.canCounterattack()) {
            counterattackDamage = (int)(calculateDamage(defender, attacker) * GameConfig.COUNTERATTACK_DAMAGE_MULTIPLIER);
            attackerDied = attacker.takeDamage(counterattackDamage);
        }
        
        // Handle unit death and tile occupation
        if (defenderDied) {
            handleUnitDeath(defender);
            if (!attackerDied) {
                // Attacker moves to defender's tile
                moveUnitToHex(attacker, new Hex(defender.q, defender.r));
            }
        }
        
        if (attackerDied) {
            handleUnitDeath(attacker);
        }
        
        return new CombatResult(attackerDamage, counterattackDamage, defenderDied, attackerDied);
    }
    
    /**
     * Calculates damage dealt by attacker to defender.
     * Currently uses simple attack strength, but can be extended for modifiers.
     */
    private int calculateDamage(Unit attacker, Unit defender) {
        // Handle civilian instant death
        if (defender.type.category.isVulnerableToInstantDeath() && 
            attacker.type.category == UnitCategory.LAND_MELEE) {
            return defender.currentHealth; // Instant kill
        }
        
        // Standard damage calculation
        return attacker.attackStrength;
    }
    
    /**
     * Validates that combat can be executed between the two units.
     */
    private boolean canExecuteCombat(Unit attacker, Unit defender) {
        return attacker.canAttack() && 
               !attacker.isDead() && 
               !defender.isDead() &&
               !attacker.owner.equals(defender.owner) &&
               attacker.currentMovementBudget >= GameConfig.ATTACK_MOVEMENT_COST;
    }
    
    /**
     * Handles unit death by removing from civilization and clearing selections.
     */
    private void handleUnitDeath(Unit deadUnit) {
        // Remove from civilization
        deadUnit.owner.removeUnit(deadUnit);
        
        // Clear from UnitManager if selected
        if (unitManager != null) {
            unitManager.clearSelectionIfUnit(deadUnit);
        }
        
        System.out.println("Unit died: " + deadUnit.type + " at (" + deadUnit.q + "," + deadUnit.r + ")");
    }
    
    /**
     * Moves a unit to the specified hex and updates its coordinates.
     */
    private void moveUnitToHex(Unit unit, Hex targetHex) {
        unit.q = targetHex.q;
        unit.r = targetHex.r;
        
        // Update visibility for the unit's civilization
        unit.owner.getVisibilityManager().updateGlobalVisibility(unit.owner.getUnits());
    }
}
