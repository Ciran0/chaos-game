package com.chaosgame.view;

import com.chaosgame.ViewManager;
import com.chaosgame.entity.Entity;
import com.chaosgame.entity.Player;
import com.chaosgame.entity.Hand;
import com.chaosgame.entity.Crate;
import com.chaosgame.Physics;
import com.chaosgame.Vector2D;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class GameView {
  private Scene scene;
  private Pane root; // The root pane where we will draw our game

  private double mouseX = 0;
  private double mouseY = 0;

  private ViewManager viewManager;
  private AnimationTimer gameLoop;
  private long lastUpdate = 0;
  private Player player;
  private Set<KeyCode> pressedKeys = new HashSet<>();

  private List<Entity> entities = new ArrayList<>();

  // Game constants
  public static final int WIDTH = 1280;
  public static final int HEIGHT = 720;

  public GameView(ViewManager viewManager) {
    this.viewManager = viewManager;
    this.root = new Pane(); // A Pane allows absolute positioning of elements
    this.root.setStyle("-fx-background-color: #1a1a1a;");

    this.scene = new Scene(root, WIDTH, HEIGHT);
    // We will add key listeners and the game loop here

    addEntity(new Crate(200, 200));
    addEntity(new Crate(1000, 500));
    addEntity(new Crate(400, 600));

    Hand hand = new Hand();
    this.player = new Player(WIDTH / 2, HEIGHT / 2, hand);
    addEntity(this.player);
    addEntity(hand);

    scene.setOnMouseMoved(event -> {
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
      if (!event.isPrimaryButtonDown()) {
        player.releaseObject();
      }
    });

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

  private void addEntity(Entity entity) {
    entities.add(entity);
    root.getChildren().add(entity.getView());
  }

  public void start() {
    gameLoop.start();
  }

  public void update(double delta) {
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

    // --- GRAB CHECK ---
    // This part is correct and remains the same
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

    // --- MAIN COLLISION LOOP ---
    for (int i = 0; i < entities.size(); i++) {
      for (int j = i + 1; j < entities.size(); j++) {
        Entity e1 = entities.get(i);
        Entity e2 = entities.get(j);

        // --- THE FIX IS HERE ---
        // 1. Skip this pair if either entity is not physical
        if (!e1.isPhysical || !e2.isPhysical) {
          continue;
        }

        // 2. Skip collision between the player and the object they are holding
        if (player.isHoldingObject() &&
            ((e1 == player && e2 == player.getHeldObject()) || (e2 == player && e1 == player.getHeldObject()))) {
          continue;
        }

        Physics.CollisionResult result = Physics.checkCollision(e1, e2);

        if (result.isColliding) {
          // The rest of your collision response logic is correct.
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
      return; // Avoid division by zero

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

  public Scene getScene() {
    return this.scene;
  }
}
