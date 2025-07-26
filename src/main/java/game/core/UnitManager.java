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
import game.model.Hex;
import game.model.HexGrid;
import game.model.Unit;
import game.model.UnitType;

/**
 * UnitManager.java
 *
 * PURPOSE:
 * Manages all unit-related logic, including creation, selection, movement,
 * and pathfinding. It acts as the central controller for all unit actions.
 */
public class UnitManager {

    private final HexGrid hexGrid;
    private final VisibilityManager visibilityManager;
    private final List<Unit> units;
    private final Camera camera;

    private Unit selectedUnit;
    private Map<Hex, Integer> reachableHexes; // Stores hex and the cost to reach it
    private int nextUnitIndex = 0; // Tracks the cycle for "Next Unit" button

    public UnitManager(HexGrid grid, VisibilityManager vm, Camera camera) {
        this.hexGrid = grid;
        this.visibilityManager = vm;
        this.camera = camera;
        this.units = new ArrayList<>();
        this.selectedUnit = null;
        this.reachableHexes = new HashMap<>();
    }

    /**
     * Creates a new unit, adds it to the game, and updates visibility.
     * @param type The type of unit to create.
     * @param q The initial q-coordinate.
     * @param r The initial r-coordinate.
     */
    public void spawnUnit(UnitType type, int q, int r) {
        Unit unit = new Unit(type, q, r);
        units.add(unit);
        visibilityManager.updateGlobalVisibility(units);
    }

    /**
     * Attempts to select a unit at the given hex. If a unit is found, it becomes
     * the selected unit and its movement range is calculated.
     * @param hex The hex where the selection attempt occurs.
     */
    public void selectUnitAt(Hex hex) {
        deselectUnit();
        for (Unit unit : units) {
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
            visibilityManager.updateGlobalVisibility(units);
        }
        deselectUnit();
    }

    /**
     * Refreshes the movement budget for all units in the game.
     */
    public void refreshAllUnits() {
        for (Unit unit : units) {
            unit.refreshMovement();
        }
        // If a unit was selected, recalculate its now larger movement range
        if (selectedUnit != null) {
            calculateReachableHexes();
        }
    }

    /**
     * Finds and selects the next unit that has movement points remaining.
     */
    public void selectNextUnitWithMovement() {
        List<Unit> availableUnits = new ArrayList<>();
        for (Unit unit : units) {
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
    public List<Unit> getUnits() { return units; }
    public Unit getSelectedUnit() { return selectedUnit; }
    public Map<Hex, Integer> getReachableHexes() { return Collections.unmodifiableMap(reachableHexes); }

    private static class PathNode {
        final Hex hex;
        final int cost;
        PathNode(Hex hex, int cost) { this.hex = hex; this.cost = cost; }
    }
}