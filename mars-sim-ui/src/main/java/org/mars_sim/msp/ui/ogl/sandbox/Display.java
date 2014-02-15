package org.mars_sim.msp.ui.ogl.sandbox;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import org.mars_sim.msp.ui.ogl.sandbox.scene.DisplayAbstract;

/**
 * with some inspiration from various tutorials, especially nehe.
 * @author stpa
 */
public class Display
extends DisplayAbstract {

	protected boolean increaseX;
	protected boolean decreaseX;
	protected boolean increaseY;
	protected boolean decreaseY;
	protected boolean zoomIn;
	protected boolean zoomOut;
	protected boolean fore;
	protected boolean back;
	protected boolean right;
	protected boolean left;
	protected boolean up;
	protected boolean down;

	@Override
	public final void init(GLAutoDrawable glDrawable) {
		super.init(glDrawable);
		GL2 gl = glDrawable.getGL().getGL2();
		gl.glCullFace(GL.GL_BACK);                            // Set Culling Face To Back Face
        gl.glEnable(GL.GL_CULL_FACE);                          // Enable Culling
        gl.glEnable(GL.GL_TEXTURE_2D);                          // Enable Texture Mapping
        gl.glShadeModel(GL2.GL_SMOOTH);                          // Enables Smooth Color Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);                  // This Will Clear The Background Color To Black
        gl.glClearDepth(1.0);                                      // Enables Clearing Of The Depth Buffer
        gl.glEnable(GL.GL_DEPTH_TEST);                              // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL);                                // The Type Of Depth Test To Do
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); // Really Nice Perspective Calculations
        gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);            // Use The Good Calculations ( NEW )
        gl.glEnable(GL.GL_LINE_SMOOTH);                            // Enable Anti-Aliasing ( NEW )
	}

	@Override
	public void display(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer
        gl.glLoadIdentity();                                        // Reset Modelview Matrix
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_DEPTH_TEST);
       	gl.glEnable(GL.GL_BLEND);
        if (SMOOTH_LINES) {
        	gl.glEnable(GL.GL_LINE_SMOOTH); // Enable Anti-Aliasing ( NEW )
        } else {
        	gl.glDisable(GL.GL_LINE_SMOOTH);
        }
		super.display(glDrawable);
        if (zoomOut) {
        	double[] x = scene.getTranslation();
        	if (Math.abs(x[2]) > 0.0f) {
        		x[2] /= ZOOM_FACTOR;
        	} else {
        		x[2] = 0.05f;
        	}
        	scene.setTranslation(x);
            zoomOut = false;
        }
        if (zoomIn) {
        	double[] x = scene.getTranslation();
        	if (Math.abs(x[2]) > 0.0f) {
        		x[2] *= ZOOM_FACTOR;
        	} else {
        		x[2] = 0.05;
        	}
        	scene.setTranslation(x);
            zoomIn = false;
        }
        if (decreaseX) {
        	double[] x = scene.getRotation();
        	x[0] -= 0.01f;
        	scene.setRotation(x);
            decreaseX = false;
        }
        if (increaseX){
        	double[] x = scene.getRotation();
        	x[0] += 0.01f;
        	scene.setRotation(x);
            increaseX = false;
        }
        if (increaseY){
        	double[] x = scene.getRotation();
        	x[1] -= 0.01f;
        	scene.setRotation(x);
            increaseY = false;
        }
        if (decreaseY){
        	double[] x = scene.getRotation();
        	x[1] += 0.01f;
        	scene.setRotation(x);
            decreaseY = false;
        }
        if (fore) {
        	double[] x = scene.getTranslation();
        	x[2] += DELTA_WASD;
        	scene.setTranslation(x);
            fore = false;
        }
        if (back) {
        	double[] x = scene.getTranslation();
        	x[2] -= DELTA_WASD;
        	scene.setTranslation(x);
            back = false;
        }
        if (right) {
        	double[] x = scene.getTranslation();
        	x[0] -= DELTA_WASD;
        	scene.setTranslation(x);
            right = false;
        }
        if (left) {
        	double[] x = scene.getTranslation();
        	x[0] += DELTA_WASD;
        	scene.setTranslation(x);
            right = false;
        }
        if (up) {
        	double[] x = scene.getTranslation();
        	x[1] -= DELTA_WASD;
        	scene.setTranslation(x);
            up = false;
        }
        if (down) {
        	double[] x = scene.getTranslation();
        	x[1] += DELTA_WASD;
        	scene.setTranslation(x);
            down = false;
        }
	}

    public void increaseXspeed(boolean increase) {
        increaseX = increase;
    }

    public void decreaseXspeed(boolean decrease) {
        decreaseX = decrease;
    }

    public void increaseYspeed(boolean increase) {
        increaseY = increase;
    }

    public void decreaseYspeed(boolean decrease) {
        decreaseY = decrease;
    }

    public void zoomIn(boolean zoom) {
        zoomIn = zoom;
    }

    public void zoomOut(boolean zoom) {
        zoomOut = zoom;
    }

    public void fore(boolean fore) {
    	this.fore = fore;
    }

    public void back(boolean back) {
    	this.back = back;
    }

    public void right(boolean right) {
    	this.right = right;
    }

    public void left(boolean left) {
    	this.left = left;
    }

    public void up(boolean up) {
    	this.up = up;
    }

    public void down(boolean down) {
    	this.down = down;
    }
}
