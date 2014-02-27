package org.mars_sim.msp.ui.ogl.sandbox.scene.line;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.mars_sim.msp.ui.ogl.sandbox.scene.SceneObjectAbstract;

/**
 * @author stpa
 * 2014-02-27
 */
public class Line
extends SceneObjectAbstract {

	/**
	 * constructor.
	 * @param translation
	 * @param rotation
	 * @param point1
	 * @param point2
	 * @param color
	 */
	public Line(
		double[] translation,
		double[] rotation,
		double[] point1,
		double[] point2,
		double[] color
	) {
		super(translation,rotation);
		this.setPoint1(point1);
		this.setPoint2(point2);
		this.setColor(color);
	}
	
	@Override
	public void prerender(GL2 gl) {
		super.prerender(gl);
		double[] punkto1 = this.getParamDoubleArray(PARAM_POINT1);
		double[] punkto2 = this.getParamDoubleArray(PARAM_POINT2);
		double[] koloro = this.getParamDoubleArray(PARAM_COLOR);
		gl.glPushMatrix();
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glColor4d(koloro[0],koloro[1],koloro[2],koloro[3]);
		gl.glVertex3d(punkto1[0],punkto1[1],punkto1[2]);
		gl.glVertex3d(punkto2[0],punkto2[1],punkto2[2]);
		gl.glEnd();
	}
	
	public double[] getColor() {
		return this.getParamDoubleArray(PARAM_COLOR);
	}
	
	public void setColor(double[] color) {
		this.setParam(PARAM_COLOR,color);
	}
	
	public void setPoint1(double[] point1) {
		this.setParam(PARAM_POINT1,point1);
	}
	
	public void setPoint2(double[] point2) {
		this.setParam(PARAM_POINT2,point2);
	}
	
	public double[] getPoint1() {
		return this.getParamDoubleArray(PARAM_POINT1);
	}
	
	public double[] getPoint2() {
		return this.getParamDoubleArray(PARAM_POINT2);
	}
}
