// src/main/java/com/chaosgame/physics/PhysicsEngine.java
package com.chaosgame.physics;

import com.chaosgame.entity.Entity;
import java.util.List;

public class PhysicsEngine {

  private final CollisionResolver collisionResolver;

  public PhysicsEngine() {
    this.collisionResolver = new CollisionResolver();
  }

  public void update(List<Entity> entities) {
    // We run the collision check multiple times (iterations) for stability
    final int iterations = 2;
    for (int i = 0; i < iterations; i++) {
      checkForCollisions(entities);
    }
  }

  private void checkForCollisions(List<Entity> entities) {
    for (int i = 0; i < entities.size(); i++) {
      for (int j = i + 1; j < entities.size(); j++) {
        Entity e1 = entities.get(i);
        Entity e2 = entities.get(j);

        if (!e1.isPhysical || !e2.isPhysical) {
          continue;
        }

        CollisionDetector.CollisionResult result = CollisionDetector.checkCollision(e1, e2);

        if (result.isColliding) {
          collisionResolver.resolvePosition(e1, e2, result.mtv);
          collisionResolver.resolveVelocity(e1, e2);
        }
      }
    }
  }
}
