package game;

import game.core.CivilizationManager;
import game.core.CityManager;
import game.core.InputHandler;
import game.core.UnitManager;
import game.model.HexGrid;
import game.rendering.GameRenderer;
import game.rendering.HexBoundaryCalculator;
import game.rendering.UIManager;
import processing.core.PApplet;

/**
 * GameObjectFactory.java
 *
 * PURPOSE:
 * Factory class responsible for creating and wiring up all game objects
 * with their dependencies. This centralizes the complex dependency
 * management and makes the GameEngine constructor cleaner.
 *
 * DESIGN PRINCIPLES:
 * - Factory Pattern: Encapsulates object creation logic
 * - Dependency Management: Handles complex dependency wiring
 * - Single Responsibility: Only responsible for object creation
 */
public class GameObjectFactory {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private GameObjectFactory() {}

    /**
     * Creates a fully configured GameEngine with all dependencies properly wired.
     *
     * @param p The PApplet instance for rendering
     * @return A fully configured GameEngine instance
     */
    public static GameEngine createGameEngine(PApplet p) {
        // Create core infrastructure components
        Camera camera = new Camera(p);
        HexGrid hexGrid = new HexGrid();
        
        // Create manager components with proper dependency order
        CivilizationManager civilizationManager = new CivilizationManager(hexGrid);
        CityManager cityManager = new CityManager(hexGrid, civilizationManager);
        UnitManager unitManager = new UnitManager(hexGrid, camera, civilizationManager);
        
        // Create UI and rendering components
        UIManager uiManager = new UIManager(p, unitManager, civilizationManager, cityManager, camera);
        HexBoundaryCalculator hexBoundaryCalculator = new HexBoundaryCalculator(p);
        GameRenderer gameRenderer = new GameRenderer(p, camera, hexGrid, unitManager, civilizationManager, cityManager, hexBoundaryCalculator);
        InputHandler inputHandler = new InputHandler(p, camera, hexGrid, unitManager, uiManager);
        
        // Create and return the GameEngine with all dependencies
        return new GameEngine(p, camera, hexGrid, civilizationManager, cityManager, unitManager, uiManager, gameRenderer, inputHandler, hexBoundaryCalculator);
    }
}
