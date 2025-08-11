package com.chaosgame.entity;

import com.chaosgame.Vector2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Wall extends Entity {

  public Wall(double x, double y, double width, double height) {
    // We pass a very large mass to simulate an immovable object.
    super(new Rectangle(width, height, Color.DARKSLATEGRAY), Double.MAX_VALUE, createBoxVertices(width, height));
    this.x = x;
    this.y = y;
    this.view.setTranslateX(this.x);
    this.view.setTranslateY(this.y);
  }

  // Walls should never move, so we override the update method to do nothing.
  @Override
  public void updatePhysics(double delta) {
  }

  public void updatePosition(double delta) {
  }

  // A helper to define the vertices for a box shape
  private static Vector2D[] createBoxVertices(double width, double height) {
    // We don't need to center the rectangle view because walls are placed at their
    // top-left corner.
    return new Vector2D[] {
        new Vector2D(0, 0), // Top-left
        new Vector2D(width, 0), // Top-right
        new Vector2D(width, height), // Bottom-right
        new Vector2D(0, height) // Bottom-left
    };
  }
}
