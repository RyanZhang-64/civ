package game.rendering;

import processing.core.PVector;

/**
 * Utility methods for vector operations used in boundary calculations.
 * Contains basic geometric operations without optimization.
 * 
 * PURPOSE:
 * Provides fundamental vector operations needed for boundary offset calculations.
 * Centralizes geometric math to ensure consistency and maintainability.
 * 
 * DESIGN PRINCIPLES:
 * - Static Methods: All operations are stateless and side-effect free
 * - Immutability: Input vectors are never modified, new vectors are returned
 * - Robustness: Handles edge cases like zero-length vectors
 * - Clarity: Method names clearly indicate their geometric purpose
 */
public class VectorUtils {
    private static final float EPSILON = 1e-6f;
    
    /**
     * Calculates the perpendicular vector (90 degrees counterclockwise).
     * This is the most common operation for boundary normal calculation.
     *
     * @param vector The input vector
     * @return A new vector perpendicular to the input
     */
    public static PVector getPerpendicular(PVector vector) {
        return new PVector(-vector.y, vector.x);
    }
    
    /**
     * Normalizes a vector to unit length.
     * Handles zero-length vectors gracefully.
     *
     * @param vector The vector to normalize
     * @return A new unit vector in the same direction, or zero vector if input is too small
     */
    public static PVector normalize(PVector vector) {
        PVector result = new PVector(vector.x, vector.y);
        float magnitude = result.mag();
        if (magnitude > EPSILON) {
            result.div(magnitude);
        } else {
            // Return zero vector for degenerate cases
            result.set(0, 0);
        }
        return result;
    }
    
    /**
     * Calculates the average of two normalized vectors.
     * Useful for computing smooth normal vectors at boundary vertices.
     *
     * @param normal1 First normalized vector
     * @param normal2 Second normalized vector
     * @return Normalized average of the two input vectors
     */
    public static PVector averageNormals(PVector normal1, PVector normal2) {
        PVector avg = PVector.add(normal1, normal2);
        return normalize(avg);
    }
    
    /**
     * Offsets a point by a distance in the direction of a normal vector.
     * This is the core operation for creating offset boundaries.
     *
     * @param point The original point
     * @param normal The direction vector (should be normalized)
     * @param distance The distance to offset (positive = along normal, negative = opposite)
     * @return A new point offset from the original
     */
    public static PVector offsetPoint(PVector point, PVector normal, float distance) {
        PVector offset = PVector.mult(normal, distance);
        return PVector.add(point, offset);
    }
    
    /**
     * Calculates the angle between two vectors in radians.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return Angle between vectors in radians [0, PI]
     */
    public static float angleBetween(PVector v1, PVector v2) {
        PVector n1 = normalize(v1);
        PVector n2 = normalize(v2);
        
        float dot = PVector.dot(n1, n2);
        // Clamp to handle floating point precision issues
        dot = Math.max(-1.0f, Math.min(1.0f, dot));
        
        return (float) Math.acos(dot);
    }
    
    /**
     * Calculates the cross product of two 2D vectors.
     * Returns the z-component of the 3D cross product.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return Z-component of cross product (positive = counterclockwise)
     */
    public static float cross2D(PVector v1, PVector v2) {
        return v1.x * v2.y - v1.y * v2.x;
    }
    
    /**
     * Linearly interpolates between two points.
     *
     * @param start Starting point
     * @param end Ending point
     * @param t Interpolation parameter [0, 1]
     * @return Interpolated point
     */
    public static PVector lerp(PVector start, PVector end, float t) {
        return PVector.lerp(start, end, t);
    }
    
    /**
     * Checks if two vectors are approximately equal within epsilon.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return true if vectors are approximately equal
     */
    public static boolean approxEqual(PVector v1, PVector v2) {
        return Math.abs(v1.x - v2.x) < EPSILON && Math.abs(v1.y - v2.y) < EPSILON;
    }
    
    /**
     * Calculates the distance between two points.
     *
     * @param p1 First point
     * @param p2 Second point
     * @return Euclidean distance between points
     */
    public static float distance(PVector p1, PVector p2) {
        return PVector.dist(p1, p2);
    }
    
    /**
     * Creates a vector from angle and magnitude.
     *
     * @param angle Angle in radians
     * @param magnitude Vector length
     * @return New vector with specified angle and magnitude
     */
    public static PVector fromPolar(float angle, float magnitude) {
        return new PVector((float) (magnitude * Math.cos(angle)), 
                          (float) (magnitude * Math.sin(angle)));
    }
}
