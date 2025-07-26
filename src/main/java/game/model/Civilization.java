package game.model;

import java.util.ArrayList;
import java.util.List;

import game.core.VisibilityManager;

/**
 * Civilization.java
 *
 * PURPOSE:
 * Represents a single civilization/player in the game. Each civilization has
 * its own units, visibility state, and game resources. This class encapsulates
 * all civilization-specific state and behavior.
 *
 * DESIGN PRINCIPLES:
 * - Encapsulation: Contains all civilization-specific data and logic
 * - Single Responsibility: Manages one civilization's state
 * - Independence: Each civilization maintains separate game state
 */
public class Civilization {

    private final int id;
    private final String name;
    private final int primaryColor;
    private final List<Unit> units;
    private final List<City> cities;
    private final VisibilityManager visibilityManager;
    
    // Turn state
    private boolean hasMovedThisTurn;

    /**
     * Constructs a new Civilization.
     *
     * @param id The unique identifier for this civilization.
     * @param name The display name of this civilization.
     * @param primaryColor The ARGB color code used to represent this civilization.
     * @param hexGrid The hex grid for visibility calculations.
     */
    public Civilization(int id, String name, int primaryColor, HexGrid hexGrid) {
        this.id = id;
        this.name = name;
        this.primaryColor = primaryColor;
        this.units = new ArrayList<>();
        this.cities = new ArrayList<>();
        this.visibilityManager = new VisibilityManager(hexGrid);
        this.hasMovedThisTurn = false;
    }

    /**
     * Spawns a new unit for this civilization.
     *
     * @param type The type of unit to create.
     * @param q The initial q-coordinate.
     * @param r The initial r-coordinate.
     */
    public void spawnUnit(UnitType type, int q, int r) {
        Unit unit = new Unit(type, q, r, this);
        units.add(unit);
        visibilityManager.updateGlobalVisibility(units);
    }

    /**
     * Removes a unit from this civilization.
     *
     * @param unit The unit to remove.
     */
    public void removeUnit(Unit unit) {
        units.remove(unit);
        visibilityManager.updateGlobalVisibility(units);
    }

    /**
     * Refreshes all units' movement points for a new turn.
     */
    public void refreshAllUnits() {
        for (Unit unit : units) {
            unit.refreshMovement();
        }
        this.hasMovedThisTurn = false;
    }

    /**
     * Checks if any units in this civilization have movement points remaining.
     *
     * @return true if any unit can still move, false otherwise.
     */
    public boolean hasUnitsWithMovement() {
        for (Unit unit : units) {
            if (unit.currentMovementBudget > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a city to this civilization.
     *
     * @param city The city to add.
     */
    public void addCity(City city) {
        cities.add(city);
    }

    /**
     * Removes a city from this civilization.
     *
     * @param city The city to remove.
     */
    public void removeCity(City city) {
        cities.remove(city);
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrimaryColor() { return primaryColor; }
    public List<Unit> getUnits() { return units; }
    public List<City> getCities() { return cities; }
    public VisibilityManager getVisibilityManager() { return visibilityManager; }
    public boolean hasMovedThisTurn() { return hasMovedThisTurn; }

    public void setHasMovedThisTurn(boolean moved) { 
        this.hasMovedThisTurn = moved; 
    }

    @Override
    public String toString() {
        return "Civilization[" + id + ": " + name + "]";
    }
}
