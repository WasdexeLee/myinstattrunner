package com.instattrunner.controller;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

public class KeyboardController implements InputProcessor {
    public boolean space;

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.SPACE || keycode == Input.Keys.UP) {
            space = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.SPACE || keycode == Input.Keys.UP) {
            space = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
