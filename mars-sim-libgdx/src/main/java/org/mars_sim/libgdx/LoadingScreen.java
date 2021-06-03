package org.mars_sim.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class LoadingScreen implements Screen {

	public final static int MENU = 0;
	public final static int PREFERENCES = 1;
	public final static int APPLICATION = 2;
	public final static int ENDGAME = 3;
	
	final MarsProjectLibGDX game;
	private Texture img;
	OrthographicCamera camera;

	public LoadingScreen(final MarsProjectLibGDX game) {
		this.game = game;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 990, 1000);
		   
		// Family on Mars by Patrick Leger
		img = new Texture("img/marsfamily.jpg");
	}

//	@Override
//	public void create () {
//		loadingScreen = new LoadingScreen();
//		setScreen(loadingScreen);
//	}
//	
//	public void changeScreen(int screen){
//		switch(screen){
//			case MENU:
//				if(menuScreen == null) menuScreen = new MenuScreen(this); // added (this)
//				this.setScreen(menuScreen);
//				break;
//			case PREFERENCES:
//				if(preferencesScreen == null) preferencesScreen = new PreferencesScreen(this); // added (this)
//				this.setScreen(preferencesScreen);
//				break;
//			case APPLICATION:
//				if(mainScreen == null) mainScreen = new MainScreen(this); //added (this)
//				this.setScreen(mainScreen);
//				break;
//			case ENDGAME:
//				if(endScreen == null) endScreen = new EndScreen(this);  // added (this)
//				this.setScreen(endScreen);
//				break;
//		}
//	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void render(float delta) {
		ScreenUtils.clear(0, 0, 0.2f, 1);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.font.setColor(0, 0, 0, 1);
		
		game.batch.begin();
		game.batch.draw(img, 0, 0);
		game.font.draw(game.batch, "Welcome to Mars Simulation Project  ", 250, 850);
		game.font.draw(game.batch, "Tap anywhere to begin!", 100, 100);
		game.batch.end();

		if (Gdx.input.isTouched()) {
			game.setScreen(new MarsGameScreen(game));
			hide();
		}
	}


	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}


        //...Rest of class omitted for succinctness.

}
