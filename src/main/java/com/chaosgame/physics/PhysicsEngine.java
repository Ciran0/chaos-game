// src/main/java/com/chaosgame/physics/PhysicsEngine.java
package com.chaosgame.physics;

import com.chaosgame.entity.Entity;
import java.util.List;

public class PhysicsEngine {

  private final CollisionResolver collisionResolver;
  private static final int MAX_SUB_STEPS = 5; // Prevents infinite loops

  public PhysicsEngine() {
    this.collisionResolver = new CollisionResolver();
  }

  public void update(List<Entity> entities, double delta) {
    double remainingTime = delta;
    int subSteps = 0;

    System.out.println(String.format("--- NEW PHYSICS FRAME (delta=%.4f) ---", delta));

    while (remainingTime > 0 && subSteps < MAX_SUB_STEPS) {

      // --- Step 1: Find the earliest collision ---
      double earliestToi = remainingTime;
      Entity entityA = null;
      Entity entityB = null;

      for (int i = 0; i < entities.size(); i++) {
        for (int j = i + 1; j < entities.size(); j++) {
          Entity e1 = entities.get(i);
          Entity e2 = entities.get(j);

          // Skip checks for non-physical entities or two walls
          if (!e1.isPhysical || !e2.isPhysical) {// || (e1 instanceof Wall && e2 instanceof Wall)) {
            continue;
          }

          // Convert toi from being relative to the whole frame (0-1) to the remaining
          // time
          double toi = CollisionDetector.findTimeOfImpact(e1, e2, 1.0) * remainingTime;

          if (toi < earliestToi) {
            earliestToi = toi;
            entityA = e1;
            entityB = e2;
          }
        }
      }

      // --- Step 2: Move all entities forward by the calculated time ---
      double timeToSimulate = earliestToi;
      for (Entity entity : entities) {
        entity.updatePosition(timeToSimulate); // We'll create this new method
      }

      // --- Step 3: If a collision was found, resolve it ---
      if (entityA != null) {
        collisionResolver.resolveVelocity(entityA, entityB);
      }

      // --- Step 4: Reduce the remaining time ---
      System.out.println(
          String.format("Sub-step %d: remainingTime=%.4f, timeToSimulate=%.4f",
              subSteps, remainingTime, earliestToi));
      remainingTime -= timeToSimulate;
      subSteps++;
    }

    // Final discrete check for any lingering overlaps
    checkForCollisions(entities);
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
