package org.mars_sim.msp.ui.ogl.sandbox.scene;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL2;

/**
 * this tells what a scene object is and what it can generally do.
 * @author stpa
 */
public interface SceneObjectInterface {

	public static final double PI180 = Math.PI / 180.0f;
	/** the smallest possible ammount of quantized time. */
	public static final double PLANCK_TIME = 0.0000001f;

	/** usually defines the object's anchor point relative to its parent. */
	public static final String PARAM_TRANSLATION = "translation";
	/** which way the object is rotated, where it is facing, its orientation. */
	public static final String PARAM_ROTATION = "rotation";
	/** expressed in degrees per second change in rotation/orientation/facing-direction. */
	public static final String PARAM_DELTA_ROTATION = "deltaRotation";
	/** scaling, to embiggen or to shrink objects. */
	public static final String PARAM_SCALE = "scale";
	/** a four component color vector with r, g, b and alpha channels, primary color. */
	public static final String PARAM_COLOR = "col";
	/** another four component color vector with r, g, b and alpha channels, secondary color. */
	public static final String PARAM_COLOR2 = "col2";
	public static final String PARAM_RADIUS = "radius";
	public static final String PARAM_RECURSION = "recursion";
	public static final String PARAM_LONGITUDE = "lon";
	public static final String PARAM_LATITUDE = "lat";
	public static final String PARAM_TEXTURE = "texture";
	public static final String PARAM_CAPTION = "caption";
	public static final String PARAM_TEXT_ORIENTATION = "orientation";

	/** initialization of drawable surface (recursively). */
	public void init(GL2 gl,long tempo);
	/** change values or internal states (recursively). */
	public void update(long delta_tempo);
	/** destroy the object (recursively) and prepare for garbage collection. */
	public void close(GL2 gl);

	public void addSubobject(SceneObjectInterface objekt);
	public void removeSubobject(SceneObjectInterface objekt);
	public SceneObjectInterface getSubobject(int i);
	public List<SceneObjectInterface> getSubobjects();

	/**
	 * first calls its own {@link #prerender()}-method,
	 * then the {@link #render()}-method of all subobjects,
	 * and then its own {@link #postrender()}-method.
	 */
	public void render(GL2 gl);

	/**
	 * calls {@link #setParam(String, Object)} with {@link #PARAM_TRANSLATION}
	 * to change the anchor point of this object.
	 */
	public void setTranslation(double[] translation);
	/**
	 * calls {@link #setParam(String, Object)} with {@link #PARAM_ROTATION}
	 * to change the direction/orientation/rotation of this object.
	 */
	public void setRotation(double[] rotation);

	/**
	 * can handle any type of parameter. use {@link SceneObjectInterface}'s static
	 * parameter names to access specific parameters, e.g. {@link #PARAM_COLOR},
	 * {@link #PARAM_RADIUS}, et cetera.
	 */
	public void setParam(String param,Object value);

	public Set<String> getParamKeys();
	public Map<String,Object> getParams();

	/** returns the raw parameter or null if it does not exists. */
	public Object getParam(String param);
	/** calls {@link #getParam(String)} and casts its result to {@link String}. */
	public String getParamString(String param);
	/** calls {@link #getParam(String)} and casts its result to an array of {@link Double}. */
	public double[] getParamDoubleArray(String param);
	/** calls {@link #getParam(String)} and casts its result to {@link Double}. */
	public double getParamDouble(String param);
	/** calls {@link #getParam(String)} and casts its result to {@link Float}. */
	public float getParamFloat(String param);
	/** calls {@link #getParam(String)} and casts its result to {@link Integer}. */
	public int getParamInt(String param);
}
