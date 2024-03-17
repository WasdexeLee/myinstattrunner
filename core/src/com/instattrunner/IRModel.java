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
import com.instattrunner.controller.KeyboardController;
import com.instattrunner.loader.IRAssetManager;
import com.instattrunner.views.MainScreen;
import com.instattrunner.BodyEditorLoader;


import com.instattrunner.BodyData;

import java.util.Iterator;

// Controls all logic in game
public class IRModel {
    public World world;
    private OrthographicCamera camera;
    private KeyboardController controller;
    private MainScreen main;
    private IRAssetManager assMan;

    // Bodies (yes bodies, just not human bodies, although we have a player BODY)
    public Body player;
    public Body floor;
    public Array<Body> obstacles = new Array<Body>();
    public Array<Body> buffs = new Array<Body>();
    public Array<Body> debuffs = new Array<Body>();

    // BodyEditorLoader for loading complex polygons to FixtureDef to Body
    // Declared here (only obstacle, buff, debuff) as repeatedly called and used (player is only used once, hence not here)
    private BodyEditorLoader playerLoader;
    private BodyEditorLoader obstacleLoader;
    private BodyEditorLoader buffLoader;
    private BodyEditorLoader debuffLoader;

    // Declare array to store name of images
    // Will be used by BodyEditorLoader to load different complex polygons to FixtureDef based on image name
    private final String playerImage;
    private final String[] obstacleImages;
    private final String[] buffImages;
    private final String[] debuffImages;

    // Declare width and height of floor for computation
    private Vector2 floorWidHei;

    // Scale of category of body
    private float playerScale;
    private float obstacleScale;
    private float buffScale;
    private float debuffScale;

    // Declare object Sound to store sound loaded from asset manager
    private Sound jump;
    private Sound collect;

    // enum for sound 
    public static final int JUMP_SOUND = 0;
    public static final int COLLECT_SOUND = 1;

    // Vars for environment
    public long obstacleTime;    // Time since last obstacle spawn
    public long buffTime;    // Time since last buff/debuff spawn
    public boolean isDead = false;
    public boolean immunity = false;
    public int score = 0;


    // Enum for obstacle, buff, debuff
    // obstacle
    private static final int SPEED = IRAssetManager.BUSINESS_MAN_1_AI;
    private static final int SIZE = IRAssetManager.NUTRITION_MAJOR;
    private static final int JUMP = IRAssetManager.COFFEE;
    private static final int IMMUNE = IRAssetManager.DEAN;
    // buff
    private static final int BUSINESS_MAN_1_AI = IRAssetManager.BUSINESS_MAN_1_AI;
    private static final int NUTRITION_MAJOR = IRAssetManager.NUTRITION_MAJOR;
    private static final int COFFEE = IRAssetManager.COFFEE;
    private static final int DEAN = IRAssetManager.DEAN;
    // debuff
    private static final int SPORTS_SCIENCE_MAJOR = IRAssetManager.SPORTS_SCIENCE_MAJOR;
    private static final int CULINARY_MAJOR = IRAssetManager.CULINARY_MAJOR;
    private static final int BEER = IRAssetManager.BEER;

  

    /* Individual Buff Debuff
    * variables are declared here in logic model,
    * variables are edited in contact listener,
    * effects are activated and processed deactivated after x seconds in logic model (sorry, had to change to logic model as i'll be using the Body(s) in main screen, didn't feel like importing again to contact listener)
    */
    // xTime to store time when collided 
    // xActive to determine whether buff/debuff is still active (becomes false after currentTime - xTime > x)

    // tweak height of jump
    /* Coffee: jump higher; Beer: jump lower; Otherwise: normal */

    public long[] effectTime = new long[4];            // effect(buff and debuff of same category) start time
    public boolean[] effectActive = new boolean[4];    // effect(buff and debuff of same category) active or not 
    public boolean[] buffActive = new boolean[4];      // whether buff is active or not 
    public boolean[] debuffActive = new boolean[4];    // whether debuff is active or not (last one is a place holder to counter Dean buff)

    public long beerTime = 0;
    public boolean beerActive = false;
    public boolean jumpLow = false;

    public long coffeeTime = 0;
    public boolean coffeeActive = false;
    public boolean jumpHigh = false;
    
    // enum for jump
    public static int NORMAL = 125;
    public static int HIGH = 160;
    public static int LOW = 80;

    // tweak speed of obstacles
    /* Sports: obstacles move faster; Biz: obstacles move slower; Otherwise: regular */
    public float regular = -20f;
    public float fast = -40f;
    public float slow = -5f;

    public long sportsTime = 0;
    public boolean sportsActive = false;
    public boolean speedUp = false;

    public long bizTime = 0;
    public boolean bizActive = false;
    public boolean slowDown = false;




    // Contructor
    // world to keep all physical objects in the game
    public IRModel(KeyboardController cont, OrthographicCamera cam, IRAssetManager assetMan, MainScreen mainScreen) {
        controller = cont;
        camera = cam;
        main = mainScreen;
        assMan = assetMan;
        world = new World(new Vector2(0, -60f), true);
        world.setContactListener(new IRContactListener(this));

        // get our body factory singleton and store it in bodyFactory
        //BodyFactory bodyFactory = BodyFactory.getInstance(world);

        // load sounds into model
        assMan.queueAddSounds();
        assMan.manager.finishLoading();
        jump = assMan.manager.get("sounds/drop.wav");
        collect = assMan.manager.get("sounds/drop.wav");

        // Init BodyEditorLoader
        playerLoader = new BodyEditorLoader(Gdx.files.internal("playerComplexPolygons.json"));
        obstacleLoader = new BodyEditorLoader(Gdx.files.internal("obstacleComplexPolygons.json"));
        buffLoader = new BodyEditorLoader(Gdx.files.internal("buffComplexPolygons.json"));
        debuffLoader = new BodyEditorLoader(Gdx.files.internal("debuffComplexPolygons.json"));

        // Load names of obstacle, buff and debuff images
        playerImage = assMan.playerImage;
        obstacleImages = assMan.obstacleImages;
        buffImages = assMan.buffImages;
        debuffImages = assMan.debuffImages;

        // Load width and height of floor
        floorWidHei = assMan.floorWidHei;
        
        // Load scale of body of different category
        playerScale = assMan.playerScale;
        obstacleScale = assMan.obstacleScale;
        buffScale = assMan.buffScale;
        debuffScale = assMan.debuffScale;

        // Create floor and player of game 
        createFloor();
        createPlayer();
    }




    // Variables for jump method
    private boolean canJump = true; // always true when player touches ground
    private boolean jumped = false;
    private int jumpCount = 0;

    public void resetJump() {
        canJump = true;
        jumped = false;
        jumpCount = 0;
    }

    private void tweakJump(int y) {
        if (player.getPosition().y < 9 && canJump && jumpCount < 5) {
            player.applyLinearImpulse(0, y, player.getWorldCenter().x, player.getWorldCenter().y, true);
            jumpCount++;
        }
        else if (player.getPosition().y > 9) {
            canJump = false;
        }
    }




    // todo ensure player cannot jump outside of view
    // logic method to run logic part of the model
    public void logicStep(float delta) {
        if (buffActive[COFFEE]) {
            if (controller.space) {
                jumped = true;
                tweakJump(HIGH);
            }
            else if (!controller.space && jumped) {
                canJump = false;
           }
        }
        if (debuffActive[BEER]) {
            if (controller.space) {
                jumped = true;
                tweakJump(LOW);
            }
            else if (!controller.space && jumped) {
                canJump = false;
           }
        }


        if (controller.space) {
            jumped = true;
            tweakJump(NORMAL);
        }
        else if (!controller.space && jumped){
            canJump = false;
            System.out.printf("Toggled canJump: %b  jumped: %b\n", canJump, jumped);
        }


        // for loop goes through all buff debuff category 
        // Check if they are expired, or both active
        // If found expired or both active, turn them off as they cancel each other
        for (int i = 0; i < 4; i++){
            // Would exists where in same category, exact expire and obtain new buff/debuff, new buff/debuff would be cancelled, let it slip as it would be very computationaly expensive to handle
            if (TimeUtils.timeSinceMillis(effectTime[i]) > 10000){
                buffActive[i] = false;
                debuffActive[i] = false;
                effectActive[i] = false;    
                effectCancellation(i);
            }
      
            if (buffActive[i] && debuffActive[i]){
                buffActive[i] = false;
                debuffActive[i] = false;
                effectActive[i] = false;    
                effectCancellation(i);
            }
        }
        // Move on to process active buff/debuff and enable their effects
        
        // Change speed of obstacle logic 
        if (effectActive[SPEED]){
            if (buffActive[BUSINESS_MAN_1_AI]){
                setSpeed(-10);
                main.spawnInterval = 4000;
            }
            else if (debuffActive[SPORTS_SCIENCE_MAJOR]){
                setSpeed(-30);
                main.spawnInterval = 3500;
            }
        }

        // Change size of obstacle logic 
        if (effectActive[SIZE]){
            if (buffActive[NUTRITION_MAJOR])
                setSize(0.0058f);
            else if (debuffActive[CULINARY_MAJOR])
                setSize(0.0082f);
        }

        // Enable immunity of player
        if (effectActive[IMMUNE])
            immunity = true;


        world.step(delta, 3, 3); // tell Box2D world to move forward in time
    }

    private void effectCancellation(int effectType){
        switch (effectType) {
            case SPEED:
                setSpeed(-20);
                main.spawnInterval = 2000;
                break;

            case SIZE:
                setSize(effectType);
                break;

            case JUMP:
                break;

            case IMMUNE:
                immunity = false;
                break;

            default:
                System.out.println("Some error has occured while cancelling effects.");
        }
    }

    private void setSpeed(int velocity){
        // Loop through all obstacles and set linear velocity to parameter (can be faster or slower, or regular)
        for (Body osbtacle : obstacles) 
            osbtacle.setLinearVelocity((float) velocity, 0);
    }

    private void setSize(float scale){
        // Get array of all fixture in player
        Array<Fixture> playerFixtures = new Array<Fixture>();
        player.getFixtureList();

        // Destroy all fixtures in player body
        for (Fixture fixture : playerFixtures)
            player.destroyFixture(fixture);
        
        // Create new FixtureDef for player Body
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1.9f;
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 0f; // bounciness

        // Load FixtureDefs to player Body
        playerLoader.attachFixture(player, playerImage, fixtureDef, scale);    // Name is the name set when making complex polygon. For all, all is image file name
    }

    private void passThrough(Body bod) {
        for (Fixture fix : bod.getFixtureList()) {
            fix.setSensor(true);
        }
    }

//    to rearrange and comment later
    private void createFloor() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0f, -10.5f);    // Max floor height is y + hy = -9    ;    Here, rectangle is set to pos in center of rectangle

        floor = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(floorWidHei.x / 2, floorWidHei.y / 2);    //Divided by 2 as .setAsBox takes half width and half height

        floor.createFixture(shape, 0f);
        floor.setUserData(new BodyData("FLOOR", 0));

        shape.dispose();
    }


    private void createPlayer() {
        // Create new BodyDef for player Body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set(-14f, (float)(floor.getPosition().y + (floorWidHei.y / 2) + 0.001));    // Complex polygon, pos is set to lower left.  Get center of floor and add with half height to get max height of floor, add 0.001 as buffer to avoid clipping
        bodyDef.fixedRotation = true;
        // Create new Body of player in World
        player = world.createBody(bodyDef);

        // Create new FixtureDef for player Body
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1.9f;
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 0f; // bounciness

        // Create new BodyEditorLoader (in declaration part) and load convex polygon using .json file
        // Has complex polygon combo for player 
        // Passes Body to BodyEditorLoader 
        // BodyEditorLoader creates multiple convex polygon using .json file 
        // 1 convex polygon, 1 FixtureDef
        // Each FixtureDef is .createFixture to Body
        // All done in BodyEditorLoader through method .attachFixture
        // Scale is scale of shape 
    
        // Load and createFixture with polygons to player Body
        // Load with respect to scale declared in asset manager
        playerLoader.attachFixture(player, playerImage, fixtureDef, playerScale);    // Name is the name set when making complex polygon. For all, all is image file name

        // Set custom class BodyData to UserData of Body of player to store bodyType and textureId
        player.setUserData(new BodyData("PLAYER", 0));
    }


    private Body createObstacle(float v) {
        // Generate random int from 0 to 3
        // int is id for texture declared (in IRAssetManager)
        int tempTextureId = MathUtils.random(0, 3);

        // Create new BodyDef 
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(16, (float)(floor.getPosition().y + (floorWidHei.y / 2)));
        // Create new Body in World
        Body obstacle = world.createBody(bodyDef);

        // Create new FixtureDef
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1f;

        // Load and createFixture with polygons to player Body
        // Load with respect to scale declared in asset manager
        obstacleLoader.attachFixture(obstacle, obstacleImages[tempTextureId], fixtureDef, obstacleScale);

        // Set obstacle to move with constant velocity of v
        obstacle.setLinearVelocity(v, 0);
        // Set custom class BodyData to UserData of Body of player to store bodyType and textureId
        obstacle.setUserData(new BodyData("OBSTACLE", tempTextureId));
        
        return obstacle;
    }

    private Body createBuff() {
        int tempTextureId = MathUtils.random(0, 3);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(16, (float)(floor.getPosition().y + (floorWidHei.y / 2) + 11.5));

        Body buff = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1f;

        buffLoader.attachFixture(buff, buffImages[tempTextureId], fixtureDef, buffScale);

        buff.setLinearVelocity(-20f, 0);
        buff.setUserData(new BodyData("BUFF", tempTextureId));

        passThrough(buff);

        return buff;
    }

    private Body createDebuff() {
        int tempTextureId = MathUtils.random(0, 2);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(16, (float)(floor.getPosition().y + (floorWidHei.y / 2) + 11.5));

        Body debuff = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1f;

        debuffLoader.attachFixture(debuff, debuffImages[tempTextureId], fixtureDef, debuffScale);

        debuff.setLinearVelocity(-20f, 0);
        debuff.setUserData(new BodyData("DEBUFF", tempTextureId));

        passThrough(debuff);

        return debuff;
    }

    public void spawnObstacles(float v) {
        obstacles.add(createObstacle(v));
        obstacleTime = TimeUtils.millis();
    }

    public void trackObstacles() {
        for (Iterator<Body> iter = obstacles.iterator(); iter.hasNext(); ) {
            Body obstacle = iter.next();
            if (obstacle.getPosition().x < -25) {  // -16 + (-9)  (9 is aprox max unit size of obstacle)
                System.out.println("Score: " + score);
                score++;
                iter.remove();
            }
        }
    }

    public void spawnBuffs() {
        buffs.add(createBuff());
        buffTime = TimeUtils.millis();
    }

    public void spawnDebuffs() {
        debuffs.add(createDebuff());
        buffTime = TimeUtils.millis();
    }

    // Check if buff/debuff is out of screen
    // If true, remove and discard
    public void trackBuffsDebuffs() {
        for (Iterator<Body> iter = buffs.iterator(); iter.hasNext(); ) {
            Body buff = iter.next();
            if (buff.getPosition().x < -21)  // -16 + (-5)  (5 is aprox max unit size of buff/debuff) 
                iter.remove();
        }
        for (Iterator<Body> iter = debuffs.iterator(); iter.hasNext(); ) {
            Body debuff = iter.next();
            if (debuff.getPosition().x < -21) 
                iter.remove();
        }
    }

    public void playSound(int sound) {
        switch(sound) {
            case JUMP_SOUND:
                jump.play();
                break;
            case COLLECT_SOUND:
                collect.play();
                break;
        }
    }

    // Takes Body
    // Returns BodyObjectType (player, obstacle, buff, debuff)
    // Used to check what Body it is (mostly in contact listener)
    public String getBodyObjectType(Body bod){
        return ((BodyData) bod.getUserData()).bodyObjectType;
    }

    // Takes Body
    // Returns TextureId (int)
    // Used to check what texture Body is using (mostly for obstacle, buff, debuff) (mostly used for hitbox and rendering)
    public int getTextureId(Body bod){
        return ((BodyData) bod.getUserData()).textureId;
    }
}
