package com.chaosgame.entity;

import com.chaosgame.Vector2D;
import javafx.scene.Node;

public abstract class Entity {
  public double x, y; // Position
  protected double vx, vy; // Velocity

  private static final double GLOBAL_FRICTION = 0.98;

  public double angle = 0; // in radians
  protected double angularVelocity = 0; // in radians per second
  private static final double ROTATIONAL_FRICTION = 0.95;

  public double mass;
  protected Vector2D[] vertices;
  public boolean isPhysical;
  protected Node view;

  public Entity(Node view, double mass, Vector2D[] vertices) {
    this.view = view;
    this.mass = mass;
    this.vertices = vertices;
    this.isPhysical = true;
  }

  public void applyForce(Vector2D force, Vector2D pointOfApplication, double delta) {
    if (this.mass == 0)
      return; // Can't apply force to a massless object

    // --- Linear Force ---
    // F = ma -> a = F/m -> v_change = (F/m) * t
    double ax = force.x / this.mass;
    double ay = force.y / this.mass;
    this.vx += ax * delta;
    this.vy += ay * delta;

    // --- Rotational Force (Torque) ---
    // Torque = r x F (cross product of lever arm and force)
    double leverArmX = pointOfApplication.x;
    double leverArmY = pointOfApplication.y;
    double torque = leverArmX * force.y - leverArmY * force.x; // 2D cross product

    // For a simple box, moment of inertia is roughly (mass * (width^2 + height^2))
    // / 12
    // We'll approximate with a simple inertia value for now.
    double momentOfInertia = this.mass * 500; // This is a magic number, tweak for effect!
    double angularAcceleration = torque / momentOfInertia;
    this.angularVelocity += angularAcceleration * delta;
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
      angularVelocity *= ROTATIONAL_FRICTION; // Apply rotational friction
    }
    x += vx * delta;
    y += vy * delta;
    angle += angularVelocity * delta; // Update angle

    view.setTranslateX(x);
    view.setTranslateY(y);
    view.setRotate(Math.toDegrees(angle)); // Apply rotation to the view
  }

  public Node getView() {
    return view;
  }
}
