package com.chaosgame.entity;

import com.chaosgame.Vector2D;
import com.chaosgame.Projection;
import com.chaosgame.physics.CollisionResult;
import javafx.scene.Node;

import java.util.List;
import java.util.ArrayList;

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

  public void updatePhysics(double delta) {
    if (!(this instanceof Player)) {
      vx *= GLOBAL_FRICTION;
      vy *= GLOBAL_FRICTION;
      angularVelocity *= ROTATIONAL_FRICTION; // Apply rotational friction
    }
  }

  public void updatePosition(double delta) {
    x += vx * delta;
    y += vy * delta;
    angle += angularVelocity * delta;

    view.setTranslateX(x);
    view.setTranslateY(y);
    view.setRotate(Math.toDegrees(angle));
  }

  public Node getView() {
    return view;
  }

  /**
   * Gathers the unique separating axes (edge normals) from this entity and
   * another.
   * 
   * @param other The other entity.
   * @return A list of normalized Vector2Ds representing the axes to test.
   */
  private List<Vector2D> getAxes(Entity other) {
    List<Vector2D> axes = new ArrayList<>();
    // Get axes from this entity
    for (int i = 0; i < this.vertices.length; i++) {
      Vector2D p1 = this.vertices[i];
      Vector2D p2 = this.vertices[i + 1 == this.vertices.length ? 0 : i + 1];
      Vector2D edge = p1.subtract(p2);
      axes.add(edge.normal().normalize());
    }
    // Get axes from the other entity
    for (int i = 0; i < other.vertices.length; i++) {
      Vector2D p1 = other.vertices[i];
      Vector2D p2 = other.vertices[i + 1 == other.vertices.length ? 0 : i + 1];
      Vector2D edge = p1.subtract(p2);
      axes.add(edge.normal().normalize());
    }
    return axes;
  }

  /**
   * Projects the entity's vertices onto a given axis to find the min/max
   * interval.
   * 
   * @param axis The normalized axis to project onto.
   * @return A Projection object containing the min and max scalar values.
   */
  private Projection project(Vector2D axis) {
    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;

    // We must transform local vertices to world space
    for (Vector2D localVertex : this.vertices) {
      // 1. Rotate the vertex by the entity's angle
      double rotatedX = localVertex.x * Math.cos(this.angle) - localVertex.y * Math.sin(this.angle);
      double rotatedY = localVertex.x * Math.sin(this.angle) + localVertex.y * Math.cos(this.angle);

      // 2. Translate the vertex by the entity's position to get the world coordinate
      Vector2D worldVertex = new Vector2D(rotatedX + this.x, rotatedY + this.y);

      // 3. Project the world vertex onto the axis
      double projection = worldVertex.dot(axis);

      // 4. Update min/max
      if (projection < min) {
        min = projection;
      }
      if (projection > max) {
        max = projection;
      }
    }
    return new Projection(min, max);
  }

  /**
   * Performs continuous collision detection (Swept SAT) with another entity.
   *
   * @param other The entity to check for collision against.
   * @param delta The time frame to check within (e.g., one frame duration).
   * @return The time of impact (a value from 0 to delta). If no collision occurs
   *         within the delta, it returns a value greater than delta. A value of 0
   *         indicates
   *         an existing overlap at the start of the frame.
   */
  public double collide(Entity other, double delta) {
    // 1. Get all potential separating axes
    List<Vector2D> axes = this.getAxes(other);

    // 2. Initialize the master Time of Impact (toi) window
    double t_enter = 0.0;
    double t_leave = delta;

    // 3. Loop through each axis
    for (Vector2D axis : axes) {
      // 4. Project both shapes onto the axis
      Projection p1 = this.project(axis);
      Projection p2 = other.project(axis);

      // 5. Calculate the relative velocity of the two entities on this axis
      Vector2D relativeVelocityVec = new Vector2D(this.vx - other.vx, this.vy - other.vy);
      // We ignore angular velocity for this simplified model, as it makes the problem
      // non-linear.
      // This is a common and effective simplification for many games.
      double relativeVelocity = relativeVelocityVec.dot(axis);

      // 6. Calculate the initial distance between the projections
      // The distance is the gap between the end of p1 and the start of p2
      double dist_enter = p2.min - p1.max; // Gap moving from p1 to p2
      double dist_leave = p2.max - p1.min; // Distance to fully pass p2

      // A small epsilon to avoid floating point issues
      final double epsilon = 1e-6;

      // If they are already overlapping or touching...
      if (dist_enter < epsilon) {
        // ...and moving apart on this axis...
        if (relativeVelocity > 0) {
          // ...then this is a separating contact. It's not a "future" collision.
          // We can treat the time of impact as infinite for this axis,
          // effectively letting another axis determine the true first TOI.
          // We continue the loop because this axis does not provide a valid
          // collision time, but it also doesn't provide a separation.
          continue;
        }
      }

      // --- 7. Calculate the time of entry and exit for this single axis ---
      double t_axis_enter, t_axis_leave;

      if (Math.abs(relativeVelocity) < 1e-6) { // Practically zero velocity
        // If they are not moving relative to each other, they will never collide on
        // this axis
        // unless they are already overlapping.
        if (dist_enter > 0) { // There is a gap, and it's not closing.
          return delta + 1; // No collision will ever occur.
        }
        // They are overlapping and not moving apart. The collision interval is the
        // whole frame.
        t_axis_enter = 0.0;
        t_axis_leave = delta;
      } else {
        // Time = Distance / Velocity
        t_axis_enter = dist_enter / relativeVelocity;
        t_axis_leave = dist_leave / relativeVelocity;
      }

      // Make sure t_axis_enter is the earlier time
      if (t_axis_enter > t_axis_leave) {
        double temp = t_axis_enter;
        t_axis_enter = t_axis_leave;
        t_axis_leave = temp;
      }

      // 8. Shrink the master time window
      t_enter = Math.max(t_enter, t_axis_enter);
      t_leave = Math.min(t_leave, t_axis_leave);

      // 9. Check for an early exit
      // If the collision window is invalid, there is a separating axis in time.
      if (t_enter > t_leave) {
        return delta + 1; // No collision possible
      }
    }

    // 10. If we got through all axes, t_enter is the time of first impact
    return t_enter;
  }

  /**
   * Checks for a collision (overlap) with another entity at the current instant.
   * If they are overlapping, it returns the Minimum Translation Vector (MTV)
   * required to push them apart.
   *
   * @param other The entity to check against.
   * @return A CollisionResult object containing the result.
   */
  public CollisionResult checkCollision(Entity other) {
    double minOverlap = Double.POSITIVE_INFINITY;
    Vector2D mtvAxis = null;

    List<Vector2D> axes = this.getAxes(other);

    for (Vector2D axis : axes) {
      // Project both shapes onto the current axis
      Projection p1 = this.project(axis);
      Projection p2 = other.project(axis);

      // Check for non-overlap on this axis
      // If p1.max < p2.min, or p2.max < p1.min, they are separated
      if (p1.max < p2.min || p2.max < p1.min) {
        // Found a separating axis, no collision is possible
        return new CollisionResult(false, null);
      }

      // If we're here, they overlap on this axis. Calculate how much.
      double overlap1 = p1.max - p2.min;
      double overlap2 = p2.max - p1.min;
      double currentOverlap = Math.min(overlap1, overlap2);

      // Check if this is the smallest overlap we've found so far
      if (currentOverlap < minOverlap) {
        minOverlap = currentOverlap;
        mtvAxis = axis;
      }
    }

    // If the loop completes without finding a separating axis, the objects are
    // colliding.
    // The MTV is the axis of minimum overlap scaled by the overlap amount.
    Vector2D mtv = mtvAxis.scale(minOverlap);

    // --- Direction Correction ---
    // The MTV should always point away from the first entity ('this').
    // We get a vector from the center of 'this' to the center of 'other'.
    Vector2D centerVector = new Vector2D(other.x - this.x, other.y - this.y);

    // If the MTV is pointing in the same general direction as the center vector,
    // it's pointing the wrong way (it should push 'other' away from 'this').
    if (centerVector.dot(mtv) < 0) {
      mtv = mtv.scale(-1);
    }

    return new CollisionResult(true, mtv);
  }
}
