package com.chaosgame.physics;

import com.chaosgame.Vector2D;

public class CollisionResult {
  public final boolean isColliding;
  public final Vector2D mtv; // Minimum Translation Vector

  public CollisionResult(boolean isColliding, Vector2D mtv) {
    this.isColliding = isColliding;
    this.mtv = mtv;
  }
}
