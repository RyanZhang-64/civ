package game.core;

import java.util.ArrayList;
import java.util.List;

import game.events.TurnChangedEvent;
import game.model.Civilization;
import game.model.Unit;
import game.model.UnitType;
import game.model.HexGrid;

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
    private GameObjectPoolManager pools;

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
        clearExistingGame();
        createTestCivilizations();
        spawnStartingUnits();
        setInitialTurn();
    }

    /**
     * Clears any existing game state for a fresh start.
     */
    private void clearExistingGame() {
        civilizations.clear();
        currentCivilizationIndex = 0;
    }

    /**
     * Creates the default test civilizations.
     */
    private void createTestCivilizations() {
        Civilization civ1 = createCivilization(0, "Player 1", 0xFF0000FF);
        Civilization civ2 = createCivilization(1, "Player 2", 0xFFFF0000);
        
        addCivilization(civ1);
        addCivilization(civ2);
    }

    /**
     * Helper method to create a civilization with standard parameters.
     */
    private Civilization createCivilization(int id, String name, int color) {
        return new Civilization(id, name, color, hexGrid);
    }

    /**
     * Spawns starting units for all civilizations.
     */
    private void spawnStartingUnits() {
        // Player 1 starting position
        spawnCivilizationStartingUnits(civilizations.get(0), 0, 0);
        
        // Player 2 starting position  
        spawnCivilizationStartingUnits(civilizations.get(1), 10, 10);
    }

    /**
     * Spawns the standard starting units for a civilization.
     * Includes combat units for testing the combat system.
     */
    private void spawnCivilizationStartingUnits(Civilization civ, int centerQ, int centerR) {
        civ.spawnUnit(UnitType.SCOUT, centerQ, centerR);
        civ.spawnUnit(UnitType.SETTLER, centerQ + 1, centerR - 1);
        civ.spawnUnit(UnitType.WARRIOR, centerQ - 1, centerR + 1); // Add warrior for combat testing
    }

    /**
     * Sets the initial turn state.
     */
    private void setInitialTurn() {
        currentCivilizationIndex = 0;
    }

    /**
     * Advances to the next civilization's turn.
     */
    public void nextTurn() {
        if (civilizations.isEmpty()) return;
        
        Civilization currentCiv = getCurrentCivilization();
        endCurrentCivilizationTurn(currentCiv);
        advanceToNextCivilization();
        beginNewCivilizationTurn();
    }

    /**
     * Handles all end-of-turn processing for the current civilization.
     */
    private void endCurrentCivilizationTurn(Civilization currentCiv) {
        currentCiv.refreshAllUnits();
        
        if (cityManager != null) {
            cityManager.processCityTurns(currentCiv);
        }
        
        // Clear any UI state tied to this civilization
        clearCivilizationUIState();
    }

    /**
     * Advances the turn index to the next civilization.
     */
    private void advanceToNextCivilization() {
        Civilization previousCiv = getCurrentCivilization();
        currentCivilizationIndex = (currentCivilizationIndex + 1) % civilizations.size();
        Civilization newCiv = getCurrentCivilization();
        
        // Fire turn changed event using pooled object if available
        if (pools != null) {
            TurnChangedEvent event = pools.getTurnEvent(previousCiv, newCiv, currentCivilizationIndex);
            // Event would be processed by GameEventManager here
            pools.returnTurnEvent(event);
        }
    }

    /**
     * Handles any setup needed for the new civilization's turn.
     */
    private void beginNewCivilizationTurn() {
        // Future: Add turn start notifications, AI processing, etc.
    }

    /**
     * Clears UI state when switching civilizations.
     */
    private void clearCivilizationUIState() {
        if (unitManager != null) {
            unitManager.deselectUnit();
        }
        // Future: Clear other UI state like selected cities, menus, etc.
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
     * Sets the pool manager for optimized object allocation.
     */
    public void setPools(GameObjectPoolManager pools) {
        this.pools = pools;
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
    
    /**
     * Gets object pool allocation metrics for performance monitoring.
     * @return String with allocation statistics, or empty if no pools configured.
     */
    public String getPoolMetrics() {
        return (pools != null) ? pools.getAllocationMetrics() : "No pools configured";
    }
}
