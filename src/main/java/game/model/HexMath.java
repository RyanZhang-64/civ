package game.model;

/**
 * HexMath.java
 *
 * PURPOSE:
 * A utility class to hold constants and static methods related to hexagonal
 * grid mathematics. This provides a single source of truth for direction vectors
 * to ensure all systems are consistent.
 */
public final class HexMath {

    private HexMath() {}

    /**
     * The canonical mapping of axial direction vectors for pointy-topped hexes,
     * synchronized with the rendering angle calculations. The order starts from
     * North-East and moves clockwise.
     *
     * 0: NE (North-East)
     * 1: E  (East)
     * 2: SE (South-East)
     * 3: SW (South-West)
     * 4: W  (West)
     * 5: NW (North-West)
     */
    public static final int[][] AXIAL_DIRECTIONS = {
        {+1, -1}, {+1, 0}, {0, +1},
        {-1, +1}, {-1, 0}, {0, -1}
    };
}
