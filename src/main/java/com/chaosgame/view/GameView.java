package com.chaosgame.view;

import com.chaosgame.ViewManager;
import com.chaosgame.entity.Entity;
import com.chaosgame.entity.Player;
import com.chaosgame.entity.Crate;
import com.chaosgame.Physics;

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

    this.player = new Player(WIDTH / 2, HEIGHT / 2);
    addEntity(player);

    addEntity(new Crate(200, 200));
    addEntity(new Crate(1000, 500));
    addEntity(new Crate(400, 600));

    scene.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
    scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));

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

    for (Entity entity : entities) {
      entity.update(delta);
    }

    checkCollisions();
  }

  private void checkCollisions() {
    for (int i = 0; i < entities.size(); i++) {
      for (int j = i + 1; j < entities.size(); j++) {
        Entity e1 = entities.get(i);
        Entity e2 = entities.get(j);

        double dx = e2.getX() - e1.getX();
        double dy = e2.getY() - e1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < e1.radius + e2.radius) {
          // --- POSITIONAL CORRECTION ---
          // 1. Calculate how much they overlap
          double overlap = (e1.radius + e2.radius) - distance;

          // 2. Push them apart based on their mass
          double totalMass = e1.mass + e2.mass;
          double pushX = (dx / distance) * overlap;
          double pushY = (dy / distance) * overlap;

          // Move e1 back
          e1.setX(e1.getX() - pushX * (e2.mass / totalMass));
          e1.setY(e1.getY() - pushX * (e2.mass / totalMass));
          // Move e2 back
          e2.setX(e2.getX() - pushX * (e1.mass / totalMass));
          e2.setY(e2.getY() - pushX * (e1.mass / totalMass));

          // --- VELOCITY RESPONSE ---
          // Collision detected! Now handle the physics
          Physics.handleCollision(e1, e2);
        }
      }
    }
  }

  public Scene getScene() {
    return this.scene;
  }
}
