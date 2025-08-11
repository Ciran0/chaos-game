package com.chaosgame.view;

import com.chaosgame.Physics;
import com.chaosgame.Vector2D;
import com.chaosgame.ViewManager;
import com.chaosgame.entity.Entity;
import com.chaosgame.entity.Hand;
import com.chaosgame.entity.Player;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract base class for any "playable" view that contains a game loop,
 * physics, entities, and player controls.
 */
public abstract class AbstractPlayableLevelView {

  // View and Scene Management
  protected Scene scene;
  protected Pane root;
  protected ViewManager viewManager;

  // Game Engine Components
  protected AnimationTimer gameLoop;
  protected List<Entity> entities = new ArrayList<>();
  protected Player player;

  // Input State
  private long lastUpdate = 0;
  private double mouseX = 0;
  private double mouseY = 0;
  private Set<KeyCode> pressedKeys = new HashSet<>();

  // Constants
  public static final int WIDTH = 1280;
  public static final int HEIGHT = 720;

  public AbstractPlayableLevelView(ViewManager viewManager) {
    this.viewManager = viewManager;
    this.root = new Pane();
    this.root.setStyle("-fx-background-color: #1a1a1a;");
    this.scene = new Scene(root, WIDTH, HEIGHT);

    // This abstract method must be implemented by subclasses to add entities.
    setupLevel();

    // Setup all listeners and the game loop
    initializeListeners();
    initializeGameLoop();
  }

  /**
   * Subclasses must implement this method to define the content of the level.
   * This is where you will create the player, crates, guards, etc.
   */
  protected abstract void setupLevel();

  private void initializeListeners() {
    scene.setOnMouseMoved(event -> {
      mouseX = event.getSceneX();
      mouseY = event.getSceneY();
    });

    // This handles mouse movement when a button IS pressed
    scene.setOnMouseDragged(event -> {
      mouseX = event.getSceneX();
      mouseY = event.getSceneY();
    });

    scene.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
    scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));

    scene.setOnMousePressed(event -> {
      if (event.isPrimaryButtonDown()) {
        player.startGrabbing();
      }
    });
    scene.setOnMouseReleased(event -> {
      if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
        player.releaseObject();
      }
    });
  }

  private void initializeGameLoop() {
    this.gameLoop = new AnimationTimer() {
      @Override
      public void handle(long now) {
        if (lastUpdate == 0) {
          lastUpdate = now;
          return;
        }
        double delta = (now - lastUpdate) / 1_000_000_000.0;
        update(delta);
        lastUpdate = now;
      }
    };
  }

  protected void addEntity(Entity entity) {
    entities.add(entity);
    root.getChildren().add(entity.getView());
  }

  public void start() {
    gameLoop.start();
  }

  public Scene getScene() {
    return this.scene;
  }

  private void update(double delta) {
    player.handleInput(pressedKeys);
    player.updateHand(mouseX, mouseY);

    for (Entity entity : entities) {
      entity.update(delta);
    }

    for (int i = 0; i < 2; i++) {
      checkCollisions();
    }
  }

  private void checkCollisions() {
    if (player.isGrabbing() && !player.isHoldingObject()) {
      for (Entity entity : entities) {
        if (entity instanceof Player || !entity.isPhysical)
          continue;
        Physics.CollisionResult grabResult = Physics.checkCollision(player.getHand(), entity);
        if (grabResult.isColliding) {
          player.grabObject(entity);
          break;
        }
      }
    }

    for (int i = 0; i < entities.size(); i++) {
      for (int j = i + 1; j < entities.size(); j++) {
        Entity e1 = entities.get(i);
        Entity e2 = entities.get(j);

        // This is the corrected logic that fixes the hand rotation bug
        if (!e1.isPhysical || !e2.isPhysical) {
          continue;
        }

        Physics.CollisionResult result = Physics.checkCollision(e1, e2);
        if (result.isColliding) {
          Vector2D mtv = result.mtv;
          double totalMass = e1.mass + e2.mass;
          e1.setX(e1.getX() - mtv.x * (e2.mass / totalMass));
          e1.setY(e1.getY() - mtv.y * (e2.mass / totalMass));
          e2.setX(e2.getX() + mtv.x * (e1.mass / totalMass));
          e2.setY(e2.getY() + mtv.y * (e1.mass / totalMass));
          handleCollisionVelocity(e1, e2);
        }
      }
    }
  }

  private void handleCollisionVelocity(Entity a, Entity b) {
    double dx = b.getX() - a.getX();
    double dy = b.getY() - a.getY();
    double distance = Math.sqrt(dx * dx + dy * dy);
    if (distance == 0)
      return;
    double normalX = dx / distance;
    double normalY = dy / distance;
    double p1 = a.getVx() * normalX + a.getVy() * normalY;
    double p2 = b.getVx() * normalX + b.getVy() * normalY;
    double newP1 = (p1 * (a.mass - b.mass) + 2 * b.mass * p2) / (a.mass + b.mass);
    double newP2 = (p1 * 2 * a.mass - p2 * (a.mass - b.mass)) / (a.mass + b.mass);
    a.setVx(a.getVx() + (newP1 - p1) * normalX);
    a.setVy(a.getVy() + (newP1 - p1) * normalY);
    b.setVx(b.getVx() + (newP2 - p2) * normalX);
    b.setVy(b.getVy() + (newP2 - p2) * normalY);
  }
}
