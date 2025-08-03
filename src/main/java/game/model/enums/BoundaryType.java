package game.model.enums;

/**
 * Defines the types of boundary paths that can be calculated.
 * Used to distinguish between different offset directions and visual styles.
 * 
 * PURPOSE:
 * Provides type-safe enumeration for boundary calculation modes, enabling
 * the dual boundary system to distinguish between inner, outer, and standard
 * boundary calculations.
 * 
 * DESIGN PRINCIPLES:
 * - Type Safety: Prevents invalid boundary type specifications
 * - Extensibility: Easy to add new boundary types (e.g., for combat ranges)
 * - Self-Documenting: Each type includes descriptive metadata
 */
public enum BoundaryType {
    /** Standard boundary - original hex edge path */
    STANDARD("Standard", "Original hex edge boundary"),
    
    /** Inner boundary - offset toward the bounded region center */
    INNER("Inner", "Boundary offset inward from hex edges"),
    
    /** Outer boundary - offset away from the bounded region center */
    OUTER("Outer", "Boundary offset outward from hex edges");

    private final String displayName;
    private final String description;

    /**
     * Constructs a BoundaryType with display information.
     *
     * @param displayName Human-readable name for UI display
     * @param description Detailed description of the boundary type
     */
    BoundaryType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the display name for this boundary type.
     *
     * @return The human-readable name
     */
    public String getDisplayName() { 
        return displayName; 
    }

    /**
     * Gets the detailed description for this boundary type.
     *
     * @return The description explaining the boundary behavior
     */
    public String getDescription() { 
        return description; 
    }
}
