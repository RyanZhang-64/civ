package game.model;

/**
 * UnitProduction.java
 *
 * PURPOSE:
 * Concrete implementation of ProductionItem for producing units.
 * When completed, spawns a new unit of the specified type at the city location.
 *
 * DESIGN PRINCIPLES:
 * - Strategy Pattern: Implements ProductionItem interface
 * - Single Responsibility: Handles only unit production
 * - Immutable: Production parameters don't change once created
 */
public class UnitProduction implements ProductionItem {

    private final UnitType unitType;
    private final int productionCost;

    /**
     * Constructs a new UnitProduction.
     *
     * @param unitType The type of unit to produce.
     */
    public UnitProduction(UnitType unitType) {
        this.unitType = unitType;
        // Calculate production cost based on unit type
        this.productionCost = calculateProductionCost(unitType);
    }

    /**
     * Calculates the production cost for a given unit type.
     * This could be moved to UnitType or GameConfig in the future.
     *
     * @param type The unit type.
     * @return The production cost.
     */
    private int calculateProductionCost(UnitType type) {
        switch (type) {
            case SCOUT:
                return 25;
            case SETTLER:
                return 50;
            default:
                return 30; // Default cost
        }
    }

    @Override
    public String getName() {
        return unitType.name();
    }

    @Override
    public int getProductionCost() {
        return productionCost;
    }

    @Override
    public void onComplete(City city) {
        // Spawn the unit at the city's location
        city.getOwner().spawnUnit(unitType, city.getQ(), city.getR());
    }

    @Override
    public String getDescription() {
        return "Produces a " + unitType.name().toLowerCase() + " unit";
    }

    /**
     * Gets the unit type this production will create.
     *
     * @return The unit type.
     */
    public UnitType getUnitType() {
        return unitType;
    }

    @Override
    public String toString() {
        return "UnitProduction[" + unitType.name() + " cost:" + productionCost + "]";
    }
}
