package game;

// Import the classes this file uses
import processing.core.PApplet;
import processing.event.MouseEvent;

public class Main extends PApplet {

    private GameEngine gameEngine;

    @Override
    public void settings() {
        size(1280, 720);
    }

    @Override
    public void setup() {
        this.gameEngine = new GameEngine(this);
        this.gameEngine.setup();
    }

    @Override
    public void draw() {
        if (gameEngine == null) return;
        gameEngine.draw();
    }

    @Override
    public void mousePressed() {
        if (gameEngine == null) return;
        gameEngine.handleMousePressed();
    }

    @Override
    public void mouseReleased() {
        if (gameEngine == null) return;
        gameEngine.handleMouseReleased();
    }

    @Override
    public void mouseDragged() {
        if (gameEngine == null) return;
        gameEngine.handleMouseDragged();
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        if (gameEngine == null) return;
        gameEngine.handleMouseWheel(event);
    }

    @Override
    public void keyPressed() {
        if (gameEngine == null) return;
        gameEngine.handleKeyPressed();
    }

    public static void main(String[] args) {
        PApplet.main("game.Main");
    }
}
