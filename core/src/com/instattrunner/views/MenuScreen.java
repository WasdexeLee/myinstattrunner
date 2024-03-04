package com.instattrunner.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.instattrunner.InstattRunner;

// Initial menu screen
public class MenuScreen implements Screen {
    private InstattRunner parent;
    private Stage stage;
    private Skin skin;
    private Label titleLabel;

    // Constructor with reference to parent passed in
    public MenuScreen(InstattRunner instattRunner) {
        parent = instattRunner;
        stage = new Stage(new ScreenViewport()); // Stage is the controller to react to user input

        // load skin using asset manager
        parent.assetMan.queueAddSkin();
        parent.assetMan.manager.finishLoading();
        skin = parent.assetMan.manager.get("skin/comic-ui.json");

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        // Add table (which holds buttons) to the stage
        Table table = new Table();
        table.setFillParent(true);
        //table.setDebug(true);
        stage.addActor(table);

        // Create labels and buttons
        //skin = new Skin(Gdx.files.internal("skin/comic-ui.json"));
        titleLabel = new Label("Instatt Runner", skin);
        TextButton play = new TextButton("Start Game", skin);
        TextButton help = new TextButton("How to Play", skin);
        TextButton exit = new TextButton("Quit", skin);

        // Add buttons to table
        table.add(titleLabel).colspan(2);
        table.row().pad(10, 0, 10, 0);
        table.add(play).fillX().uniformX();
        table.row().pad(10, 0, 10, 0);
        table.add(help).fillX().uniformX();
        table.row().pad(10, 0, 10, 0);
        table.add(exit).fillX().uniformX();

        // Action for exit button
        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        // Action for help button
        help.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(InstattRunner.HELP);
            }
        });

        // Action for play button
        play.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(InstattRunner.PLAY);
            }
        });

    }

    @Override
    public void render(float delta) {
        // Clear screen before start drawing the next screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // recalculate viewport each time window is resized
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        // skin disposed via asset manager
    }
}
