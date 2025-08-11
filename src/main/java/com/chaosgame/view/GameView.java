package com.chaosgame.view;

import com.chaosgame.ViewManager;
import com.chaosgame.entity.Crate;
import com.chaosgame.entity.Hand;
import com.chaosgame.entity.Player;
import com.chaosgame.entity.Wall;

/**
 * A concrete implementation of a playable level.
 * Its only job is to set up the specific entities for this level.
 */
public class GameView extends AbstractPlayableLevelView {

  public GameView(ViewManager viewManager) {
    super(viewManager);
  }

  @Override
  protected void setupLevel() {
    // Create the hand and player
    Hand hand = new Hand();
    this.player = new Player(WIDTH / 2, HEIGHT / 2, hand);

    // Add the player and hand to the level
    addEntity(this.player);
    addEntity(hand);

    // Add some crates for this specific level
    addEntity(new Crate(200, 200, 25));
    addEntity(new Crate(1000, 500, 50));
    addEntity(new Crate(400, 600, 100));

    addEntity(new Wall(0, 50, WIDTH * 2, 10));
    addEntity(new Wall(0, 500, WIDTH * 2, 250));
  }
}
