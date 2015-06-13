package org.mars_sim.msp.ui.jme3.jme3FX;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class CameraDriverAppState extends AbstractAppState {
	Application app;

	public float up = 0;
	public float down = 0;
	public float forward = 0;
	public float backward = 0;
	public float left = 0;
	public float right = 0;
	//to continue yaw pitch ...

	//tmp store
	private Vector3f v3 = new Vector3f();

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);
		this.app = app;
	}

	@Override
	public void update(float tpf) {
		super.update(tpf);
		Camera cam = app.getCamera();
		Vector3f pos = cam.getLocation();

		cam.getLeft(v3);
		v3.multLocal((left - right) * tpf);
		pos.addLocal(v3);

		cam.getDirection(v3);
		v3.multLocal((forward - backward) * tpf);
		pos.addLocal(v3);

		cam.getUp(v3);
		v3.multLocal((up - down) * tpf);
		pos.addLocal(v3);

		cam.setLocation(pos);
	}
}
