package com.chaosgame;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The main entry point for the application.
 * This class is responsible for launching the JavaFX application
 * and setting up the primary window (Stage).
 */
public class App extends Application {

  @Override
  public void start(Stage primaryStage) {
    // The Stage is the main window of our application
    primaryStage.setTitle("Chaos Game");

    // We delegate the responsibility of showing views to the ViewManager
    ViewManager viewManager = new ViewManager(primaryStage);
    viewManager.showMainMenu(); // Start by showing the main menu

    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
