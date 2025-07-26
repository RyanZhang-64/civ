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
    private final HexBoundaryCalculator hexBoundaryCalculator;

    /**
     * Constructs a GameEngine with all dependencies injected.
     * Use GameObjectFactory.createGameEngine() to create instances.
     *
     * @param p The PApplet instance for rendering
     * @param camera The camera for view management
     * @param hexGrid The hex grid for the game world
     * @param civilizationManager The civilization manager
     * @param cityManager The city manager
     * @param unitManager The unit manager
     * @param uiManager The UI manager
     * @param gameRenderer The game renderer
     * @param inputHandler The input handler
     * @param hexBoundaryCalculator The hex boundary calculator
     */
    public GameEngine(PApplet p, Camera camera, HexGrid hexGrid, CivilizationManager civilizationManager,
                     CityManager cityManager, UnitManager unitManager, UIManager uiManager,
                     GameRenderer gameRenderer, InputHandler inputHandler, HexBoundaryCalculator hexBoundaryCalculator) {
        this.p = p;
        this.camera = camera;
        this.hexGrid = hexGrid;
        this.civilizationManager = civilizationManager;
        this.cityManager = cityManager;
        this.unitManager = unitManager;
        this.uiManager = uiManager;
        this.gameRenderer = gameRenderer;
        this.inputHandler = inputHandler;
        this.hexBoundaryCalculator = hexBoundaryCalculator;
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
