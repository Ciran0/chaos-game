package com.chaosgame.entity;

import com.chaosgame.Vector2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Crate extends Entity {

  // A constant to define the "density" of our crates.
  // This makes the mass calculation clear and easy to tweak.
  private static final double DENSITY = 0.02; // kg per pixel-squared, for example

  public Crate(int x, int y, double side) {
    // The super() call is now much easier to read.
    super(createCenteredRect(side), calculateMass(side), createBoxVertices(side, side));
    this.x = x;
    this.y = y;
  }

  /**
   * Calculates the mass of the crate based on its area (as a proxy for volume in
   * 2D)
   * and its density.
   */
  private static double calculateMass(double side) {
    double area = side * side;
    return area * DENSITY;
  }

  /**
   * Creates a Rectangle view for the crate, centered at its origin (0,0).
   * The parameter name is changed from 'mass' to 'side' to be more accurate.
   */
  private static Rectangle createCenteredRect(double side) {
    Rectangle rect = new Rectangle(side, side, Color.SADDLEBROWN);
    rect.setX(-side / 2);
    rect.setY(-side / 2);
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
