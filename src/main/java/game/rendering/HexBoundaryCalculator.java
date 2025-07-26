package game.rendering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import game.GameConfig;
import game.model.Hex;
import game.model.HexGrid;
import game.model.HexMath;
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
        LinkedList<LineSegment> remainingSegments = new LinkedList<>(segments);
        
        while (!remainingSegments.isEmpty()) {
            List<PVector> currentPolygon = new ArrayList<>();
            LineSegment startSegment = remainingSegments.poll();
            currentPolygon.add(startSegment.start);
            PVector lastPoint = startSegment.end;
            currentPolygon.add(lastPoint);

            boolean closedLoop = false;
            while (!closedLoop && !remainingSegments.isEmpty()) {
                boolean foundNext = false;
                for (int i = 0; i < remainingSegments.size(); i++) {
                    LineSegment nextSegment = remainingSegments.get(i);
                    float tolerance = 1.0f;

                    if (PVector.dist(lastPoint, nextSegment.start) < tolerance) {
                        lastPoint = nextSegment.end;
                        currentPolygon.add(lastPoint);
                        remainingSegments.remove(i);
                        foundNext = true;
                        break;
                    } else if (PVector.dist(lastPoint, nextSegment.end) < tolerance) {
                        lastPoint = nextSegment.start;
                        currentPolygon.add(lastPoint);
                        remainingSegments.remove(i);
                        foundNext = true;
                        break;
                    }
                }
                if (!foundNext) {
                    closedLoop = true;
                }
            }
            allPolygons.add(currentPolygon);
        }
        return allPolygons;
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
            center[0] + GameConfig.HEX_RADIUS * p.cos(startAngle),
            center[1] + GameConfig.HEX_RADIUS * p.sin(startAngle)
        );
        PVector end = new PVector(
            center[0] + GameConfig.HEX_RADIUS * p.cos(endAngle),
            center[1] + GameConfig.HEX_RADIUS * p.sin(endAngle)
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
}
