package game.model;

import java.util.Objects;

/**
 * Hex.java
 *
 * PURPOSE:
 * A pure data class representing a single hexagonal tile on the game map.
 * It holds its axial coordinates and its assigned biome. This class contains no
 * rendering or game logic.
 *
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Its only job is to store the state of a hex tile.
 * - Data Class: Acts as a simple container for data.
 * - Immutability: The coordinates (q, r) are final and cannot be changed after
 * creation, ensuring the grid structure is stable.
 */
public class Hex {

    /**
     * The axial q-coordinate of the hex. This is final and cannot be changed.
     */
    public final int q;

    /**
     * The axial r-coordinate of the hex. This is final and cannot be changed.
     */
    public final int r;

    /**
     * The biome (terrain type) of this hex. This is assigned during map generation.
     */
    public Biome biome;

    /**
     * Constructs a new Hex with the specified axial coordinates.
     * The biome is left unassigned to be set later by a map generator.
     *
     * @param q The axial q-coordinate.
     * @param r The axial r-coordinate.
     */
    public Hex(int q, int r) {
        this.q = q;
        this.r = r;
        this.biome = null; // Will be assigned by HexGrid during generation.
    }

    /**
     * Determines if this Hex is equal to another object.
     * Two Hex objects are considered equal if they have the same q and r coordinates.
     *
     * @param obj The object to compare against.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Hex hex = (Hex) obj;
        return q == hex.q && r == hex.r;
    }

    /**
     * Generates a hash code for this Hex.
     * The hash code is based on the q and r coordinates, which uniquely identify a hex.
     * This is essential for using Hex objects in HashSets and HashMaps.
     *
     * @return The hash code for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }

    /**
     * Provides a string representation of the Hex, useful for debugging.
     *
     * @return A string in the format "Hex[q,r]".
     */
    @Override
    public String toString() {
        return "Hex[" + q + "," + r + "]";
    }
}
