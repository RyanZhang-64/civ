package game.rendering;

import game.model.enums.BoundaryType;

/**
 * Configuration class for boundary rendering appearance.
 * Centralizes visual settings for different boundary types.
 * 
 * PURPOSE:
 * Provides a single source of truth for boundary rendering configuration,
 * enabling consistent visual appearance across the application and easy
 * adjustment of boundary styles.
 * 
 * DESIGN PRINCIPLES:
 * - Constants: All configuration values are static final for performance
 * - Type Safety: Uses enums for boundary type differentiation
 * - Extensibility: Easy to add new boundary configurations
 * - Centralization: All boundary rendering settings in one place
 */
public class BoundaryRenderConfig {
    
    // Default offset distances (as percentage of hex size)
    /** Inner boundary offset as fraction of hex radius (negative = inward) */
    public static final float DEFAULT_INNER_OFFSET = -0.2f;  // 20% inward
    
    /** Outer boundary offset as fraction of hex radius (positive = outward) */
    public static final float DEFAULT_OUTER_OFFSET = 0.2f;   // 20% outward
    
    // Colors for different boundary types (ARGB format)
    /** Color for inner boundary lines */
    public static final int INNER_BOUNDARY_COLOR = 0xFF00FF00;  // Green
    
    /** Color for outer boundary lines */
    public static final int OUTER_BOUNDARY_COLOR = 0xFF000000;  // Black
    
    /** Color for standard boundary lines */
    public static final int STANDARD_BOUNDARY_COLOR = 0xFFFFFFFF; // White
    
    // Line weights for different boundary types
    /** Line weight for inner boundary - will be calculated dynamically */
    public static final float INNER_BOUNDARY_WEIGHT = 1.0f; // Base weight, actual calculated dynamically
    
    /** Line weight for outer boundary - will be calculated dynamically */
    public static final float OUTER_BOUNDARY_WEIGHT = 1.0f; // Base weight, actual calculated dynamically
    
    /** Line weight for standard boundary */
    public static final float STANDARD_BOUNDARY_WEIGHT = 2.0f;
    
    // Color blending configuration
    /** Blending ratio for civilization color mixing (0.0 = boundary color, 1.0 = civ color) */
    public static final float CIVILIZATION_COLOR_BLEND = 0.5f;
    
    /** Alpha channel for boundary transparency (0 = transparent, 255 = opaque) */
    public static final int BOUNDARY_ALPHA = 200;
    
    /**
     * Gets the color for a specific boundary type.
     *
     * @param type The boundary type
     * @return ARGB color value for the boundary type
     * @throws IllegalArgumentException if boundary type is unknown
     */
    public static int getColorForBoundaryType(BoundaryType type) {
        switch (type) {
            case INNER: return INNER_BOUNDARY_COLOR;
            case OUTER: return OUTER_BOUNDARY_COLOR;
            case STANDARD: return STANDARD_BOUNDARY_COLOR;
            default: throw new IllegalArgumentException("Unknown boundary type: " + type);
        }
    }
    
    /**
     * Gets the line weight for a specific boundary type.
     *
     * @param type The boundary type
     * @return Line weight for the boundary type
     * @throws IllegalArgumentException if boundary type is unknown
     */
    public static float getWeightForBoundaryType(BoundaryType type) {
        switch (type) {
            case INNER: return INNER_BOUNDARY_WEIGHT;
            case OUTER: return OUTER_BOUNDARY_WEIGHT;
            case STANDARD: return STANDARD_BOUNDARY_WEIGHT;
            default: throw new IllegalArgumentException("Unknown boundary type: " + type);
        }
    }
    
    /**
     * Gets the default offset distance for a boundary type.
     *
     * @param type The boundary type
     * @return Offset distance as fraction of hex radius
     * @throws IllegalArgumentException if boundary type is unknown
     */
    public static float getDefaultOffsetForBoundaryType(BoundaryType type) {
        switch (type) {
            case INNER: return DEFAULT_INNER_OFFSET;
            case OUTER: return DEFAULT_OUTER_OFFSET;
            case STANDARD: return 0.0f; // No offset for standard
            default: throw new IllegalArgumentException("Unknown boundary type: " + type);
        }
    }
    
    /**
     * Blends two ARGB colors using the configured blend ratio.
     *
     * @param civilizationColor The civilization's primary color
     * @param boundaryColor The boundary type's default color
     * @return Blended ARGB color
     */
    public static int blendColors(int civilizationColor, int boundaryColor) {
        // Extract color components
        int civR = (civilizationColor >> 16) & 0xFF;
        int civG = (civilizationColor >> 8) & 0xFF;
        int civB = civilizationColor & 0xFF;
        
        int boundaryR = (boundaryColor >> 16) & 0xFF;
        int boundaryG = (boundaryColor >> 8) & 0xFF;
        int boundaryB = boundaryColor & 0xFF;
        
        // Blend components
        int blendedR = (int) (civR * CIVILIZATION_COLOR_BLEND + boundaryR * (1.0f - CIVILIZATION_COLOR_BLEND));
        int blendedG = (int) (civG * CIVILIZATION_COLOR_BLEND + boundaryG * (1.0f - CIVILIZATION_COLOR_BLEND));
        int blendedB = (int) (civB * CIVILIZATION_COLOR_BLEND + boundaryB * (1.0f - CIVILIZATION_COLOR_BLEND));
        
        // Ensure components stay in valid range
        blendedR = Math.max(0, Math.min(255, blendedR));
        blendedG = Math.max(0, Math.min(255, blendedG));
        blendedB = Math.max(0, Math.min(255, blendedB));
        
        // Combine with configured alpha
        return (BOUNDARY_ALPHA << 24) | (blendedR << 16) | (blendedG << 8) | blendedB;
    }
    
    /**
     * Creates a color with specified alpha transparency.
     *
     * @param color The base ARGB color
     * @param alpha The alpha value (0-255)
     * @return New color with specified alpha
     */
    public static int withAlpha(int color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
    
    /**
     * Calculates the line thickness needed to span from offset boundary to hex edge.
     * 
     * @param offsetDistance The offset distance as fraction of hex radius
     * @param hexRadius The hex radius in pixels
     * @return Line thickness in pixels to span from offset to hex edge
     */
    public static float calculateLineThickness(float offsetDistance, float hexRadius) {
        // The thickness should span from the offset position to the hex edge (offset = 0)
        // So thickness = |offsetDistance| * hexRadius * 2 (to cover the full span)
        return Math.abs(offsetDistance) * hexRadius * 2.0f;
    }
    
    /**
     * Calculates the midpoint offset between the boundary offset and hex edge.
     * The line will be drawn at this midpoint with thickness spanning to both edges.
     * 
     * @param offsetDistance The original offset distance as fraction of hex radius
     * @return The midpoint offset as fraction of hex radius
     */
    public static float calculateMidpointOffset(float offsetDistance) {
        // Midpoint between offset and hex edge (0) is simply half the offset
        return offsetDistance / 2.0f;
    }
}
