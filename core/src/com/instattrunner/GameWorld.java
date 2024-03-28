package com.instattrunner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpawnEllipseSide;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.instattrunner.bodies.Buff;
import com.instattrunner.bodies.Debuff;
import com.instattrunner.bodies.Floor;
import com.instattrunner.bodies.Obstacle;
import com.instattrunner.bodies.Player;
import com.instattrunner.controller.KeyboardController;
import com.instattrunner.loader.ConstHub;
import com.instattrunner.loader.GameAssetManager;
import com.instattrunner.views.GameScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

// Controls all logic in game
public class GameWorld {
    private GameScreen parent;
    public World world;
    private KeyboardController controller;
    public GameAssetManager assMan;
    public ConstHub locCHub;

    // Random generator
    public Random random = new Random(TimeUtils.millis());

    // Bodies (yes bodies, just not human bodies, although we have a player BODY)
    public Body player;
    public Body regularPlayer;
    public Body smallPlayer;    // Pre-build as very computationaly intensive, tends to crash game when done during run time
    public Body bigPlayer;
    public Body floor;
    public Array<Body> obstacles = new Array<Body>();
    public Array<Body> buffs = new Array<Body>();
    public Array<Body> debuffs = new Array<Body>();
    public Body collideDeBuff;
    public Body collideObstacle;

    // Declare different type of Body objects in order to access their methods
    private Floor floorClass;
    private Player playerClass;
    public Obstacle obstacleClass;
    public Buff buffClass;
    public Debuff debuffClass;

    // Declare different support classes to access their methods
    private SpawnNTrack spawnNTrackClass;
    private BuffDebuffEffects buffDebuffEffectsClass;
    
    // Timestamps and spawnInterval
    public long minSpawnInterval = 1200;    // Determines how many milli second has to pass to spawn new obstacle/buff/debuff
    public long obstacleSpawnInterval = minSpawnInterval;    //Obstacle and buff/debuff set to min and four times of min during init
    public long buffDebuffSpawnInterval = minSpawnInterval * 4;    //Changed to random within range everytime new obstacle/buff/debuff spawn
    public long obstacleTimestamp = TimeUtils.millis() - 1200;    // Time since last obstacle spawn
    public long buffDebuffTimestamp = TimeUtils.millis() - 1200;    // Time since last buff/debuff spawn
    

    // Vars for environment
    public boolean isDead = false;
    public int score = 0;
    public int velocityIncrement = 0;
    private int highscore = ScoreManager.loadTextFile();

    




    // ArrayList for spawn randomization
    private ArrayList<Integer> buffSpawnUnused = new ArrayList<>(Arrays.asList(0,1,2,3));
    private ArrayList<Integer> buffSpawnUsed = new ArrayList<>();
    private ArrayList<Integer> debuffSpawnUnused = new ArrayList<>(Arrays.asList(0,1,2));
    private ArrayList<Integer> debuffSpawnUsed = new ArrayList<>();

    // enum for jump
    public int NORMAL = 115;
    public int HIGH = 135;
    public int LOW = 73;

    // tweak speed of obstacles
    /* Sports: obstacles move faster; Biz: obstacles move slower; Otherwise: regular */
    public float regular = -20f;
    public float fast = -40f;
    public float slow = -5f;


    // Contructor
    // world to keep all physical objects in the game
    public GameWorld(KeyboardController cont, GameAssetManager assetMan, GameScreen gameScreen) {
        System.out.println("New Model Created.");

        controller = cont;
        parent = gameScreen;
        assMan = assetMan;
        locCHub = parent.locCHub;
        world = new World(new Vector2(0, -60f), true);
        world.setContactListener(new CollisionListener(this));

        // Init different type of Body classes
        floorClass = new Floor(this);
        playerClass = new Player(this);
        obstacleClass = new Obstacle(this);
        buffClass = new Buff(this);
        debuffClass = new Debuff(this);

        spawnNTrackClass = new SpawnNTrack(this);

        collideDeBuff = null;
        
        // Create floor and player of game 
        floorClass.createFloor();
        playerClass.createPlayer();
    }


    // todo ensure player cannot jump outside of view
    // logic method to run logic part of the model
    public void logicStep(float delta) {
        // Run spawnLogic to spawn obstacle/buff/debuff if conditions met
        spawnLogic();

        // Call tracking method to check whether obstacle/buff/debuff are out of screen
        // If true, remove (obstacle will also increment score by 1 
        spawnNTrackClass.trackObstacles();
        spawnNTrackClass.trackBuffsDebuffs();

        // Check if exists collided buff/debuff, if true, remove
        if (collideDeBuff != null)
            removeCollidedDeBuff();

        jumplogic;
        
        endGameLogic();

        world.step(delta, 3, 3); // tell Box2D world to move forward in time
    }


    private void spawnLogic() {
        // Spawn obstacle based on speed var determiner 
        if(TimeUtils.timeSinceMillis(obstacleTimestamp) > obstacleSpawnInterval) 
            spawnNTrackClass.spawnObstacles(gameWorld.regular);

        // Randomly choose to spawn buff or debuff  
        // Type of buff/debuff will be randomly choosen by .create method in GameWorld
        int choice = random.nextInt(2);
        if (TimeUtils.timeSinceMillis(buffDebuffTimestamp) > buffDebuffSpawnInterval){
            if (choice == 0) 
                spawnNTrackClass.spawnBuffs();
            else if (choice == 1)
                spawnNTrackClass.spawnDebuffs();
        }
    }


    private void endGameLogic() {
        if (isDead) {
            if (buffDebuffEffectsClass.immunity) {
                removeCollidedObstacle();
                buffDebuffEffectsClass.resetImmune();
            }

            else {
                if (highscore < score) {
                    highscore = score;
                    System.out.print("new high score obtain  :  ");
                    System.out.println(highscore);
                    ScoreManager.updateHighScore(highscore);
                }
                parent.parent.finalScore = score;
                parent.parent.changeScreen(ScreenManager.END);
            }

            isDead = false;
        }
    }


    public void passThrough(Body bod) {
        for (Fixture fix : bod.getFixtureList()) {
            fix.setSensor(true);
        }
    }
 

    public void removeCollidedObstacle(){
        collideObstacle.setTransform(-27f, collideObstacle.getPosition().y, collideObstacle.getAngle());
    }


    public void removeCollidedDeBuff(){
        collideDeBuff.setTransform(-27f, collideDeBuff.getPosition().y, collideDeBuff.getAngle());
        collideDeBuff = null;
    }
}
