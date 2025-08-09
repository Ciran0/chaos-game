package com.chaosgame.view;

import com.chaosgame.ViewManager;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MainMenuView {
  private Scene scene;
  private ViewManager viewManager;

  public MainMenuView(ViewManager viewManager) {
    this.viewManager = viewManager;
    VBox root = new VBox(20); // VBox arranges nodes vertically with 20px spacing
    root.setAlignment(Pos.CENTER);
    root.setStyle("-fx-background-color: #282c34;");

    Label title = new Label("CHAOS GAME");
    title.setFont(new Font("Arial", 40));
    title.setStyle("-fx-text-fill: white;");

    Button startGameButton = new Button("Start Game");
    startGameButton.setOnAction(e -> viewManager.showGameView());

    root.getChildren().addAll(title, startGameButton);
    this.scene = new Scene(root, 800, 600);
  }

  public Scene getScene() {
    return this.scene;
  }
}
