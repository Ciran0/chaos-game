package com.chaosgame;

import com.chaosgame.entity.Entity;

public class Physics {

  /**
   * A helper class to store the result of a collision check.
   */
  public static class CollisionResult {
    public boolean isColliding;
    public Vector2D mtv; // Minimum Translation Vector

    public CollisionResult(boolean isColliding, Vector2D mtv) {
      this.isColliding = isColliding;
      this.mtv = mtv;
    }
  }

  /**
   * A helper class to store the min/max projection of a shape on an axis.
   */
  private static class Projection {
    double min, max;

    public Projection(double min, double max) {
      this.min = min;
      this.max = max;
    }

    public boolean overlaps(Projection other) {
      return this.max > other.min && other.max > this.min;
    }

    public double getOverlap(Projection other) {
      return Math.min(this.max, other.max) - Math.max(this.min, other.min);
    }
  }

  /**
   * Checks for collision between two entities using the Separating Axis Theorem
   * (SAT).
   * 
   * @return A CollisionResult containing whether they collide and the MTV.
   */
  public static CollisionResult checkCollision(Entity a, Entity b) {
    double minOverlap = Double.MAX_VALUE;
    Vector2D smallestAxis = null;

    // Get all axes to test (the normals of the edges of both shapes)
    Vector2D[] axes = getAxes(a, b);

    for (Vector2D axis : axes) {
      // Project both shapes onto the axis
      Projection p1 = project(a, axis);
      Projection p2 = project(b, axis);

      // If there is a gap between the projections, they are not colliding
      if (!p1.overlaps(p2)) {
        return new CollisionResult(false, null); // No collision
      } else {
        // There is an overlap, find out how much
        double overlap = p1.getOverlap(p2);

        // Check if this is the smallest overlap we've found so far
        if (overlap < minOverlap) {
          minOverlap = overlap;
          smallestAxis = axis;
        }
      }
    }

    // If we get here, all projections overlapped, so the shapes are colliding.
    // The Minimum Translation Vector (MTV) is the smallest axis scaled by the
    // minimum overlap.
    Vector2D mtv = smallestAxis.scale(minOverlap);

    // Make sure the MTV is pointing in the right direction to push 'a' away from
    // 'b'
    Vector2D centerA = new Vector2D(a.getX(), a.getY());
    Vector2D centerB = new Vector2D(b.getX(), b.getY());
    Vector2D direction = centerB.subtract(centerA);
    if (direction.dot(mtv) < 0) {
      mtv = mtv.scale(-1);
    }

    return new CollisionResult(true, mtv);
  }

  // Helper to get all unique axes (normals) from two entities
  private static Vector2D[] getAxes(Entity a, Entity b) {
    Vector2D[] axesA = getEntityAxes(a);
    Vector2D[] axesB = getEntityAxes(b);
    Vector2D[] axes = new Vector2D[axesA.length + axesB.length];
    System.arraycopy(axesA, 0, axes, 0, axesA.length);
    System.arraycopy(axesB, 0, axes, axesA.length, axesB.length);
    return axes;
  }

  // Helper to get the axes for a single entity
  private static Vector2D[] getEntityAxes(Entity entity) {
    Vector2D[] vertices = entity.getVertices();
    Vector2D[] axes = new Vector2D[vertices.length];
    for (int i = 0; i < vertices.length; i++) {
      Vector2D p1 = vertices[i];
      Vector2D p2 = vertices[i + 1 == vertices.length ? 0 : i + 1];
      Vector2D edge = p1.subtract(p2);
      axes[i] = edge.normal().normalize();
    }
    return axes;
  }

  // Helper to project an entity's vertices onto an axis
  private static Projection project(Entity entity, Vector2D axis) {
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;

    // Get the entity's vertices in world space
    Vector2D[] vertices = entity.getVertices();
    Vector2D entityPos = new Vector2D(entity.getX(), entity.getY());

    for (Vector2D vertex : vertices) {
      // Position the vertex in the world, then project it
      double p = entityPos.add(vertex).dot(axis);
      min = Math.min(min, p);
      max = Math.max(max, p);
    }
    return new Projection(min, max);
  }
}
