package com.chaosgame.entity;

import com.chaosgame.Vector2D;
import javafx.scene.Node;

public abstract class Entity {
  public double x, y; // Position
  protected double vx, vy; // Velocity

  private static final double GLOBAL_FRICTION = 0.98;

  public double mass;
  // We no longer use a single radius. Instead, we have vertices.
  protected Vector2D[] vertices;

  protected Node view;

  public Entity(Node view, double mass, Vector2D[] vertices) {
    this.view = view;
    this.mass = mass;
    this.vertices = vertices;
  }

  // Getters and Setters
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

  public void setVx(double vx) {
    this.vx = vx;
  }

  public double getVy() {
    return vy;
  }

  public void setVy(double vy) {
    this.vy = vy;
  }

  public Vector2D[] getVertices() {
    return vertices;
  }

  public void update(double delta) {
    if (!(this instanceof Player)) {
      vx *= GLOBAL_FRICTION;
      vy *= GLOBAL_FRICTION;
    }
    x += vx * delta;
    y += vy * delta;
    view.setTranslateX(x);
    view.setTranslateY(y);
  }

  public Node getView() {
    return view;
  }
}
