package com.instattrunner;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.TimeUtils;
import com.instattrunner.loader.IRAssetManager;

// contact listener to react to collisions in world
// everytime two bodies collide, beginContact is run
public class IRContactListener implements ContactListener {
    private IRModel parent;

    // For output to terminal for debugging
    public final String[] buffTypes = {"Business man 1 (AI)", "Nutrition major", "Coffee", "Dean"};
    public final String[] debuffTypes = {"Sports science major", "Culinary major", "Beer"};

    // might not need this 

    // Enum for obstacle, buff, debuff
    // obstacle
    public static final int CAT = IRAssetManager.CAT;
    public static final int GOOSE = IRAssetManager.GOOSE;
    public static final int LAKE = IRAssetManager.LAKE;
    public static final int STAIRS = IRAssetManager.STAIRS;
    // buff
    public static final int BUSINESS_MAN_1_AI = IRAssetManager.BUSINESS_MAN_1_AI;
    public static final int NUTRITION_MAJOR = IRAssetManager.NUTRITION_MAJOR;
    public static final int DEAN = IRAssetManager.DEAN;
    public static final int COFFEE = IRAssetManager.COFFEE;
    // debuff
    public static final int SPORTS_SCIENCE_MAJOR = IRAssetManager.SPORTS_SCIENCE_MAJOR;
    public static final int CULINARY_MAJOR = IRAssetManager.CULINARY_MAJOR;
    public static final int BEER = IRAssetManager.BEER;

    // Just to temporary hold id of buff/debuff
    private int tempTextureId;

    // Constructor
    public IRContactListener(IRModel parent) {
        this.parent = parent;
    }

    // each body has a fixture which is the colliding part (store in fa, fb)
    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        // Check player collide with obstacle
        if ((parent.getBodyObjectType(fa.getBody()) == "OBSTACLE" && parent.getBodyObjectType(fb.getBody()) == "PLAYER") || (parent.getBodyObjectType(fb.getBody()) == "OBSTACLE" && parent.getBodyObjectType(fa.getBody()) == "PLAYER")) {
            System.out.println("Player hit obstacle");
            parent.isDead = true; // triggers change to end screen from render() in main
            return;
        }

        // Check player collide with ground to determine whether player on ground or not 
        if ((parent.getBodyObjectType(fa.getBody()) == "FLOOR" && parent.getBodyObjectType(fb.getBody()) == "PLAYER") || (parent.getBodyObjectType(fb.getBody()) == "FLOOR" && parent.getBodyObjectType(fa.getBody()) == "PLAYER")) {
            System.out.printf("Player is on ground at %f\n", fa.getBody().getPosition().y);
            parent.resetJump();
            return;
        }

        // Check player collide with buff and produce relevant effect 
        if (parent.getBodyObjectType(fa.getBody()) == "BUFF" || parent.getBodyObjectType(fb.getBody()) == "BUFF") {
            // Get TextureId of buff
            if (parent.getBodyObjectType(fa.getBody()) == "BUFF")
                tempTextureId = parent.getTextureId(fa.getBody());
            else    
                tempTextureId = parent.getTextureId(fb.getBody());
            
            System.out.printf("Buff : %s\n", buffTypes[tempTextureId]);
            parent.playSound(IRModel.COLLECT_SOUND);
            parent.effectTime[tempTextureId] = TimeUtils.millis();
            parent.effectActive[tempTextureId] = true;
            parent.buffActive[tempTextureId] = true;

            return;
        }

        // Check player collide with debuff and produce relevant effect 
        if (parent.getBodyObjectType(fa.getBody()) == "DEBUFF" || parent.getBodyObjectType(fb.getBody()) == "DEBUFF") {
            // Get TextureId of buff
            if (parent.getBodyObjectType(fa.getBody()) == "DEBUFF")
                tempTextureId = parent.getTextureId(fa.getBody());
            else    
                tempTextureId = parent.getTextureId(fb.getBody());

            System.out.printf("Debuff : %s\n", debuffTypes[tempTextureId]);
            parent.playSound(IRModel.COLLECT_SOUND);
            parent.effectTime[tempTextureId] = TimeUtils.millis();
            parent.effectActive[tempTextureId] = true;
            parent.debuffActive[tempTextureId] = true;

            return;
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
