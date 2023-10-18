package com.mars_sim.libgdx;

import com.badlogic.gdx.Screen;
import com.mars_sim.headless.MarsProjectHeadlessStarter;

public class MarsGameScreen implements Screen {
	final MarsProjectLibGDX game;

	public MarsGameScreen(final MarsProjectLibGDX game) {
		this.game = game;
		MarsProjectHeadlessStarter.main(new String[] {" -new", " -Xmx1536m"});
}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub
		
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
}