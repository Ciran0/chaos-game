package com.chaosgame.entity;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.Set;

public class Player extends Entity {

  // --- New Physics Constants ---
  private static final double ACCELERATION = 800; // How fast the player speeds up
  private static final double MAX_SPEED = 350; // The player's top speed
  private static final double DAMPING = 0.96; // Friction for the player (closer to 1 = less friction)
  private static final double DASH_IMPULSE = 1500; // An instant velocity boost

  // --- Dash State ---
  private static final double DASH_COOLDOWN = 1.0;
  private double dashCooldownTimer = 0;

  // --- Input state ---
  private double inputAx = 0; // Acceleration from input on X axis
  private double inputAy = 0; // Acceleration from input on Y axis

  public Player(int x, int y) {
    super(new Circle(15, Color.WHITE), 10.0, 15.0);
    this.x = x;
    this.y = y;
  }

  public void handleInput(Set<KeyCode> keys) {
    // --- Dashing ---
    if (keys.contains(KeyCode.SPACE) && dashCooldownTimer <= 0) {
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

  @Override
  public void update(double delta) {
    // --- Timers ---
    if (dashCooldownTimer > 0) {
      dashCooldownTimer -= delta;
    }

    // --- Physics ---
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

    // Call the parent update method to apply final velocity to position
    super.update(delta);
  }
}
