package game;

/**
 * GameConfig.java
 *
 * PURPOSE:
 * Provides a centralized location for all static configuration values and "magic numbers"
 * used throughout the game. This makes the game easier to balance, configure, and maintain.
 *
 * DESIGN PRINCIPLES:
 * - Single Source of Truth: All constants are defined here once, preventing inconsistencies.
 * - Readability: Using named constants (e.g., `GRID_RADIUS`) is more descriptive
 * than using hardcoded numbers.
 * - Utility Class: This class is final and has a private constructor, as it is not
 * meant to be instantiated.
 */
public final class GameConfig {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private GameConfig() {}

    //-------------------------------------------------------------------------
    // --- Grid & Map Settings ---
    //-------------------------------------------------------------------------
    public static final int GRID_RADIUS = 20;
    public static final float HEX_RADIUS = 20.0f;
    public static final float NOISE_SCALE = 0.15f;

    //-------------------------------------------------------------------------
    // --- Camera Settings ---
    //-------------------------------------------------------------------------
    public static final float MIN_ZOOM = 0.2f;
    public static final float MAX_ZOOM = 5.0f;
    public static final float ZOOM_SENSITIVITY = 1.1f;

    //-------------------------------------------------------------------------
    // --- UI Settings ---
    //-------------------------------------------------------------------------
    public static final float BUTTON_WIDTH = 120.0f;
    public static final float BUTTON_HEIGHT = 40.0f;
    public static final float BUTTON_MARGIN = 10.0f;

    //-------------------------------------------------------------------------
    // --- Rendering & Color Settings ---
    //-------------------------------------------------------------------------
    // Note: Colors are stored as integers in ARGB format (e.g., 0xAARRGGBB).
    public static final int FOG_OF_WAR_COLOR = 0xFF22223B; // Dark blue-grey
    public static final int MOVEMENT_RANGE_FILL_COLOR = 0x5032CD32; // Semi-transparent lime green
    public static final int MOVEMENT_BOUNDARY_COLOR = 0xFFFFFF00; // Bright yellow
    public static final int SELECTION_INDICATOR_COLOR = 0x64FF0000; // Semi-transparent red
    public static final float DISCOVERED_BRIGHTNESS_MULTIPLIER = 0.5f; // How dim to make previously seen hexes

    //-------------------------------------------------------------------------
    // --- Unit Base Stats ---
    //-------------------------------------------------------------------------
    // Scout
    public static final int SCOUT_MOVEMENT_BUDGET = 12;
    public static final int SCOUT_VISIBILITY_BUDGET = 8;

    // Settler
    public static final int SETTLER_MOVEMENT_BUDGET = 8;
    public static final int SETTLER_VISIBILITY_BUDGET = 6;
}
