/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.mars_sim.libgdx.surface3d;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class OrbitCameraController extends InputAdapter {

    private final PerspectiveCamera cam;
    private final Vector3 target = new Vector3();

    // Angles and distance
    private float yawDeg = 0f;      // around Y axis
    private float pitchDeg = 15f;   // up-down
    private float distance = 100f;

    // Limits
    private float minDistance = 10f;
    private float maxDistance = 100000f;

    // Controls
    private float rotateSpeed = 0.5f;
    private float scrollZoomStrength = 0.1f;
    private int lastX = -1;
    private int lastY = -1;
    private boolean rotating = false;

    public OrbitCameraController(PerspectiveCamera cam, float targetX, float targetY, float targetZ) {
        this.cam = cam;
        this.target.set(targetX, targetY, targetZ);
        updateCamera();
    }

    public void setDistance(float d) {
        distance = MathUtils.clamp(d, minDistance, maxDistance);
        updateCamera();
    }

    public void setClampDistance(float min, float max) {
        this.minDistance = min;
        this.maxDistance = max;
        setDistance(distance);
    }

    public void setRotateSpeed(float s) {
        this.rotateSpeed = s;
    }

    public void setScrollZoomStrength(float s) {
        this.scrollZoomStrength = s;
    }

    public void update(float dt) {
        // No-op for now; leave hook for future smoothing/damping.
        updateCamera();
    }

    private void updateCamera() {
        float yawRad = yawDeg * MathUtils.degreesToRadians;
        float pitchRad = MathUtils.clamp(pitchDeg, -89.9f, 89.9f) * MathUtils.degreesToRadians;

        float cosPitch = MathUtils.cos(pitchRad);
        float sinPitch = MathUtils.sin(pitchRad);
        float cosYaw = MathUtils.cos(yawRad);
        float sinYaw = MathUtils.sin(yawRad);

        // Spherical → Cartesian
        float x = target.x + distance * cosPitch * sinYaw;
        float y = target.y + distance * sinPitch;
        float z = target.z + distance * cosPitch * cosYaw;

        cam.position.set(x, y, z);
        cam.lookAt(target);
        cam.up.set(0f, 1f, 0f);
        cam.update();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            rotating = true;
            lastX = screenX;
            lastY = screenY;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!rotating) {
            return false;
        }
        int dx = screenX - lastX;
        int dy = screenY - lastY;
        lastX = screenX;
        lastY = screenY;
        yawDeg -= dx * rotateSpeed;
        pitchDeg -= dy * rotateSpeed;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            rotating = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        float zoom = 1f + (amountY * scrollZoomStrength);
        distance = MathUtils.clamp(distance * zoom, minDistance, maxDistance);
        return true;
    }
}
