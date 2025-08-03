package game.model;

/**
 * CombatResult.java
 *
 * PURPOSE:
 * Immutable data structure containing the results of a combat action.
 * Used for UI updates, logging, and game state management.
 *
 * DESIGN PRINCIPLES:
 * - Immutability: Result cannot be changed after creation
 * - Comprehensive: Contains all necessary combat outcome information
 * - Validation: Distinguishes between valid and invalid combat attempts
 */
public class CombatResult {
    private final int attackerDamageDealt;
    private final int defenderDamageDealt;
    private final boolean defenderKilled;
    private final boolean attackerKilled;
    private final boolean isValid;
    private final String errorMessage;
    
    /**
     * Creates a valid combat result.
     */
    public CombatResult(int attackerDamage, int defenderDamage, 
                       boolean defenderKilled, boolean attackerKilled) {
        this.attackerDamageDealt = attackerDamage;
        this.defenderDamageDealt = defenderDamage;
        this.defenderKilled = defenderKilled;
        this.attackerKilled = attackerKilled;
        this.isValid = true;
        this.errorMessage = null;
    }
    
    /**
     * Creates an invalid combat result with an error message.
     */
    private CombatResult(String errorMessage) {
        this.attackerDamageDealt = 0;
        this.defenderDamageDealt = 0;
        this.defenderKilled = false;
        this.attackerKilled = false;
        this.isValid = false;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Factory method for creating invalid combat results.
     */
    public static CombatResult invalid(String reason) {
        return new CombatResult(reason);
    }
    
    // Getters
    public int getAttackerDamageDealt() { return attackerDamageDealt; }
    public int getDefenderDamageDealt() { return defenderDamageDealt; }
    public boolean isDefenderKilled() { return defenderKilled; }
    public boolean isAttackerKilled() { return attackerKilled; }
    public boolean isValid() { return isValid; }
    public boolean hasWinner() { return defenderKilled || attackerKilled; }
    public String getErrorMessage() { return errorMessage; }
    
    @Override
    public String toString() {
        if (!isValid) {
            return "CombatResult[INVALID: " + errorMessage + "]";
        }
        return "CombatResult[Attacker dealt: " + attackerDamageDealt + 
               ", Defender dealt: " + defenderDamageDealt + 
               ", DefenderKilled: " + defenderKilled + 
               ", AttackerKilled: " + attackerKilled + "]";
    }
}
