package game;

import game.core.InputHandler;
import game.core.UnitManager;
import game.core.VisibilityManager;
import game.model.HexGrid;
import game.model.UnitType;
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
    private final VisibilityManager visibilityManager;
    private final HexGrid hexGrid;
    private final GameRenderer gameRenderer;
    private final UIManager uiManager;
    private final HexBoundaryCalculator hexBoundaryCalculator; // <-- FIX: Add field

    public GameEngine(PApplet p) {
        this.p = p;
        this.camera = new Camera(p);
        this.hexGrid = new HexGrid();
        this.visibilityManager = new VisibilityManager(hexGrid);
        this.unitManager = new UnitManager(hexGrid, visibilityManager, camera); // Updated constructor call
        this.uiManager = new UIManager(p, unitManager);
        this.hexBoundaryCalculator = new HexBoundaryCalculator(p);
        this.gameRenderer = new GameRenderer(p, camera, hexGrid, unitManager, visibilityManager, hexBoundaryCalculator);
        this.inputHandler = new InputHandler(p, camera, hexGrid, unitManager, uiManager);
    }

    public void setup() {
        hexGrid.generate(p);
        unitManager.spawnUnit(UnitType.SCOUT, 0, 0);
        unitManager.spawnUnit(UnitType.SETTLER, 1, -1);
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
