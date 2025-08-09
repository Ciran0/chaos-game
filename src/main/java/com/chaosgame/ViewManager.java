package com.chaosgame;

import com.chaosgame.view.GameView;
import com.chaosgame.view.MainMenuView;
import javafx.stage.Stage;

/**
 * Manages switching between different scenes (views) in the application.
 */
public class ViewManager {
  private Stage stage;

  public ViewManager(Stage stage) {
    this.stage = stage;
  }

  public void showMainMenu() {
    MainMenuView menuView = new MainMenuView(this);
    stage.setScene(menuView.getScene());
  }

  public void showGameView() {
    GameView gameView = new GameView(this);
    stage.setScene(gameView.getScene());
    gameView.start();
    // We will later need to start the game loop here
  }

  // We will add showLobbyView() here later
}
