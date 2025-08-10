package com.chaosgame.entity;

import com.chaosgame.Vector2D;
import com.chaosgame.entity.Hand;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.Set;

public class Player extends Entity {

  private Hand hand;
  private static final double HAND_ORBIT_RADIUS = 30;
  private boolean isGrabbing = false;
  private Entity heldObject = null;
  private double heldObjectOffsetX;
  private double heldObjectOffsetY;

  // --- New Physics Constants ---
  private static final double ACCELERATION = 2000; // How fast the player speeds up
  private static final double MAX_SPEED = 300; // The player's top speed
  private static final double DAMPING = 0.92; // Friction for the player (closer to 1 = less friction)
  private static final double DASH_IMPULSE = 1500; // An instant velocity boost

  // --- Dash State ---
  private static final double DASH_COOLDOWN = 1.0;
  private static final double DASH_DURATION = 0.15;
  private boolean isDashing = false;
  private double dashTimer = 0;
  private double dashCooldownTimer = 0;

  // --- Input state ---
  private double inputAx = 0; // Acceleration from input on X axis
  private double inputAy = 0; // Acceleration from input on Y axis

  public Player(int x, int y, Hand hand) {
    super(new Circle(15, Color.WHITE), 10.0, createCircleVertices(15, 8));
    this.x = x;
    this.y = y;
    this.hand = hand;
  }

  public Hand getHand() {
    return this.hand;
  }

  public boolean isGrabbing() {
    return isGrabbing;
  }

  public boolean isHoldingObject() {
    return heldObject != null;
  }

  public Entity getHeldObject() {
    return this.heldObject;
  }

  public void startGrabbing() {
    isGrabbing = true;
    hand.setGrabbing(true);
  }

  public void grabObject(Entity entity) {
    if (heldObject != null)
      return; // Already holding something

    this.heldObject = entity;
    this.heldObjectOffsetX = entity.getX() - this.x;
    this.heldObjectOffsetY = entity.getY() - this.y;

    // --- MOMENTUM CONSERVATION ---
    // Calculate the combined momentum before the grab
    double momentumX = (this.mass * this.vx) + (entity.mass * entity.getVx());
    double momentumY = (this.mass * this.vy) + (entity.mass * entity.getVy());

    // Calculate the new total mass
    double totalMass = this.mass + entity.mass;

    // Calculate the new shared velocity
    double finalVx = momentumX / totalMass;
    double finalVy = momentumY / totalMass;

    // Apply the new velocity to both the player and the grabbed object
    this.setVx(finalVx);
    this.setVy(finalVy);
    entity.setVx(finalVx);
    entity.setVy(finalVy);
  }

  public void releaseObject() {
    this.isGrabbing = false;
    hand.setGrabbing(false);
    if (heldObject != null) {
      // The object keeps the shared velocity when released
      heldObject = null;
    }
  }

  public void updateHand(double mouseX, double mouseY) {
    double dx = mouseX - this.x;
    double dy = mouseY - this.y;
    double angle = Math.atan2(dy, dx);

    this.hand.setX(this.x + HAND_ORBIT_RADIUS * Math.cos(angle));
    this.hand.setY(this.y + HAND_ORBIT_RADIUS * Math.sin(angle));
  }

  private static Vector2D[] createCircleVertices(double radius, int sides) {
    Vector2D[] vertices = new Vector2D[sides];
    for (int i = 0; i < sides; i++) {
      double angle = 2 * Math.PI * i / sides;
      vertices[i] = new Vector2D(radius * Math.cos(angle), radius * Math.sin(angle));
    }
    return vertices;
  }

  public void handleInput(Set<KeyCode> keys) {
    // --- Dashing ---
    // We only allow a dash to start if we aren't already in one
    if (keys.contains(KeyCode.SPACE) && dashCooldownTimer <= 0 && !isDashing) {
      isDashing = true;
      dashTimer = DASH_DURATION;
      dashCooldownTimer = DASH_COOLDOWN;

      // Find the current movement direction, or default to up
      double directionX = vx;
      double directionY = vy;
      if (directionX == 0 && directionY == 0) {
        directionY = -1; // Dash "up" if standing still
      }

      // Normalize
      double length = Math.sqrt(directionX * directionX + directionY * directionY);
      directionX /= length;
      directionY /= length;

      // Apply dash impulse
      vx += directionX * DASH_IMPULSE;
      vy += directionY * DASH_IMPULSE;
    }

    // --- Movement ---
    // This method now just sets the INTENDED acceleration direction
    inputAx = 0;
    inputAy = 0;
    // We ignore WASD input while dashing
    if (!isDashing) {
      if (keys.contains(KeyCode.W))
        inputAy -= 1;
      if (keys.contains(KeyCode.S))
        inputAy += 1;
      if (keys.contains(KeyCode.A))
        inputAx -= 1;
      if (keys.contains(KeyCode.D))
        inputAx += 1;

      // Normalize acceleration vector if moving diagonally
      if (inputAx != 0 && inputAy != 0) {
        double length = Math.sqrt(inputAx * inputAx + inputAy * inputAy);
        inputAx = (inputAx / length);
        inputAy = (inputAy / length);
      }
    }
  }

  @Override
  public void update(double delta) {
    // --- Timers ---
    if (dashCooldownTimer > 0) {
      dashCooldownTimer -= delta;
    }
    if (dashTimer > 0) {
      dashTimer -= delta;
      if (dashTimer <= 0) {
        isDashing = false;
      }
    }

    // --- Physics ---
    // If we are NOT dashing, apply normal movement physics
    if (!isDashing) {
      // 1. Apply acceleration from input

      double currentMass = this.mass + (heldObject != null ? heldObject.mass : 0);
      double currentAcceleration = ACCELERATION * (this.mass / currentMass);

      vx += inputAx * currentAcceleration * delta;
      vy += inputAy * currentAcceleration * delta;

      // 2. Apply friction (damping)
      vx *= DAMPING;
      vy *= DAMPING;

      // 3. Cap max speed
      double currentSpeed = Math.sqrt(vx * vx + vy * vy);
      if (currentSpeed > MAX_SPEED) {
        vx = (vx / currentSpeed) * MAX_SPEED;
        vy = (vy / currentSpeed) * MAX_SPEED;
      }
    }

    // Call the parent update method to apply final velocity to position
    super.update(delta);

    if (heldObject != null) {
      // Make the held object share our velocity and position
      heldObject.setVx(this.vx);
      heldObject.setVy(this.vy);
      // This keeps the object "stuck" to the player visually
      heldObject.setX(this.x + heldObjectOffsetX);
      heldObject.setY(this.y + heldObjectOffsetY);
    }
  }
}
