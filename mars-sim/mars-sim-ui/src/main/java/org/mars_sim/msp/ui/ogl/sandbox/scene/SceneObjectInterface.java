package org.mars_sim.msp.ui.ogl.sandbox.scene;

import javax.media.opengl.GL2;

public interface SceneObjectInterface {

	public static final double PI180 = Math.PI / 180.0f;
	/** the smallest possible ammount of quantized time. */
	public static final double PLANCK_TIME = 0.0000001f;
	
	public static final String PARAM_TRANSLATION = "translation";
	public static final String PARAM_ROTATION = "rotation";
	public static final String PARAM_DELTA_ROTATION = "deltaRotation";
	public static final String PARAM_SCALE = "scale";
	public static final String PARAM_COLOR = "col";
	public static final String PARAM_COLOR2 = "col2";
	public static final String PARAM_RADIUS = "radius";
	public static final String PARAM_RECURSION = "recursion";
	public static final String PARAM_LONGITUDE = "lon";
	public static final String PARAM_LATITUDE = "lat";
	public static final String PARAM_TEXTURE = "texture";
	
	/** initialization of drawable surface. */
	public void init(GL2 gl,long tempo);
	/** change values or internal states. */
	public void update(long delta_tempo);
	/** destroy the object and prepare for garbage collection. */
	public void close(GL2 gl);

	public void addSubobject(SceneObjectInterface objekt);
	public void removeSubobject(SceneObjectInterface objekt);

	/**
	 * first calls its own {@link #prerender()}-method,
	 * then the {@link #render()}-method of all subobjects,
	 * and then its own {@link #postrender()}-method.
	 */
	public void render(GL2 gl);
	
	public void setTranslation(double[] translation);
	public void setRotation(double[] rotation);
	
	public void setParam(String param,Object value);

	public Object getParam(String param);
	public double[] getParamDoubleArray(String param);
	public double getParamDouble(String param);
	public float getParamFloat(String param);
	public int getParamInt(String param);
}
