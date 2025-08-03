package game.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import processing.core.PApplet;  // Add this import
import game.Camera;
import game.GameConfig;
import game.events.UnitMovedEvent;
import game.model.Civilization;
import game.model.Hex;
import game.model.Unit;
import game.model.HexGrid;
import game.model.AttackTarget;
import game.model.City;
import game.model.CombatResult;

/**
 * UnitManager.java
 *
 * PURPOSE:
 * Manages unit-related logic for the currently active civilization, including
 * selection, movement, and pathfinding. Works with CivilizationManager to
 * ensure only the current player's units can be controlled.
 */
public class UnitManager {

    private final HexGrid hexGrid;
    private final Camera camera;
    private final CivilizationManager civilizationManager;
    private GameObjectPoolManager pools;
    private CityManager cityManager; // For illegal territory validation
    private CombatManager combatManager; // For combat resolution

    private Unit selectedUnit;
    private Map<Hex, Integer> reachableHexes; // Stores hex and the cost to reach it
    private List<AttackTarget> attackableTargets; // Cache of current attack targets
    private int nextUnitIndex = 0; // Tracks the cycle for "Next Unit" button

    public UnitManager(HexGrid grid, Camera camera, CivilizationManager civManager) {
        this.hexGrid = grid;
        this.camera = camera;
        this.civilizationManager = civManager;
        this.selectedUnit = null;
        this.reachableHexes = new HashMap<>();
        this.attackableTargets = new ArrayList<>();
    }

    /**
     * Attempts to select a unit at the given hex. Only allows selection of
     * units belonging to the current civilization.
     * @param hex The hex where the selection attempt occurs.
     */
    public void selectUnitAt(Hex hex) {
        deselectUnit();
        Civilization currentCiv = civilizationManager.getCurrentCivilization();
        if (currentCiv == null) return;
        
        for (Unit unit : currentCiv.getUnits()) {
            if (unit.q == hex.q && unit.r == hex.r) {
                this.selectedUnit = unit;
                calculateReachableHexes();
                calculateAttackableTargets(); // Calculate attack targets after movement range
                return;
            }
        }
    }

    /**
     * Clears the current selection and movement range.
     */
    public void deselectUnit() {
        this.selectedUnit = null;
        this.reachableHexes.clear();
        this.attackableTargets.clear();
    }

    /**
     * Clears selection if the specified unit is currently selected.
     * Used when units die or are removed from the game.
     * 
     * @param unit The unit to check and potentially clear from selection
     */
    public void clearSelectionIfUnit(Unit unit) {
        if (selectedUnit == unit) {
            deselectUnit();
        }
    }

    /**
     * Attempts to move the currently selected unit to a target hex.
     * @param targetHex The destination hex.
     */
    public void moveSelectedUnit(Hex targetHex) {
        if (selectedUnit == null || !reachableHexes.containsKey(targetHex)) {
            return;
        }
        int cost = reachableHexes.get(targetHex);
        if (selectedUnit.currentMovementBudget >= cost) {
            int fromQ = selectedUnit.q;
            int fromR = selectedUnit.r;
            
            selectedUnit.currentMovementBudget -= cost;
            selectedUnit.setPosition(targetHex.q, targetHex.r);
            
            // Fire unit moved event using pooled object if available
            if (pools != null) {
                UnitMovedEvent event = pools.getUnitMovedEvent(selectedUnit, fromQ, fromR, targetHex.q, targetHex.r);
                // Event would be processed by GameEventManager here
                pools.returnUnitMovedEvent(event);
            }
            
            // Update visibility for the unit's civilization
            selectedUnit.owner.getVisibilityManager().updateGlobalVisibility(selectedUnit.owner.getUnits());
        }
        deselectUnit();
    }

    /**
     * Finds and selects the next unit that has movement points remaining
     * from the current civilization.
     */
    public void selectNextUnitWithMovement() {
        Civilization currentCiv = civilizationManager.getCurrentCivilization();
        if (currentCiv == null) {
            deselectUnit();
            return;
        }
        
        List<Unit> availableUnits = new ArrayList<>();
        for (Unit unit : currentCiv.getUnits()) {
            if (unit.currentMovementBudget > 0) {
                availableUnits.add(unit);
            }
        }

        if (availableUnits.isEmpty()) {
            deselectUnit();
            return;
        }

        nextUnitIndex = nextUnitIndex % availableUnits.size();
        Unit unitToSelect = availableUnits.get(nextUnitIndex);
        nextUnitIndex++;

        // Use the existing selection method to select the unit and calculate its range
        selectUnitAt(hexGrid.getHexAt(unitToSelect.q, unitToSelect.r));
        // Center the camera on the newly selected unit
        centerCameraOnSelectedUnit();
    }

    /**
     * Centers the camera on the currently selected unit.
     */
    private void centerCameraOnSelectedUnit() {
        if (selectedUnit == null) return;
        // Convert hex coordinates to pixel coordinates using the same formula as HexBoundaryCalculator
        float x = GameConfig.HEX_RADIUS * (PApplet.sqrt(3) * selectedUnit.q + PApplet.sqrt(3) / 2.0f * selectedUnit.r);
        float y = GameConfig.HEX_RADIUS * (3.0f / 2.0f * selectedUnit.r);
        camera.centerOn(x, y);
    }

    /**
     * Calculates the movement range for the currently selected unit using Dijkstra's algorithm.
     */
    private void calculateReachableHexes() {
        reachableHexes.clear();
        if (selectedUnit == null) return;
        
        Hex startHex = hexGrid.getHexAt(selectedUnit.q, selectedUnit.r);
        PriorityQueue<PathNode> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
        queue.add(new PathNode(startHex, 0));
        reachableHexes.put(startHex, 0);

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            for (Hex neighbor : hexGrid.getNeighbors(current.hex)) {
                int newCost = current.cost + neighbor.biome.movementCost;
                if (newCost <= selectedUnit.currentMovementBudget) {
                    if (!reachableHexes.containsKey(neighbor) || newCost < reachableHexes.get(neighbor)) {
                        reachableHexes.put(neighbor, newCost);
                        queue.add(new PathNode(neighbor, newCost));
                    }
                }
            }
        }
    }

    // --- Getters ---
    public List<Unit> getUnits() { 
        Civilization currentCiv = civilizationManager.getCurrentCivilization();
        return currentCiv != null ? currentCiv.getUnits() : new ArrayList<>(); 
    }
    public Unit getSelectedUnit() { return selectedUnit; }
    public Map<Hex, Integer> getReachableHexes() { return Collections.unmodifiableMap(reachableHexes); }
    
    /**
     * Gets the list of currently attackable targets.
     * This list is updated when calculateAttackableTargets() is called during unit selection.
     * 
     * @return Immutable list of attackable targets
     */
    public List<AttackTarget> getAttackableTargets() {
        return Collections.unmodifiableList(attackableTargets);
    }
    
    /**
     * Sets the pool manager for optimized object allocation.
     */
    public void setPools(GameObjectPoolManager pools) {
        this.pools = pools;
    }
    
    /**
     * Sets the city manager for illegal territory validation.
     * This is called during system initialization to establish dependencies.
     * 
     * @param cityManager The city manager instance
     */
    public void setCityManager(CityManager cityManager) {
        this.cityManager = cityManager;
    }
    
    /**
     * Sets the combat manager for combat resolution.
     * This is called during system initialization to establish dependencies.
     * 
     * @param combatManager The combat manager instance
     */
    public void setCombatManager(CombatManager combatManager) {
        this.combatManager = combatManager;
    }
    
    /**
     * Calculates all enemy units that can be attacked by the currently selected unit.
     * For each attackable enemy, determines the optimal adjacent hex to attack from
     * that minimizes movement cost while avoiding illegal territory.
     * 
     * Attack requirements:
     * - Unit must be able to reach an adjacent hex to the enemy
     * - Unit must have at least 1 movement point remaining after reaching attack position
     * - Attack position must not be in illegal territory (enemy cities or occupied hexes)
     * 
     * @return List of AttackTarget objects representing attackable enemies
     */
    public List<AttackTarget> calculateAttackableTargets() {
        attackableTargets.clear();
        
        if (selectedUnit == null) {
            return attackableTargets;
        }
        
        // Only units that can still attack this turn can have attack targets
        if (!selectedUnit.canAttackThisTurn()) {
            return attackableTargets;
        }
        
        Civilization currentCiv = civilizationManager.getCurrentCivilization();
        if (currentCiv == null) {
            return attackableTargets;
        }
        
        // Get all foreign units (enemies)
        List<Unit> enemyUnits = getAllForeignUnits(currentCiv);
        
        // For each enemy unit, check if it's attackable
        for (Unit enemy : enemyUnits) {
            AttackTarget attackTarget = findOptimalAttackPosition(enemy);
            if (attackTarget != null) {
                attackableTargets.add(attackTarget);
            }
        }
        
        return attackableTargets;
    }
    
    /**
     * Finds the optimal attack position for a target enemy unit.
     * Checks all hexes adjacent to the enemy and selects the one with minimum
     * movement cost that is reachable and not in illegal territory.
     * 
     * @param enemyUnit The enemy unit to find attack position for
     * @return AttackTarget if enemy is attackable, null otherwise
     */
    private AttackTarget findOptimalAttackPosition(Unit enemyUnit) {
        Hex enemyHex = hexGrid.getHexAt(enemyUnit.q, enemyUnit.r);
        if (enemyHex == null) return null;
        
        List<Hex> adjacentHexes = hexGrid.getNeighbors(enemyHex);
        
        Hex bestAttackHex = null;
        int minMovementCost = Integer.MAX_VALUE;
        
        // Check each adjacent hex for the optimal attack position
        for (Hex adjacentHex : adjacentHexes) {
            // Check if hex is reachable with budget for attack (need ≥1 movement after reaching)
            Integer movementCost = reachableHexes.get(adjacentHex);
            if (movementCost == null || movementCost >= selectedUnit.currentMovementBudget) {
                continue; // Not reachable or no budget left for attack
            }
            
            // Check if hex is in illegal territory
            if (isIllegalTerritory(adjacentHex)) {
                continue; // Cannot attack from this position
            }
            
            // This is a valid attack position - check if it's the best so far
            if (movementCost < minMovementCost) {
                minMovementCost = movementCost;
                bestAttackHex = adjacentHex;
            }
        }
        
        if (bestAttackHex != null) {
            return new AttackTarget(enemyUnit, bestAttackHex, minMovementCost);
        }
        
        return null; // Enemy not attackable
    }
    
    /**
     * Executes an attack command against a target unit.
     * Called from InputHandler when player clicks on attackable enemy.
     * Moves the attacker to optimal position and resolves combat.
     * 
     * @param target The attack target containing enemy unit and attack position
     * @return CombatResult containing the outcome of the battle
     */
    public CombatResult executeAttack(AttackTarget target) {
        if (selectedUnit == null || combatManager == null) {
            return CombatResult.invalid("No unit selected or combat manager not available");
        }
        
        Unit attacker = selectedUnit;
        Unit defender = target.getTargetUnit();
        Hex attackFromHex = target.getOptimalAttackHex();
        
        // Validate attacker can still attack
        if (!attacker.canAttackThisTurn()) {
            return CombatResult.invalid("Selected unit cannot attack (already attacked this turn)");
        }
        
        // Execute combat
        CombatResult result = combatManager.executeMeleeAttack(attacker, defender, attackFromHex);
        
        // Mark attacker as having attacked this turn (regardless of outcome)
        if (result.isValid()) {
            attacker.markAsAttacked();
            
            // Immediately clear attack targets since unit can no longer attack
            attackableTargets.clear();
            
            // Recalculate reachable hexes for remaining movement (but no more attacks)
            calculateReachableHexes();
            // Don't recalculate attack targets since unit already attacked
        }
        
        return result;
    }
    
    /**
     * Determines if a hex is in illegal territory for unit movement/attack.
     * Illegal territory includes:
     * - Enemy city tiles
     * - Hexes occupied by enemy units
     * 
     * @param hex The hex to check
     * @return true if hex is illegal territory, false otherwise
     */
    private boolean isIllegalTerritory(Hex hex) {
        Civilization currentCiv = civilizationManager.getCurrentCivilization();
        if (currentCiv == null) return true;
        
        // Check if hex contains an enemy city (only if cityManager is available)
        if (cityManager != null) {
            City cityAtHex = cityManager.getCityAt(hex.q, hex.r);
            if (cityAtHex != null && !cityAtHex.getOwner().equals(currentCiv)) {
                return true; // Enemy city
            }
        }
        
        // Check if hex is occupied by an enemy unit
        for (Civilization civ : civilizationManager.getAllCivilizations()) {
            if (civ.equals(currentCiv)) continue; // Skip own civilization
            
            for (Unit unit : civ.getUnits()) {
                if (unit.q == hex.q && unit.r == hex.r) {
                    return true; // Enemy unit occupies this hex
                }
            }
        }
        
        return false; // Hex is legal territory
    }
    
    /**
     * Gets all units belonging to foreign civilizations (enemies).
     * 
     * @param currentCiv The current player's civilization
     * @return List of all foreign units
     */
    private List<Unit> getAllForeignUnits(Civilization currentCiv) {
        List<Unit> foreignUnits = new ArrayList<>();
        
        for (Civilization civ : civilizationManager.getAllCivilizations()) {
            if (!civ.equals(currentCiv)) {
                foreignUnits.addAll(civ.getUnits());
            }
        }
        
        return foreignUnits;
    }

    private static class PathNode {
        final Hex hex;
        final int cost;
        PathNode(Hex hex, int cost) { this.hex = hex; this.cost = cost; }
    }
}
