package org.marble.ui;

import java.net.MalformedURLException;
import java.net.URL;

import de.lessvoid.nifty.controls.TextField;

import org.marble.Game;

public class LevelPackScreen extends AbstractScreenController {

    public LevelPackScreen(final Game game) {
        super(game);
    }

    @Override
    public void onGoto() {
        screen.findNiftyControl("level-pack-url", TextField.class).setText(
                game.getCurrentLevelPackURL().toExternalForm());
    }

    public void loadLevelPack() {
        try {
            game.loadLevelPack(new URL(screen.findNiftyControl(
                    "level-pack-url", TextField.class).getText()));
        } catch (final MalformedURLException e) {
            game.handleError("Could not load level pack: invalid URL", e);
        }
    }

    public void goBack() {
        game.gotoScreen(UIScreen.Start);
    }
}
