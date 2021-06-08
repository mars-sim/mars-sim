/**
 * Mars Simulation Project
 * MySplash.java
 * @version 3.1.2 2021-05-28
 * @author Manny Kung
 */
package org.mars_sim.libgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class MySplash extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture img;

	long startTime;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		   
		// Family on Mars by Patrick Leger
		img = new Texture("/splash/marsfamily.jpg");

	}

	@Override
	public void render() {
		ScreenUtils.clear(1, 0, 0, 1);
		
	    if(TimeUtils.millis() - startTime != 5_000) {
	        // 5 seconds haven't passed yet
	        batch.begin();
	        // Creates a white background
//	        Gdx.gl.glClearColor(1f, 1f, 1f, 1);
			batch.draw(img, 0, 0);
	        // Draw your animation here
	        batch.end();
	    } else {
//	    	 app.setScreen(new MainMenuScreen());
//	    	this.setScreen(new MainMenuScreen(this));
	    }
		
//		if (Gdx.input.isTouched()) {
//			dispose();
//		}
		
	}
	
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}

}
