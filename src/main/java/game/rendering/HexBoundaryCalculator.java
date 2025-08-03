package game.rendering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import game.GameConfig;
import game.model.Hex;
import game.model.HexGrid;
import game.model.HexMath;
import game.model.BoundarySegment;
import game.model.DualBoundary;
import game.model.enums.BoundaryType;
import processing.core.PApplet;
import processing.core.PVector;

public class HexBoundaryCalculator {

    private final PApplet p;

    public HexBoundaryCalculator(PApplet p) {
        this.p = p;
    }

    public List<List<PVector>> calculateBoundaries(Set<Hex> hexSet, HexGrid hexGrid) {
        if (hexSet == null || hexSet.isEmpty()) {
            return new ArrayList<>();
        }
        Set<BoundaryEdge> boundaryEdges = findBoundaryEdges(hexSet, hexGrid);
        List<LineSegment> segments = convertEdgesToSegments(boundaryEdges);
        return stitchSegmentsIntoPolygons(segments);
    }

    private Set<BoundaryEdge> findBoundaryEdges(Set<Hex> hexSet, HexGrid hexGrid) {
        Set<BoundaryEdge> edges = new HashSet<>();
        for (Hex hex : hexSet) {
            for (int i = 0; i < 6; i++) {
                int[] dir = HexMath.AXIAL_DIRECTIONS[i];
                Hex neighbor = hexGrid.getHexAt(hex.q + dir[0], hex.r + dir[1]);
                if (neighbor == null || !hexSet.contains(neighbor)) {
                    // Add 3 to the direction to get the opposite edge, matching the original implementation
                    edges.add(new BoundaryEdge(hex, (i + 3) % 6));
                }
            }
        }
        return edges;
    }
    
    private List<LineSegment> convertEdgesToSegments(Set<BoundaryEdge> edges) {
        List<LineSegment> segments = new ArrayList<>();
        for (BoundaryEdge edge : edges) {
            segments.add(getEdgeLineSegment(edge.hex, edge.direction));
        }
        return segments;
    }

    private List<List<PVector>> stitchSegmentsIntoPolygons(List<LineSegment> segments) {
        List<List<PVector>> allPolygons = new ArrayList<>();
        
        if (segments.isEmpty()) {
            return allPolygons;
        }
        
        // Simple approach: create a single polygon by connecting segment endpoints
        List<PVector> polygon = new ArrayList<>();
        Set<PVector> addedVertices = new HashSet<>();
        
        // Start with first segment
        LineSegment firstSegment = segments.get(0);
        polygon.add(firstSegment.start);
        addedVertices.add(firstSegment.start);
        
        PVector currentPoint = firstSegment.end;
        
        // Find the next connected segment iteratively
        List<LineSegment> remainingSegments = new ArrayList<>(segments.subList(1, segments.size()));
        
        while (!remainingSegments.isEmpty()) {
            boolean foundConnection = false;
            float tolerance = 2.0f; // Slightly larger tolerance
            
            for (int i = 0; i < remainingSegments.size(); i++) {
                LineSegment segment = remainingSegments.get(i);
                
                if (PVector.dist(currentPoint, segment.start) < tolerance) {
                    // Connect to start of this segment
                    if (!isVertexAlreadyAdded(segment.start, addedVertices, tolerance)) {
                        polygon.add(segment.start);
                        addedVertices.add(segment.start);
                    }
                    currentPoint = segment.end;
                    remainingSegments.remove(i);
                    foundConnection = true;
                    break;
                    
                } else if (PVector.dist(currentPoint, segment.end) < tolerance) {
                    // Connect to end of this segment (reverse direction)
                    if (!isVertexAlreadyAdded(segment.end, addedVertices, tolerance)) {
                        polygon.add(segment.end);
                        addedVertices.add(segment.end);
                    }
                    currentPoint = segment.start;
                    remainingSegments.remove(i);
                    foundConnection = true;
                    break;
                }
            }
            
            if (!foundConnection) {
                break;
            }
            
            // Check if we've closed the loop
            if (PVector.dist(currentPoint, firstSegment.start) < tolerance) {
                break;
            }
        }
        
        allPolygons.add(polygon);
        return allPolygons;
    }

    /**
     * Helper method to check if a vertex is already added within tolerance.
     */
    private boolean isVertexAlreadyAdded(PVector vertex, Set<PVector> addedVertices, float tolerance) {
        for (PVector existing : addedVertices) {
            if (PVector.dist(vertex, existing) < tolerance) {
                return true;
            }
        }
        return false;
    }

     /**
     * Uses the exact angle calculation from the original code.
     * The critical points are:
     * 1. Angles start from PI/2 (90°), not PI/6 (30°)
     * 2. The direction system is reversed compared to HexMath.AXIAL_DIRECTIONS
     * 3. Each vertex is 60° (PI/3) apart
     */
    private LineSegment getEdgeLineSegment(Hex hex, int edgeIndex) {
        float[] center = hexToPixel(hex.q, hex.r);
        
        float startAngle = PApplet.PI / 3 * edgeIndex + PApplet.PI / 2;
        float endAngle = PApplet.PI / 3 * ((edgeIndex + 1) % 6) + PApplet.PI / 2;

        PVector start = new PVector(
            center[0] + GameConfig.HEX_RADIUS * PApplet.cos(startAngle),
            center[1] + GameConfig.HEX_RADIUS * PApplet.sin(startAngle)
        );
        PVector end = new PVector(
            center[0] + GameConfig.HEX_RADIUS * PApplet.cos(endAngle),
            center[1] + GameConfig.HEX_RADIUS * PApplet.sin(endAngle)
        );
        
        return new LineSegment(start, end);
    }

    private float[] hexToPixel(int q, int r) {
        float x = GameConfig.HEX_RADIUS * (PApplet.sqrt(3) * q + PApplet.sqrt(3) / 2.0f * r);
        float y = GameConfig.HEX_RADIUS * (3.0f / 2.0f * r);
        return new float[]{x, y};
    }

    private static class BoundaryEdge {
        final Hex hex;
        final int direction;
        BoundaryEdge(Hex hex, int direction) { this.hex = hex; this.direction = direction; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BoundaryEdge that = (BoundaryEdge) o;
            return direction == that.direction && hex.equals(that.hex);
        }
        @Override
        public int hashCode() { return Objects.hash(hex, direction); }
    }

    private static class LineSegment {
        final PVector start, end;
        LineSegment(PVector start, PVector end) { this.start = start; this.end = end; }
    }

    /**
     * Calculates dual boundaries (inner and outer) for a set of hexes.
     * 
     * @param hexes The hexes to create boundaries for
     * @param hexGrid The hex grid for neighbor calculations
     * @param innerOffset Distance to offset inner boundary (negative for inward)
     * @param outerOffset Distance to offset outer boundary (positive for outward)
     * @return DualBoundary containing all three boundary types
     */
    public DualBoundary calculateDualBoundary(Set<Hex> hexes, HexGrid hexGrid, float innerOffset, float outerOffset) {
        if (hexes == null || hexes.isEmpty()) {
            throw new IllegalArgumentException("Hex set cannot be null or empty");
        }
        
        // First calculate the standard boundary using existing method
        List<BoundarySegment> standardBoundary = calculateStandardBoundarySegments(hexes, hexGrid);
        
        // Calculate offset boundaries at midpoint positions
        float innerMidpointOffset = BoundaryRenderConfig.calculateMidpointOffset(innerOffset);
        float outerMidpointOffset = BoundaryRenderConfig.calculateMidpointOffset(outerOffset);
        
        List<BoundarySegment> innerBoundary = calculateOffsetBoundary(standardBoundary, innerMidpointOffset, BoundaryType.INNER);
        List<BoundarySegment> outerBoundary = calculateOffsetBoundary(standardBoundary, outerMidpointOffset, BoundaryType.OUTER);
        
        return new DualBoundary(standardBoundary, innerBoundary, outerBoundary);
    }

    /**
     * Converts existing boundary calculation to use BoundarySegment objects.
     * This creates a single polygon from the hex set (assumes connected region).
     * 
     * @param hexes The hexes to create boundary for
     * @param hexGrid The hex grid for neighbor calculations
     * @return List of boundary segments forming a closed polygon
     */
    private List<BoundarySegment> calculateStandardBoundarySegments(Set<Hex> hexes, HexGrid hexGrid) {
        // Use existing boundary calculation to get polygons
        List<List<PVector>> polygons = calculateBoundaries(hexes, hexGrid);
        
        if (polygons.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Take the first (and usually only) polygon
        List<PVector> polygon = polygons.get(0);
        
        List<BoundarySegment> segments = new ArrayList<>();
        
        // Determine if the polygon is clockwise or counterclockwise
        boolean isClockwise = isPolygonClockwise(polygon);
        
        for (int i = 0; i < polygon.size(); i++) {
            PVector start = polygon.get(i);
            PVector end = polygon.get((i + 1) % polygon.size());
            
            // Calculate edge vector and normal
            PVector edgeVector = PVector.sub(end, start);
            PVector normal = VectorUtils.getPerpendicular(VectorUtils.normalize(edgeVector));
            
            // Ensure normal points inward (toward the bounded region)
            // For clockwise polygons, perpendicular points inward
            // For counterclockwise polygons, we need to reverse it
            if (!isClockwise) {
                normal.mult(-1);
            }
            
            segments.add(new BoundarySegment(start, end, normal, BoundaryType.STANDARD));
        }
        
        return segments;
    }
    
    /**
     * Determines if a polygon is wound clockwise using the shoelace formula.
     * 
     * @param polygon List of vertices in order
     * @return true if clockwise, false if counterclockwise
     */
    private boolean isPolygonClockwise(List<PVector> polygon) {
        float sum = 0;
        for (int i = 0; i < polygon.size(); i++) {
            PVector current = polygon.get(i);
            PVector next = polygon.get((i + 1) % polygon.size());
            sum += (next.x - current.x) * (next.y + current.y);
        }
        return sum > 0; // Positive sum indicates clockwise winding
    }

    /**
     * Calculates an offset boundary from a standard boundary.
     * 
     * @param standardBoundary The original boundary segments
     * @param offsetDistance Distance to offset (positive = outward, negative = inward)
     * @param boundaryType The type of boundary being calculated
     * @return List of offset boundary segments
     */
    private List<BoundarySegment> calculateOffsetBoundary(List<BoundarySegment> standardBoundary, 
                                                         float offsetDistance, 
                                                         BoundaryType boundaryType) {
        if (standardBoundary.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<BoundarySegment> offsetSegments = new ArrayList<>();
        int segmentCount = standardBoundary.size();
        
        // Calculate offset vertices first
        List<PVector> offsetVertices = new ArrayList<>();
        for (int i = 0; i < segmentCount; i++) {
            BoundarySegment currentSegment = standardBoundary.get(i);
            BoundarySegment nextSegment = standardBoundary.get((i + 1) % segmentCount);
            
            PVector offsetVertex = calculateOffsetVertex(currentSegment, nextSegment, offsetDistance);
            offsetVertices.add(offsetVertex);
        }
        
        // Create offset segments from offset vertices
        for (int i = 0; i < offsetVertices.size(); i++) {
            PVector start = offsetVertices.get(i);
            PVector end = offsetVertices.get((i + 1) % offsetVertices.size());
            
            // Calculate normal for offset segment
            PVector edgeVector = PVector.sub(end, start);
            PVector normal = VectorUtils.getPerpendicular(VectorUtils.normalize(edgeVector));
            
            offsetSegments.add(new BoundarySegment(start, end, normal, boundaryType));
        }
        
        return offsetSegments;
    }

    /**
     * Calculates the offset position for a vertex where two boundary segments meet.
     * 
     * @param segment1 First segment ending at the vertex
     * @param segment2 Second segment starting at the vertex
     * @param offsetDistance Distance to offset the vertex
     * @return The offset vertex position
     */
    private PVector calculateOffsetVertex(BoundarySegment segment1, BoundarySegment segment2, float offsetDistance) {
        // Get the vertex where segments meet
        PVector vertex = segment1.getEndPoint(); // Should equal segment2.getStartPoint()
        
        // Calculate average normal at this vertex
        PVector normal1 = segment1.getNormal();
        PVector normal2 = segment2.getNormal();
        PVector averageNormal = VectorUtils.averageNormals(normal1, normal2);
        
        // Apply offset
        return VectorUtils.offsetPoint(vertex, averageNormal, offsetDistance);
    }
}
