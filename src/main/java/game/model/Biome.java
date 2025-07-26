package game.model;

/**
 * Defines the different types of terrain (biomes) in the game world.
 * Each biome has associated costs and a specific color for rendering.
 */
public enum Biome {
    DEEP_OCEAN(100000, 0xFF003278, 2),
    SHALLOW_OCEAN(100000, 0xFF0064B4, 2),
    BEACH(2, 0xFFC2B280, 1),
    GRASSLAND(1, 0xFF327832, 1),
    FOREST(3, 0xFF225022, 3),
    HILLS(4, 0xFF654321, 2),
    MOUNTAINS(8, 0xFF786450, 4),
    PEAKS(100000, 0xFFF0F0F0, 5);

    public final int movementCost;
    public final int biomeColor;
    public final int visibilityCost;

    /**
     * Constructor for a Biome.
     * @param moveCost The cost in movement points to enter a hex of this type.
     * @param biomeCol The ARGB color code used to render this biome.
     * @param visCost The cost in vision points to see through a hex of this type.
     */
    Biome(int moveCost, int biomeCol, int visCost) {
        this.movementCost = moveCost;
        this.biomeColor = biomeCol;
        this.visibilityCost = visCost;
    }
}
