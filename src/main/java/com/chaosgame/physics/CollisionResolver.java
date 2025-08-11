// src/main/java/com/chaosgame/physics/CollisionResolver.java
package com.chaosgame.physics;

import com.chaosgame.Vector2D;
import com.chaosgame.entity.Entity;
import com.chaosgame.entity.Wall;

public class CollisionResolver {

  private static final double RESTITUTION = 0.6; // Bounciness

  /**
   * Adjusts the positions of two colliding entities to no longer overlap.
   */
  public void resolvePosition(Entity e1, Entity e2, Vector2D mtv) {
    double totalMass = e1.mass + e2.mass;
    e1.setX(e1.getX() - mtv.x * (e2.mass / totalMass));
    e1.setY(e1.getY() - mtv.y * (e2.mass / totalMass));
    e2.setX(e2.getX() + mtv.x * (e1.mass / totalMass));
    e2.setY(e2.getY() + mtv.y * (e1.mass / totalMass));
  }

  /**
   * Adjusts the velocities of two colliding entities.
   */
  public void resolveVelocity(Entity a, Entity b) {
    // A restitution value of < 1 makes collisions "lossy" (less bouncy).
    // A value of 1 is a perfect bounce.
    final double restitution = 0.6;

    // --- Special case for wall collisions ---
    if (a instanceof Wall) {
      // 'a' is the wall, so we only modify 'b'
      // We reflect b's velocity and apply restitution
      b.setVx(b.getVx() * -restitution);
      b.setVy(b.getVy() * -restitution);
      return;
    } else if (b instanceof Wall) {
      // 'b' is the wall, so we only modify 'a'
      a.setVx(a.getVx() * -restitution);
      a.setVy(a.getVy() * -restitution);
      return;
    }

    // --- Default case for two movable objects ---
    double dx = b.getX() - a.getX();
    double dy = b.getY() - a.getY();
    double distance = Math.sqrt(dx * dx + dy * dy);
    if (distance == 0)
      return;

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
}
