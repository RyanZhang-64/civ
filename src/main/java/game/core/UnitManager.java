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
import game.model.Civilization;
import game.model.Hex;
import game.model.HexGrid;
import game.model.Unit;

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

    private Unit selectedUnit;
    private Map<Hex, Integer> reachableHexes; // Stores hex and the cost to reach it
    private int nextUnitIndex = 0; // Tracks the cycle for "Next Unit" button

    public UnitManager(HexGrid grid, Camera camera, CivilizationManager civManager) {
        this.hexGrid = grid;
        this.camera = camera;
        this.civilizationManager = civManager;
        this.selectedUnit = null;
        this.reachableHexes = new HashMap<>();
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
            selectedUnit.currentMovementBudget -= cost;
            selectedUnit.setPosition(targetHex.q, targetHex.r);
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

    private static class PathNode {
        final Hex hex;
        final int cost;
        PathNode(Hex hex, int cost) { this.hex = hex; this.cost = cost; }
    }
}
