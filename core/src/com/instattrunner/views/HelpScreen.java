package com.instattrunner.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.instattrunner.ScreenManager;
import com.instattrunner.loader.ConstHub;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;


public class HelpScreen implements Screen {
    // ScreenManager as Container
    private ScreenManager container;

    // Create Stage to store ui elements and skin for button skins
    private Stage stage;
    private Skin skin;

    // Image of background
    private Image backgroundImage;

    // Table to store ui elements in it and then only pass the table to stage
    private Table table;

    // Boolean to determine whether to display detailed help page or not 
    private boolean detailPage = false;

    
    public HelpScreen(ScreenManager screenManager) {
        container = screenManager;

        OrthographicCamera gameCam  = new OrthographicCamera();
        stage = new Stage(new FitViewport(container.VIEW_WIDTH, container.VIEW_HEIGHT, gameCam));

        // Load skin using asset manager
        container.assMan.queueAddSkin();
        container.assMan.manager.finishLoading();
        skin = container.assMan.manager.get(ConstHub.skinName);

        // Create Image from backgroundTexture from ScreenManager
        backgroundImage = new Image(container.backgroundTexture);
    }


    @Override
    public void show() {
        // Set the background image
        backgroundImage.setFillParent(true);
        stage.clear();
        stage.addActor(backgroundImage);

        // Push input to stage
        Gdx.input.setInputProcessor(stage);

        // Add table (which holds buttons) to the stage
        table = new Table();
        table.setFillParent(true);
        // table.setDebug(true);

        // Check to see if boolean is true or false (boolean changed by clicking the label)
        // if false, then show not detail
        if (!detailPage){
            Label titleLabel = new Label("How to Play", skin, "big");

            Label i1 = new Label("Spacebar to jump", skin, "big");
            Label i2 = new Label("Once hit obstacle, die", skin, "big");

            Label b1 = new Label("Collect buffs and debuffs", skin, "big");
            Label b2 = new Label("which are represented by students of other majors", skin, "big");

            Label detail = new Label("See Buffs and Debuffs detailed description", skin,"boxed");

            // Go back to menu
            TextButton menu = new TextButton("Back to Menu", skin);

            // Listen for click on the Detailed Description prompt
            detail.addListener(new ClickListener() {
                @Override
                // When clicked, make detailPage boolean true(negation of previous state which is false)
                // Then call show() method which refreshes HelpScreen
                public void clicked(InputEvent event, float x, float y) {
                    detailPage = !detailPage;
                    show();
                }
            });

            menu.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    container.changeScreen(ScreenManager.MENU);
                }
            });

            // Add padding between buttons
            table.defaults().padBottom(20);

            table.add(titleLabel).colspan(3).padBottom(50).center();
            table.row();

            table.add(i1).colspan(3).padBottom(20).center();
            table.row();
            table.add(i2).colspan(3).padBottom(50).center();
            table.row();
            table.add(b1).colspan(3).padBottom(20).center(); // Use colspan to span across all columns
            table.row();
            table.add(b2).colspan(3).padBottom(20).center(); // Use colspan to span across all columns
            table.row();
            table.add(detail).colspan(3).padBottom(20).center(); // Use colspan to span across all columns
            table.row();
            table.add(menu).colspan(3).padTop(50).center();
        }

        // Else, if detail button is clicked, boolean true
        else {
            Label buffsLabel = new Label("Buffs:", skin, "big");
            Label debuffsLabel = new Label("Debuffs:", skin, "big");

            Label buff1 = new Label("Business Major Sprite : Player speed decreases for 10 seconds", skin);
            Label buff2 = new Label("Nutrition Major Sprite: Player size decreases for 10 seconds", skin);
            Label buff3 = new Label("Dean Sprite: Player is immune to 1 obstacle for 5 seconds", skin);
            Label buff4 = new Label("Coffee Icon: Player jumps higher for 10 seconds", skin);

            Label debuff1 = new Label("Sports Science Major Sprite : Player speed increases for 10 seconds", skin);
            Label debuff2 = new Label("Culinary Major Sprite : Player size increases for 10 seconds", skin);
            Label debuff3 = new Label("Beer Icon : Player jumps lower for 10 seconds", skin);

            Label detail = new Label("Back to How to Play", skin,"boxed");

            // Add padding between buttons
            table.add(buffsLabel).colspan(3).padBottom(20).center();
            table.row();
            table.add(buff1).colspan(3).padBottom(10).center();
            table.row();
            table.add(buff2).colspan(3).padBottom(10).center();
            table.row();
            table.add(buff3).colspan(3).padBottom(10).center();
            table.row();
            table.add(buff4).colspan(3).padBottom(50).center();
            table.row();

            table.add(debuffsLabel).colspan(3).padBottom(20).center();
            table.row();
            table.add(debuff1).colspan(3).padBottom(10).center();
            table.row();
            table.add(debuff2).colspan(3).padBottom(10).center();
            table.row();
            table.add(debuff3).colspan(3).padBottom(10).center();
            table.row();
            table.add(detail).colspan(3).padBottom(20).center(); // Use colspan to span across all columns

            // Listen for click on the Less Detail prompt
            detail.addListener(new ClickListener() {
                @Override
                // When clicked, make detailPage boolean false(negation of previous state which is true)
                // Then call show() method which refreshes HelpScreen
                public void clicked(InputEvent event, float x, float y) {
                    detailPage = !detailPage;
                    show();
                }
            });
        }

       stage.addActor(table);
    }


    @Override
    public void render(float delta) {
        // Clear screen before start drawing the next screen
        Gdx.gl.glClearColor(135/255f, 206/255f, 235/255f, 1);
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
        skin.dispose();
    }
}