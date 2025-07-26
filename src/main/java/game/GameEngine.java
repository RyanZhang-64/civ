package game;

import game.core.CivilizationManager;
import game.core.CityManager;
import game.core.InputHandler;
import game.core.UnitManager;
import game.model.HexGrid;
import game.rendering.GameRenderer;
import game.rendering.HexBoundaryCalculator; // <-- FIX: Import the calculator
import game.rendering.UIManager;
import game.Camera;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class GameEngine {

    private final PApplet p;
    private final Camera camera;
    private final InputHandler inputHandler;
    private final UnitManager unitManager;
    private final CivilizationManager civilizationManager;
    private final CityManager cityManager;
    private final HexGrid hexGrid;
    private final GameRenderer gameRenderer;
    private final UIManager uiManager;
    private final HexBoundaryCalculator hexBoundaryCalculator; // <-- FIX: Add field

    public GameEngine(PApplet p) {
        this.p = p;
        this.camera = new Camera(p);
        this.hexGrid = new HexGrid();
        this.civilizationManager = new CivilizationManager(hexGrid);
        this.cityManager = new CityManager(hexGrid, civilizationManager);
        this.unitManager = new UnitManager(hexGrid, camera, civilizationManager);
        this.uiManager = new UIManager(p, unitManager, civilizationManager, cityManager, camera);
        this.hexBoundaryCalculator = new HexBoundaryCalculator(p);
        this.gameRenderer = new GameRenderer(p, camera, hexGrid, unitManager, civilizationManager, cityManager, hexBoundaryCalculator);
        this.inputHandler = new InputHandler(p, camera, hexGrid, unitManager, uiManager);
    }

    public void setup() {
        hexGrid.generate(p);
        civilizationManager.setCityManager(cityManager);
        civilizationManager.setUnitManager(unitManager);
        civilizationManager.initializeTestScenario();
    }

    public void draw() {
        gameRenderer.render();
        uiManager.render();
    }

    public void handleMousePressed() { inputHandler.handleMousePressed(); }
    public void handleMouseReleased() { inputHandler.handleMouseReleased(); }
    public void handleMouseDragged() { inputHandler.handleMouseDragged(); }
    public void handleMouseWheel(MouseEvent event) { inputHandler.handleMouseWheel(event); }
    public void handleKeyPressed() { inputHandler.handleKeyPressed(); }
}
