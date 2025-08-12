package com.chaosgame;

// A simple class to represent 2D vectors, to make physics code cleaner.
public class Vector2D {
  public double x, y;

  public Vector2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public String toString() {
    return ("[" + this.x + ", " + this.y + "]");
  }

  public Vector2D add(Vector2D other) {
    return new Vector2D(this.x + other.x, this.y + other.y);
  }

  public Vector2D subtract(Vector2D other) {
    return new Vector2D(this.x - other.x, this.y - other.y);
  }

  public Vector2D scale(double scalar) {
    return new Vector2D(this.x * scalar, this.y * scalar);
  }

  public Vector2D normalize() {
    double mag = magnitude();
    if (mag == 0) {
      return new Vector2D(0, 0);
    }
    return new Vector2D(x / mag, y / mag);
  }

  public double dot(Vector2D other) {
    return this.x * other.x + this.y * other.y;
  }

  public double magnitude() {
    return Math.sqrt(x * x + y * y);
  }

  // Returns a vector perpendicular to this one
  public Vector2D normal() {
    return new Vector2D(-y, x);
  }
}
