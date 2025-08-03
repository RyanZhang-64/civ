package game.model;

import processing.core.PVector;
import game.model.enums.BoundaryType;

/**
 * Represents a single segment of a boundary path between two points.
 * Contains geometric information needed for rendering and calculations.
 * 
 * PURPOSE:
 * Encapsulates the geometric data for individual boundary segments, providing
 * the building blocks for complex boundary paths. Each segment knows its
 * endpoints, normal vector, and type for proper rendering and offset calculations.
 * 
 * DESIGN PRINCIPLES:
 * - Immutability: Segment data cannot be modified after construction
 * - Defensive Copying: Returns copies of internal vectors to prevent external modification
 * - Self-Contained: Contains all information needed for geometric operations
 */
public class BoundarySegment {
    private final PVector startPoint;
    private final PVector endPoint;
    private final PVector normal;
    private final BoundaryType type;
    
    /**
     * Constructs a boundary segment with specified geometric properties.
     *
     * @param start The starting point of the segment
     * @param end The ending point of the segment
     * @param normal The normal vector for this segment (used for offset calculations)
     * @param type The type of boundary this segment represents
     */
    public BoundarySegment(PVector start, PVector end, PVector normal, BoundaryType type) {
        if (start == null || end == null || normal == null || type == null) {
            throw new IllegalArgumentException("BoundarySegment parameters cannot be null");
        }
        
        this.startPoint = new PVector(start.x, start.y);
        this.endPoint = new PVector(end.x, end.y);
        this.normal = new PVector(normal.x, normal.y);
        this.type = type;
    }
    
    /**
     * Gets a copy of the starting point.
     *
     * @return A new PVector containing the start point coordinates
     */
    public PVector getStartPoint() { 
        return new PVector(startPoint.x, startPoint.y); 
    }
    
    /**
     * Gets a copy of the ending point.
     *
     * @return A new PVector containing the end point coordinates
     */
    public PVector getEndPoint() { 
        return new PVector(endPoint.x, endPoint.y); 
    }
    
    /**
     * Gets a copy of the normal vector.
     *
     * @return A new PVector containing the normal vector components
     */
    public PVector getNormal() { 
        return new PVector(normal.x, normal.y); 
    }
    
    /**
     * Gets the boundary type for this segment.
     *
     * @return The BoundaryType enum value
     */
    public BoundaryType getType() { 
        return type; 
    }
    
    /**
     * Calculates the length of this boundary segment.
     *
     * @return The Euclidean distance between start and end points
     */
    public float getLength() {
        return PVector.dist(startPoint, endPoint);
    }
    
    /**
     * Gets the direction vector of this segment.
     *
     * @return A new PVector representing the direction from start to end
     */
    public PVector getDirection() {
        return PVector.sub(endPoint, startPoint);
    }
    
    /**
     * Gets the midpoint of this segment.
     *
     * @return A new PVector at the center of the segment
     */
    public PVector getMidpoint() {
        return PVector.lerp(startPoint, endPoint, 0.5f);
    }
    
    @Override
    public String toString() {
        return String.format("BoundarySegment[%s: (%.1f,%.1f) -> (%.1f,%.1f)]", 
                           type, startPoint.x, startPoint.y, endPoint.x, endPoint.y);
    }
}
