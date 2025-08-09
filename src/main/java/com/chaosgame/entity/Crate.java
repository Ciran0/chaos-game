package com.chaosgame.entity;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Crate extends Entity {
  public Crate(int x, int y) {
    // A 40x40 brown square. Its collision radius is half its diagonal.
    super(createCenteredRect(),
        50.0, // mass = 50 (heavier than player)
        28.0); // radius ~= sqrt(20*20 + 20*20)
    this.x = x;
    this.y = y;
  }

  /**
   * A private static helper method to create and configure the Crate's visual
   * shape.
   * This allows us to prepare the Rectangle before calling super().
   * 
   * @return A centered Rectangle node.
   */
  private static Rectangle createCenteredRect() {
    // Create a 40x40 rectangle
    Rectangle rect = new Rectangle(40, 40, Color.SADDLEBROWN);
    // Shift its drawing position back so its center is at (0,0)
    rect.setX(-20); // -width / 2
    rect.setY(-20); // -height / 2
    return rect;
  }
}
