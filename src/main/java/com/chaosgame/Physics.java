package com.chaosgame;

import com.chaosgame.entity.Entity;

public class Physics {

  /**
   * Handles the elastic collision between two entities.
   * This method calculates and updates their velocities based on their mass.
   */
  public static void handleCollision(Entity a, Entity b) {
    // Step 1: Find the vector between the centers of the circles
    double dx = b.getX() - a.getX();
    double dy = b.getY() - a.getY();
    double distance = Math.sqrt(dx * dx + dy * dy);

    // Calculate the amount of overlap
    double overlap = a.radius + b.radius - distance;

    // Step 2: Find the normalized collision normal vector (direction of impact)
    double normalX = dx / distance;
    double normalY = dy / distance;

    // Step 3: Project the velocities onto the normal vector
    double p1 = a.getVx() * normalX + a.getVy() * normalY;
    double p2 = b.getVx() * normalX + b.getVy() * normalY;

    // Step 4: Use the 1D elastic collision formula to find the new velocities along
    // the normal
    double newP1 = (p1 * (a.mass - b.mass) + 2 * b.mass * p2) / (a.mass + b.mass);
    double newP2 = (p1 * 2 * a.mass - p2 * (a.mass - b.mass)) / (a.mass + b.mass);

    // Step 5: Update the velocities
    a.setVx(a.getVx() + (newP1 - p1) * normalX);
    a.setVy(a.getVy() + (newP1 - p1) * normalY);
    b.setVx(b.getVx() + (newP2 - p2) * normalX);
    b.setVy(b.getVy() + (newP2 - p2) * normalY);
  }
}
