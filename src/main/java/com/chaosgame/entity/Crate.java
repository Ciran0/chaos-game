package com.chaosgame.entity;

import com.chaosgame.Vector2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Crate extends Entity {

  public Crate(int x, int y) {
    super(createCenteredRect(), 50.0, createBoxVertices(40, 40));
    this.x = x;
    this.y = y;
  }

  private static Rectangle createCenteredRect() {
    Rectangle rect = new Rectangle(40, 40, Color.SADDLEBROWN);
    rect.setX(-20);
    rect.setY(-20);
    return rect;
  }

  // A helper to define the vertices for a box shape
  private static Vector2D[] createBoxVertices(double width, double height) {
    double halfWidth = width / 2;
    double halfHeight = height / 2;
    return new Vector2D[] {
        new Vector2D(-halfWidth, -halfHeight), // Top-left
        new Vector2D(halfWidth, -halfHeight), // Top-right
        new Vector2D(halfWidth, halfHeight), // Bottom-right
        new Vector2D(-halfWidth, halfHeight) // Bottom-left
    };
  }
}
