package game.core;

import game.GameEngine;
import processing.core.PApplet;

/**
 * GameObjectFactory.java
 *
 * PURPOSE:
 * Factory for creating game engine instances. Currently uses the simple approach
 * where GameEngine manages its own dependencies, but provides a centralized
 * creation point for potential future enhancements.
 *
 * DESIGN PRINCIPLES:
 * - Factory Pattern: Centralized object creation
 * - Single Responsibility: Focuses on GameEngine instantiation
 * - Future-Proof: Can be extended for dependency injection if needed
 */
public class GameObjectFactory {

    /**
     * Creates a fully configured GameEngine for normal gameplay.
     * 
     * @param p The Processing PApplet instance
     * @return A fully configured GameEngine ready for use
     */
    public static GameEngine createGameEngine(PApplet p) {
        return new GameEngine(p);
    }

    /**
     * Creates a GameEngine configured for testing scenarios.
     * Future: Could accept mock dependencies or test configurations.
     * 
     * @param p The Processing PApplet instance
     * @return A GameEngine configured for testing
     */
    public static GameEngine createTestGameEngine(PApplet p) {
        // For now, same as regular engine
        // Future: Could inject test doubles or simplified components
        return new GameEngine(p);
    }

    /**
     * Future method for dependency injection approach.
     * Currently not implemented but shows potential evolution path.
     */
    /*
    public static GameEngine createGameEngineWithDependencies(
            PApplet p, 
            Camera camera, 
            HexGrid hexGrid,
            // ... other dependencies
            ) {
        // Would create GameEngine with injected dependencies
        // Requires changes to GameEngine constructor
        throw new UnsupportedOperationException("Not yet implemented");
    }
    */
}
