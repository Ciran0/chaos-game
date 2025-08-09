package com.chaosgame.entity;

import javafx.scene.Node;

/**
 * The base class for all game objects (Player, Crates, Guards, etc.).
 * It handles position, velocity, and the visual representation.
 */
public abstract class Entity {
  // Game state
  protected double x, y; // Position
  protected double vx, vy; // Velocity

  private static final double GLOBAL_FRICTION = 0.98;

  public double mass;
  public double radius;

  // Visual representation
  protected Node view;

  public Entity(Node view, double mass, double radius) {
    this.view = view;
    this.mass = mass;
    this.radius = radius;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getVx() {
    return vx;
  }

  public double getVy() {
    return vy;
  }

  // SETTERS - To allow other classes to WRITE these values
  public void setVx(double vx) {
    this.vx = vx;
  }

  public void setVy(double vy) {
    this.vy = vy;
  }

  // This method will be called on every frame by the game loop
  public void update(double delta) {

    if (!(this instanceof Player)) {
      vx *= GLOBAL_FRICTION;
      vy *= GLOBAL_FRICTION;
    }

    // Move the entity based on its velocity and time passed
    x += vx * delta;
    y += vy * delta;

    // Update the visual node's position
    view.setTranslateX(x);
    view.setTranslateY(y);
  }

  public Node getView() {
    return view;
  }
}
