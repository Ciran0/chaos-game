package com.chaosgame.entity;

import com.chaosgame.Vector2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.Set;

public class Player extends Entity {

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

  public Player(int x, int y) {
    super(new Circle(15, Color.WHITE), 10.0, createCircleVertices(15, 8));
    this.x = x;
    this.y = y;
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
      vx += inputAx * ACCELERATION * delta;
      vy += inputAy * ACCELERATION * delta;

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
  }
}
