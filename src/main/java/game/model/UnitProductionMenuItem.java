package game.model;

/**
 * UnitProductionMenuItem.java
 *
 * PURPOSE:
 * Concrete implementation of ProductionMenuItem for unit production.
 * Extends the existing UnitProduction functionality with menu-specific features.
 *
 * DESIGN PRINCIPLES:
 * - Composition: Wraps existing UnitProduction logic
 * - DRY: Reuses existing production cost calculations and completion logic
 * - Single Responsibility: Handles unit-specific menu requirements
 */
public class UnitProductionMenuItem implements ProductionMenuItem {

    private final UnitProduction unitProduction;

    /**
     * Constructs a new UnitProductionMenuItem.
     *
     * @param unitType The type of unit to produce
     */
    public UnitProductionMenuItem(UnitType unitType) {
        this.unitProduction = new UnitProduction(unitType);
    }

    @Override
    public ProductionCategory getCategory() {
        return ProductionCategory.UNITS;
    }

    @Override
    public boolean canProduce(City city) {
        // Units can generally always be produced
        // Future: Could check for specific requirements like population, resources, etc.
        return true;
    }

    @Override
    public String getUnavailableReason(City city) {
        if (canProduce(city)) {
            return null;
        }
        // Future: Return specific reasons like "Requires Barracks" or "Insufficient population"
        return "Cannot produce this unit";
    }

    @Override
    public int getTurnsToComplete(City city) {
        if (!canProduce(city)) {
            return -1;
        }
        
        int productionPerTurn = city.getPopulation(); // Simple calculation for now
        if (productionPerTurn <= 0) {
            return -1;
        }
        
        return (int) Math.ceil((double) getProductionCost() / productionPerTurn);
    }

    @Override
    public String getTooltipText() {
        UnitType unitType = unitProduction.getUnitType();
        return String.format("Movement: %d, Vision: %d\n%s", 
            unitType.maxMovementBudget, 
            unitType.maxVisibilityBudget,
            getDescription());
    }

    // Delegate ProductionItem methods to the wrapped UnitProduction
    @Override
    public String getName() {
        return unitProduction.getName();
    }

    @Override
    public int getProductionCost() {
        return unitProduction.getProductionCost();
    }

    @Override
    public void onComplete(City city) {
        unitProduction.onComplete(city);
    }

    @Override
    public String getDescription() {
        return unitProduction.getDescription();
    }

    /**
     * Gets the underlying unit type.
     *
     * @return The unit type this menu item produces
     */
    public UnitType getUnitType() {
        return unitProduction.getUnitType();
    }
}