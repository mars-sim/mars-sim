package org.mars_sim.msp.ui.ogl.sandbox.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;

public class SceneObjectAbstract
implements SceneObjectInterface {

	protected List<SceneObjectInterface> subObjects = new ArrayList<SceneObjectInterface>();
	protected Map<String,Object> parameters = new HashMap<String,Object>();
	
	public SceneObjectAbstract(double[] translation, double[] rotation) {
		this.setTranslation(translation);
		this.setRotation(rotation);
	}
	
	public void init(GL2 gl, long time) {
		preinit(gl);
		for (SceneObjectInterface subobjekto : subObjects) {
			subobjekto.init(gl,time);
		}
		postinit(gl);
	}
	
	public void update(long deltaTime) {
		preupdate(deltaTime);
		for (SceneObjectInterface subobjekto : subObjects) {
			subobjekto.update(deltaTime);
		}
		postupdate(deltaTime);
	}
	
	public void close() {
		for (SceneObjectInterface o : subObjects) {
			o.close();
		}
	}
	
	public void addSubobject(SceneObjectInterface object) {
		this.subObjects.add(object);
	}
	
	public void removeSubobject(SceneObjectInterface object) {
		this.subObjects.remove(object);
	}
	
	public void render(GL2 gl) {
		prerender(gl);
		for (SceneObjectInterface subObject : subObjects) {
			subObject.render(gl);
		}
		postrender(gl);
	}
	
	/**
	 * ĉi tie la objekto povas puŝi la antaŭan matricon kaj pentri sin mem,
	 * la transformoj do validu ankaŭ por ĉiuj subobjektoj.
	 */
	protected void prerender(GL2 gl) {
		double[] translation = this.getTranslation();
		double[] rotation = this.getRotation();
		gl.glPushMatrix();
		gl.glTranslated(
			translation[0],
			translation[1],
			translation[2]
		);
		gl.glRotated(rotation[0],1.0f,0.0f,0.0f);
		gl.glRotated(rotation[1],0.0f,1.0f,0.0f);
		gl.glRotated(rotation[2],0.0f,0.0f,1.0f);
	}
	
	/**
	 * ĉi tie la objekto povas popi/restaŭri la antaŭan matricon.
	 */
	protected void postrender(GL2 gl) {
		gl.glPopMatrix();
	}

	/**
	 * @param deltaTime {@link Long} in nanoseconds
	 */
	protected void preupdate(long deltaTime) {
		// do nothing.
	}

	protected void postupdate(long deltaTime) {
		// do nothing.
	}
	
	protected void preinit(GL2 gl) {
		// do nothing.
	}
	
	protected void postinit(GL2 gl) {
		// do nothing.
	}

	public double[] getRotation() {
		return (double[]) this.parameters.get(PARAM_ROTATION);
	}

	public double[] getTranslation() {
		return (double[]) this.parameters.get(PARAM_TRANSLATION);
	}

	public void setRotation(double[] rotation) {
		this.setParam(PARAM_ROTATION,rotation);
	}

	public void setTranslation(double[] translation) {
		this.setParam(PARAM_TRANSLATION,translation);
	}

	public void setParam(String param, Object value) {
		this.parameters.put(param,value);
	}
	
	public Object getParam(String param) {
		return this.parameters.get(param);
	}

	public String getParamString(String param) {
		return (String) this.parameters.get(param);
	}

	public double[] getParamDoubleArray(String param) {
		return (double[]) this.parameters.get(param);
	}
	
	public double getParamDouble(String param) {
		return (Double) this.parameters.get(param);
	}
	
	public float getParamFloat(String param) {
		return (Float) this.parameters.get(param);
	}

	public int getParamInt(String param) {
		return (Integer) this.parameters.get(param);
	}

}
