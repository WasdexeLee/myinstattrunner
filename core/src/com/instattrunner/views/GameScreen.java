package com.instattrunner.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Array;
import com.instattrunner.BodyData;
import com.instattrunner.GameWorld;
import com.instattrunner.ScoreManager;
import com.instattrunner.ScreenManager;
import com.instattrunner.controller.KeyboardController;
import com.instattrunner.loader.ConstHub;


// Screen which shows the game play
public class GameScreen implements Screen {
    // ScreenManager as Container
    public ScreenManager container;
    private OrthographicCamera cam;
    private KeyboardController controller;
    private GameWorld gameWorld;
    private SpriteBatch sb;
    private BitmapFont font;

    private Box2DDebugRenderer debugRenderer;
    private boolean debug = false; // tweak if want to debug
    
    // Declare Texture var for all Body in game
    private final Texture floorTex;
    private final Texture playerTex;
    // private final Texture bgTex;
    private Array<Texture> obTexs = new Array<Texture>();
    private Array<Texture> buffTexs = new Array<Texture>();
    private Array<Texture> debuffTexs = new Array<Texture>();

    // Declare array to store width and height of different player, obstacle, buff and debuff
    private final Vector2 floorWidHei;
    private final Vector2 playerWidHei;
    private final Vector2[] obstacleWidHei;
    private final Vector2[] buffWidHei;
    private final Vector2[] debuffWidHei; 

    // Scale of category of body
    private final float obstacleScale;
    private final float buffScale;
    private final float debuffScale;

    // Store high score value
    private int highscore;


    public GameScreen(ScreenManager screenManager) {
        container = screenManager;

        cam = new OrthographicCamera(32, 24);
        debugRenderer = new Box2DDebugRenderer(true, true, true, true,true, true);

        sb = new SpriteBatch();
        sb.setProjectionMatrix(cam.combined);

        controller = new KeyboardController();

        gameWorld = new GameWorld(controller, container.assMan, this);
    
        container.assMan.queueAddImages();
        container.assMan.manager.finishLoading();

        // Gets images as Texture from asset manager (indivdual Texture for player and background)
        // Load images as Texture into array of Texture (obstacle, buff, debuff as there are multiple options)
        floorTex = container.assMan.manager.get(ConstHub.floorImageName);
        playerTex = container.assMan.manager.get(ConstHub.playerImageName);
        for (String obstacleImage : ConstHub.obstacleImagesName)
            obTexs.add(container.assMan.manager.get(obstacleImage));
        for (String buffImage : ConstHub.buffImagesName)
            buffTexs.add(container.assMan.manager.get(buffImage));
        for (String debuffImage : ConstHub.debuffImagesName)
            debuffTexs.add(container.assMan.manager.get(debuffImage));

        // Load width and heigth of player, obstacle, buff and debuff
        floorWidHei = ConstHub.floorWidHei;
        playerWidHei = ConstHub.playerWidHei;
        obstacleWidHei = ConstHub.obstacleWidHei;
        buffWidHei = ConstHub.buffWidHei;
        debuffWidHei = ConstHub.debuffWidHei;

        // Load scale of category of Body
        obstacleScale = ConstHub.obstacleScale;
        buffScale = ConstHub.buffScale;
        debuffScale = ConstHub.debuffScale;

        font = new BitmapFont(Gdx.files.internal("skin/score.fnt"));
        highscore = ScoreManager.loadTextFile();
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(controller);
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(135/255f, 206/255f, 235/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameWorld.logicStep(delta); // move game logic forward; use if to pause game

        if (debug) debugRenderer.render(gameWorld.world, cam.combined);

        /* START DRAWING */
        sb.begin();
 
        // Draw all objects
        // Draw player 
        sb.draw(playerTex, gameWorld.player.getPosition().x, gameWorld.player.getPosition().y, playerWidHei.x * gameWorld.renderPlayerScale, playerWidHei.y * gameWorld.renderPlayerScale);
        // Draw floor
        sb.draw(floorTex, gameWorld.floor.getPosition().x - (floorWidHei.x / 2), gameWorld.floor.getPosition().y - (floorWidHei.y / 2), floorWidHei.x, floorWidHei.y);
        // Draw all obstacles, buffs, debuffs
        loopDraw(gameWorld.obstacles, obTexs, obstacleWidHei, obstacleScale);
        loopDraw(gameWorld.buffs, buffTexs, buffWidHei, buffScale);
        loopDraw(gameWorld.debuffs, debuffTexs, debuffWidHei, debuffScale);


        float tempLocBuff = gameWorld.floor.getPosition().x - (floorWidHei.x / 2) + 0.5f;
        float tempLocDebuff = gameWorld.floor.getPosition().x - (floorWidHei.x / 2) + 0.5f;
        for (int i = 0; i < 4; i++){
            if (gameWorld.buffDebuffEffectsClass.buffActive[i]){
                if (buffWidHei[i].x > buffWidHei[i].y)
                    sb.draw(buffTexs.get(i), tempLocBuff, 9.8f, 1.5f, 1.5f / buffWidHei[i].x * buffWidHei[i].y);
                else
                    sb.draw(buffTexs.get(i), tempLocBuff, 9.5f, 2 / buffWidHei[i].y * buffWidHei[i].x, 2);
                tempLocBuff += 2f;
            }
            if (gameWorld.buffDebuffEffectsClass.debuffActive[i]){
                if (debuffWidHei[i].x > debuffWidHei[i].y)
                    sb.draw(debuffTexs.get(i), tempLocDebuff,7.1f, 1.5f, 1.5f / debuffWidHei[i].x * debuffWidHei[i].y);
                else
                    sb.draw(debuffTexs.get(i), tempLocDebuff, 7.1f, 2 / debuffWidHei[i].y * debuffWidHei[i].x, 2);
                tempLocDebuff += 2f;
            }

        }

        // Set the font size for the "Score" text
        font.getData().setScale(0.03f);
        // Draw the "Score" text
        font.draw(sb, "Score", 4, 11);

        // Set the font size for the "Score" text
        font.getData().setScale(0.03f);
        // Draw the "Score" text
        font.draw(sb, String.format("%04d", gameWorld.score), 5, 9);

        // Set the font size for the "HighScore" text
        font.getData().setScale(0.03f);
        font.draw(sb, "H1ghscore", -9,11);

        font.getData().setScale(0.03f);
        font.draw(sb, String.format("%04d", highscore), -7,9);

        // End sprite batch
        sb.end();
    }


    @Override
    public void resize(int width, int height) {
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
        playerTex.dispose();
        for (Texture obTex : obTexs)
            obTex.dispose();
        for (Texture buffTex : buffTexs)
            buffTex.dispose();
        for (Texture debuffTex : debuffTexs)
            debuffTex.dispose();
        sb.dispose();
        gameWorld.collisionListener.soundEffectClass.jump.dispose();
        gameWorld.collisionListener.soundEffectClass.collect.dispose();
    }


    // Just trying to reduce repeated code
    private void loopDraw(Array<Body> bodys, Array<Texture> bodyTexs, Vector2[] bodyWidHei, float bodyScale) {
        int tempTextureId;
        Vector2 tempWidHei;

        for (Body body : bodys) {
            // .getTextureId return texture id of particular model and use it as index on the texture array 
            // .getPosition returns bottom left coord as these are complex polygon (only floor .getPosition return center)
            tempTextureId = BodyData.getTextureId(body);
            tempWidHei = bodyWidHei[tempTextureId];
            sb.draw(bodyTexs.get(tempTextureId), body.getPosition().x, body.getPosition().y, tempWidHei.x* bodyScale, tempWidHei.y * bodyScale);
        }
   }
}