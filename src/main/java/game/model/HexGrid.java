package game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.GameConfig;
import processing.core.PApplet;

public class HexGrid {

    private final Map<String, Hex> hexes;
    private final int gridRadius;

    public HexGrid() {
        this.hexes = new HashMap<>();
        this.gridRadius = GameConfig.GRID_RADIUS;
    }

    public void generate(PApplet p) {
        hexes.clear();
        for (int q = -gridRadius; q <= gridRadius; q++) {
            int r1 = Math.max(-gridRadius, -q - gridRadius);
            int r2 = Math.min(gridRadius, -q + gridRadius);
            for (int r = r1; r <= r2; r++) {
                Hex hex = new Hex(q, r);
                assignBiome(hex, p);
                hexes.put(q + "," + r, hex);
            }
        }
    }

    public Hex getHexAt(int q, int r) {
        return hexes.get(q + "," + r);
    }

    /**
     * Finds all valid neighbors of a given hex using the canonical directions.
     */
    public List<Hex> getNeighbors(Hex hex) {
        List<Hex> neighbors = new ArrayList<>();
        // Use the centralized direction vectors from HexMath
        for (int[] dir : HexMath.AXIAL_DIRECTIONS) {
            Hex neighbor = getHexAt(hex.q + dir[0], hex.r + dir[1]);
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    public List<Hex> getAllHexes() {
        return new ArrayList<>(hexes.values());
    }

    private void assignBiome(Hex hex, PApplet p) {
        float noiseValue = p.noise(
            (float)hex.q * GameConfig.NOISE_SCALE,
            (float)hex.r * GameConfig.NOISE_SCALE
        );

        if (noiseValue < 0.15) hex.biome = Biome.DEEP_OCEAN;
        else if (noiseValue < 0.25) hex.biome = Biome.SHALLOW_OCEAN;
        else if (noiseValue < 0.35) hex.biome = Biome.BEACH;
        else if (noiseValue < 0.55) hex.biome = Biome.GRASSLAND;
        else if (noiseValue < 0.65) hex.biome = Biome.FOREST;
        else if (noiseValue < 0.75) hex.biome = Biome.HILLS;
        else if (noiseValue < 0.90) hex.biome = Biome.MOUNTAINS;
        else hex.biome = Biome.PEAKS;
    }
}
