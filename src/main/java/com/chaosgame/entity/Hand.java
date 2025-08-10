package com.chaosgame.entity;

import com.chaosgame.Vector2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Hand extends Entity {

  public Hand() {
    // A simple 10x10 square for the hand
    super(new Rectangle(10, 10, Color.LIGHTGRAY), 1.0, createBoxVertices(10, 10));
    Rectangle rect = (Rectangle) this.view;
    rect.setX(-5);
    rect.setY(-5);

    this.isPhysical = false;
  }

  public void setGrabbing(boolean isGrabbing) {
    Rectangle rect = (Rectangle) this.view;
    if (isGrabbing) {
      rect.setFill(Color.LIGHTGREEN); // The color when trying to grab
    } else {
      rect.setFill(Color.LIGHTGRAY); // The default color
    }
  }

  // Helper to define the vertices for the hand's collision shape
  private static Vector2D[] createBoxVertices(double width, double height) {
    double halfWidth = width / 2;
    double halfHeight = height / 2;
    return new Vector2D[] {
        new Vector2D(-halfWidth, -halfHeight), new Vector2D(halfWidth, -halfHeight),
        new Vector2D(halfWidth, halfHeight), new Vector2D(-halfWidth, halfHeight)
    };
  }
}
