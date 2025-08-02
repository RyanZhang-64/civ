package game.model;

/**
 * Defines the different types of terrain (biomes) in the game world.
 * Each biome has associated costs, yields, and a specific color for rendering.
 */
public enum Biome {
    DEEP_OCEAN(100000, 0xFF003278, 2, 1, 0),      // 1 Food, 0 Production
    SHALLOW_OCEAN(100000, 0xFF0064B4, 2, 1, 0),   // 1 Food, 0 Production  
    BEACH(2, 0xFFC2B280, 1, 1, 0),                // 1 Food, 0 Production
    GRASSLAND(1, 0xFF327832, 1, 2, 0),            // 2 Food, 0 Production
    FOREST(3, 0xFF225022, 3, 1, 2),               // 1 Food, 2 Production
    HILLS(4, 0xFF654321, 2, 0, 2),                // 0 Food, 2 Production
    MOUNTAINS(8, 0xFF786450, 4, 0, 1),            // 0 Food, 1 Production
    PEAKS(100000, 0xFFF0F0F0, 5, 0, 0);           // 0 Food, 0 Production

    public final int movementCost;
    public final int biomeColor;
    public final int visibilityCost;
    public final int foodYield;
    public final int productionYield;

    /**
     * Constructor for a Biome.
     * @param moveCost The cost in movement points to enter a hex of this type.
     * @param biomeCol The ARGB color code used to render this biome.
     * @param visCost The cost in vision points to see through a hex of this type.
     * @param foodYield The food yield this biome provides when worked by a city.
     * @param productionYield The production yield this biome provides when worked by a city.
     */
    Biome(int moveCost, int biomeCol, int visCost, int foodYield, int productionYield) {
        this.movementCost = moveCost;
        this.biomeColor = biomeCol;
        this.visibilityCost = visCost;
        this.foodYield = foodYield;
        this.productionYield = productionYield;
    }

    /**
     * Gets the total yield value for this biome (food + production).
     * Used for automatic tile assignment algorithms.
     *
     * @return The combined yield value
     */
    public int getTotalYield() {
        return foodYield + productionYield;
    }

    /**
     * Checks if this biome can be worked by cities.
     * Some biomes like PEAKS may be unworkable.
     *
     * @return true if the biome can be worked, false otherwise
     */
    public boolean isWorkable() {
        return this != PEAKS; // Peaks are unworkable
    }

    /**
     * Gets a human-readable description of this biome's yields.
     *
     * @return String describing the yields
     */
    public String getYieldDescription() {
        return "Food: " + foodYield + ", Production: " + productionYield;
    }
}
