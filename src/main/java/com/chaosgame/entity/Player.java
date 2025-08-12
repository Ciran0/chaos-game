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
  private Vector2D grabPoint; // The point on the object we grabbed, relative to its center
  private static final double SPRING_STIFFNESS = 2000; // How "strong" the grab is
  private static final double SPRING_DAMPING = 100; // Prevents the object from oscillating wildly

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
    // Calculate where on the object we grabbed it, relative to the object's center
    this.grabPoint = new Vector2D(hand.getX() - entity.getX(), hand.getY() - entity.getY());
  }

  public void releaseObject() {
    this.isGrabbing = false;
    hand.setGrabbing(false);
    if (heldObject != null) {
      heldObject = null;
      grabPoint = null;
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

  public void applyForceFromCenter(Vector2D force, double delta) {
    // This helper ignores torque for the player for simplicity.
    if (this.mass == 0)
      return;
    double ax = force.x / this.mass;
    double ay = force.y / this.mass;
    this.vx += ax * delta;
    this.vy += ay * delta;
  }

  @Override
  public void updatePhysics(double delta) {
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

    // --- Spring Grabbing Physics ---
    if (heldObject != null) {
      // 1. Calculate the target position for the grab point (which is the hand's
      // position)
      Vector2D handPosition = new Vector2D(hand.getX(), hand.getY());

      // 2. Calculate the current world position of the grab point on the object
      Vector2D currentGrabPointPosition = new Vector2D(heldObject.getX() + grabPoint.x,
          heldObject.getY() + grabPoint.y);

      // 3. Calculate the displacement vector (the "stretch" of the spring)
      Vector2D displacement = currentGrabPointPosition.subtract(handPosition);

      // 4. Calculate the spring force (Hooke's Law: F = -kx)
      Vector2D springForce = displacement.scale(-SPRING_STIFFNESS);

      // 5. Calculate the damping force (to reduce oscillation)
      Vector2D relativeVelocity = new Vector2D(heldObject.getVx() - this.vx, heldObject.getVy() - this.vy);
      Vector2D dampingForce = relativeVelocity.scale(-SPRING_DAMPING);

      // 6. Calculate the total force of the grab
      Vector2D totalForce = springForce.add(dampingForce);

      // 7. Apply the forces!
      // The object gets the full force.
      heldObject.applyForce(totalForce, grabPoint, delta);
      // The player gets the equal and opposite force (Newton's Third Law).
      heldObject.angularVelocity *= 0.82;
      this.applyForceFromCenter(totalForce.scale(-1), delta);
    }
  }

  public String toString() {
    return "Player";
  }
}
