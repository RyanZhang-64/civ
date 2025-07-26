package game.core;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import game.model.Biome;
import game.model.Hex;
import game.model.HexGrid;
import game.model.Unit;
import game.model.VisibilityState;

public class VisibilityManager {

    private final HexGrid hexGrid;
    private final Map<Hex, VisibilityState> visibilityMap;

    public VisibilityManager(HexGrid grid) {
        this.hexGrid = grid;
        this.visibilityMap = new HashMap<>();
        initializeVisibility();
    }

    private void initializeVisibility() {
        for (Hex hex : hexGrid.getAllHexes()) {
            visibilityMap.put(hex, VisibilityState.UNDISCOVERED);
        }
    }

    public void updateGlobalVisibility(List<Unit> allUnits) {
        Set<Hex> currentlyVisibleHexes = new HashSet<>();
        for (Unit unit : allUnits) {
            currentlyVisibleHexes.addAll(calculateVisibleHexesForUnit(unit));
        }

        for (Hex hex : hexGrid.getAllHexes()) {
            VisibilityState currentState = visibilityMap.get(hex);
            if (currentlyVisibleHexes.contains(hex)) {
                visibilityMap.put(hex, VisibilityState.CURRENTLY_VISIBLE);
            } else if (currentState == VisibilityState.CURRENTLY_VISIBLE) {
                visibilityMap.put(hex, VisibilityState.DISCOVERED);
            }
        }
    }

    public Set<Hex> calculateVisibleHexesForUnit(Unit unit) {
        Set<Hex> visibleHexes = new HashSet<>();
        Hex startHex = hexGrid.getHexAt(unit.q, unit.r);
        if (startHex == null) return visibleHexes;

        PriorityQueue<PathNode> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
        Map<Hex, Integer> costSoFar = new HashMap<>();

        queue.add(new PathNode(startHex, 0));
        costSoFar.put(startHex, 0);
        visibleHexes.add(startHex);

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            if (current.cost > unit.type.maxVisibilityBudget) continue;

            for (Hex neighbor : hexGrid.getNeighbors(current.hex)) {
                int newCost = current.cost + neighbor.biome.visibilityCost;
                if (newCost <= unit.type.maxVisibilityBudget) {
                    if (!costSoFar.containsKey(neighbor) || newCost < costSoFar.get(neighbor)) {
                        costSoFar.put(neighbor, newCost);
                        visibleHexes.add(neighbor);
                        if (neighbor.biome != Biome.MOUNTAINS && neighbor.biome != Biome.PEAKS) {
                            queue.add(new PathNode(neighbor, newCost));
                        }
                    }
                }
            }
        }
        return visibleHexes;
    }

    public VisibilityState getVisibilityState(Hex hex) {
        return visibilityMap.getOrDefault(hex, VisibilityState.UNDISCOVERED);
    }

    /**
     * Returns a set of all hexes that are not currently undiscovered.
     */
    public Set<Hex> getDiscoveredHexes() {
        Set<Hex> discovered = new HashSet<>();
        for (Map.Entry<Hex, VisibilityState> entry : visibilityMap.entrySet()) {
            if (entry.getValue() != VisibilityState.UNDISCOVERED) {
                discovered.add(entry.getKey());
            }
        }
        return discovered;
    }

    /**
     * Returns a set of all hexes that are currently visible.
     */
    public Set<Hex> getCurrentlyVisibleHexes() {
        Set<Hex> visible = new HashSet<>();
        for (Map.Entry<Hex, VisibilityState> entry : visibilityMap.entrySet()) {
            if (entry.getValue() == VisibilityState.CURRENTLY_VISIBLE) {
                visible.add(entry.getKey());
            }
        }
        return visible;
    }

    private static class PathNode {
        final Hex hex;
        final int cost;
        PathNode(Hex hex, int cost) { this.hex = hex; this.cost = cost; }
    }
}
