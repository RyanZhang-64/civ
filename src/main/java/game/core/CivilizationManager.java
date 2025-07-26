package game.core;

import java.util.ArrayList;
import java.util.List;

import game.model.Civilization;
import game.model.HexGrid;
import game.model.Unit;
import game.model.UnitType;

/**
 * CivilizationManager.java
 *
 * PURPOSE:
 * Manages all civilizations in the game, including turn order, initialization,
 * and turn cycling. Acts as the central controller for civilization-level
 * game mechanics.
 *
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Manages civilization lifecycle and turn order
 * - Centralized Control: Single point of control for multi-player mechanics
 * - Turn Management: Handles the sequential turn-based gameplay
 */
public class CivilizationManager {

    private final List<Civilization> civilizations;
    private int currentCivilizationIndex;
    private final HexGrid hexGrid;
    private CityManager cityManager;
    private UnitManager unitManager;

    /**
     * Constructs a new CivilizationManager.
     *
     * @param hexGrid The hex grid for the game world.
     */
    public CivilizationManager(HexGrid hexGrid) {
        this.civilizations = new ArrayList<>();
        this.currentCivilizationIndex = 0;
        this.hexGrid = hexGrid;
    }

    /**
     * Adds a new civilization to the game.
     *
     * @param civilization The civilization to add.
     */
    public void addCivilization(Civilization civilization) {
        civilizations.add(civilization);
    }

    /**
     * Initializes the game with two test civilizations.
     */
    public void initializeTestScenario() {
        // Clear any existing civilizations
        civilizations.clear();
        
        // Create two test civilizations
        Civilization civ1 = new Civilization(0, "Player 1", 0xFF0000FF, hexGrid); // Blue
        Civilization civ2 = new Civilization(1, "Player 2", 0xFFFF0000, hexGrid); // Red
        
        // Add them to the manager
        addCivilization(civ1);
        addCivilization(civ2);
        
        // Spawn starting units for each civilization at different locations
        civ1.spawnUnit(UnitType.SCOUT, 0, 0);
        civ1.spawnUnit(UnitType.SETTLER, 1, -1);
        
        civ2.spawnUnit(UnitType.SCOUT, 10, 10);
        civ2.spawnUnit(UnitType.SETTLER, 11, 9);
        
        // Start with the first civilization
        currentCivilizationIndex = 0;
    }

    /**
     * Advances to the next civilization's turn.
     */
    public void nextTurn() {
        if (civilizations.isEmpty()) return;
        
        Civilization currentCiv = getCurrentCivilization();
        
        // Refresh current civilization's units before ending their turn
        currentCiv.refreshAllUnits();
        
        // Process city turns for the current civilization
        if (cityManager != null) {
            cityManager.processCityTurns(currentCiv);
        }
        
        // Clear unit selection when switching turns
        if (unitManager != null) {
            unitManager.deselectUnit();
        }
        
        // Move to next civilization
        currentCivilizationIndex = (currentCivilizationIndex + 1) % civilizations.size();
        
        // The new current civilization's units are automatically ready
        // (they were refreshed when their turn ended previously)
    }
    
    /**
     * Sets the unit manager for turn integration.
     * This allows the civilization manager to clear unit selections when switching turns.
     */
    public void setUnitManager(UnitManager unitManager) {
        this.unitManager = unitManager;
    }
    
    /**
     * Sets the city manager for turn processing integration.
     * This is called after the CityManager is created.
     */
    public void setCityManager(CityManager cityManager) {
        this.cityManager = cityManager;
    }
    
    /**
     * Gets the first unit of the current civilization (useful for camera centering).
     *
     * @return The first unit of the current civilization, or null if none exist.
     */
    public Unit getFirstUnitOfCurrentCivilization() {
        Civilization currentCiv = getCurrentCivilization();
        if (currentCiv == null || currentCiv.getUnits().isEmpty()) return null;
        return currentCiv.getUnits().get(0);
    }

    /**
     * Gets the currently active civilization.
     *
     * @return The current civilization, or null if no civilizations exist.
     */
    public Civilization getCurrentCivilization() {
        if (civilizations.isEmpty()) return null;
        return civilizations.get(currentCivilizationIndex);
    }

    /**
     * Checks if a civilization exists and is valid.
     *
     * @param civilization The civilization to check.
     * @return true if the civilization is managed by this manager.
     */
    public boolean isValidCivilization(Civilization civilization) {
        return civilizations.contains(civilization);
    }

    /**
     * Gets all civilizations in the game.
     *
     * @return A copy of the civilizations list.
     */
    public List<Civilization> getAllCivilizations() {
        return new ArrayList<>(civilizations);
    }

    /**
     * Gets the number of civilizations in the game.
     *
     * @return The number of civilizations.
     */
    public int getCivilizationCount() {
        return civilizations.size();
    }

    /**
     * Gets the index of the current civilization.
     *
     * @return The current civilization index.
     */
    public int getCurrentCivilizationIndex() {
        return currentCivilizationIndex;
    }
}
