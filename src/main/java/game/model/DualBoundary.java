package game.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import game.model.enums.BoundaryType;

/**
 * Container for both inner and outer boundary paths calculated from the same hex set.
 * Provides unified access to dual boundary data with validation.
 * 
 * PURPOSE:
 * Manages the three boundary types (standard, inner, outer) as a cohesive unit,
 * ensuring they remain synchronized and providing convenient access methods
 * for rendering and geometric operations.
 * 
 * DESIGN PRINCIPLES:
 * - Immutability: Boundary data cannot be modified after construction
 * - Validation: Ensures all boundary types are properly constructed
 * - Encapsulation: Provides controlled access to boundary data
 * - Type Safety: Uses enums for boundary type selection
 */
public class DualBoundary {
    private final List<BoundarySegment> innerBoundary;
    private final List<BoundarySegment> outerBoundary;
    private final List<BoundarySegment> standardBoundary;
    
    /**
     * Constructs a DualBoundary containing all three boundary types.
     *
     * @param standard The original hex edge boundary segments
     * @param inner The inward-offset boundary segments
     * @param outer The outward-offset boundary segments
     * @throws IllegalArgumentException if any boundary list is null or empty
     */
    public DualBoundary(List<BoundarySegment> standard, 
                       List<BoundarySegment> inner, 
                       List<BoundarySegment> outer) {
        if (standard == null || inner == null || outer == null) {
            throw new IllegalArgumentException("Boundary lists cannot be null");
        }
        
        this.standardBoundary = new ArrayList<>(standard);
        this.innerBoundary = new ArrayList<>(inner);
        this.outerBoundary = new ArrayList<>(outer);
        
        validateBoundaries();
    }
    
    /**
     * Validates that all boundary lists contain valid data.
     *
     * @throws IllegalArgumentException if boundaries are invalid
     */
    private void validateBoundaries() {
        if (innerBoundary.isEmpty() || outerBoundary.isEmpty() || standardBoundary.isEmpty()) {
            throw new IllegalArgumentException("Boundary lists cannot be empty");
        }
        
        // Validate that boundary types match their containers
        validateBoundaryTypes(standardBoundary, BoundaryType.STANDARD);
        validateBoundaryTypes(innerBoundary, BoundaryType.INNER);
        validateBoundaryTypes(outerBoundary, BoundaryType.OUTER);
    }
    
    /**
     * Validates that all segments in a boundary have the expected type.
     *
     * @param boundary The boundary segments to validate
     * @param expectedType The expected boundary type
     * @throws IllegalArgumentException if any segment has wrong type
     */
    private void validateBoundaryTypes(List<BoundarySegment> boundary, BoundaryType expectedType) {
        for (BoundarySegment segment : boundary) {
            if (segment.getType() != expectedType) {
                throw new IllegalArgumentException(
                    String.format("Expected boundary type %s but found %s", 
                                expectedType, segment.getType()));
            }
        }
    }
    
    /**
     * Gets an immutable view of the standard boundary segments.
     *
     * @return Unmodifiable list of standard boundary segments
     */
    public List<BoundarySegment> getStandardBoundary() {
        return Collections.unmodifiableList(standardBoundary);
    }
    
    /**
     * Gets an immutable view of the inner boundary segments.
     *
     * @return Unmodifiable list of inner boundary segments
     */
    public List<BoundarySegment> getInnerBoundary() {
        return Collections.unmodifiableList(innerBoundary);
    }
    
    /**
     * Gets an immutable view of the outer boundary segments.
     *
     * @return Unmodifiable list of outer boundary segments
     */
    public List<BoundarySegment> getOuterBoundary() {
        return Collections.unmodifiableList(outerBoundary);
    }
    
    /**
     * Gets boundary segments by type.
     *
     * @param type The boundary type to retrieve
     * @return Unmodifiable list of segments for the specified type
     * @throws IllegalArgumentException if boundary type is unknown
     */
    public List<BoundarySegment> getBoundaryByType(BoundaryType type) {
        switch (type) {
            case STANDARD: return getStandardBoundary();
            case INNER: return getInnerBoundary();
            case OUTER: return getOuterBoundary();
            default: throw new IllegalArgumentException("Unknown boundary type: " + type);
        }
    }
    
    /**
     * Gets the number of segments in each boundary.
     *
     * @return The segment count (should be same for all boundary types)
     */
    public int getSegmentCount() {
        return standardBoundary.size();
    }
    
    /**
     * Checks if this dual boundary is empty.
     *
     * @return true if all boundaries are empty, false otherwise
     */
    public boolean isEmpty() {
        return standardBoundary.isEmpty() && innerBoundary.isEmpty() && outerBoundary.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("DualBoundary[segments=%d, types=%s]", 
                           getSegmentCount(), 
                           "Standard/Inner/Outer");
    }
}
