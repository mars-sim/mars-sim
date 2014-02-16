package org.mars_sim.msp.ui.ogl.sandbox.scene.text;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

public class TextOutline
extends TextAbstract {

	protected static final int FONT = GLUT.STROKE_MONO_ROMAN;
	protected GLUT glut = new GLUT();

	public TextOutline(
		double[] translation,
		double[] rotation,
		double[] deltaRotation,
		double[] scale,
		String caption,
		EnumTextOrientation textOrientation,
		double[] color
	) {
		super(translation, rotation, deltaRotation, scale, caption, textOrientation);
		this.setColor(color);
	}

	@Override
	public void prerender(GL2 gl) {
		super.prerender(gl);
		double color[] = this.getColor();
		gl.glColor4d(
			color[0],
			color[1],
			color[2],
			color[3]
		);
		String text = this.getCaption();
		if (text != null && text.length() > 0) {
			gl.glScalef(0.001f, 0.001f, 0.0f);
			double length = glut.glutStrokeLength(FONT,text);
			switch (this.getTextOrientation()) {
				case CENTER : gl.glTranslated(-length * 0.5f,0.0f,0.0f); break;
				case RIGHT : gl.glTranslated(-length,0.0f,0.0f); break;
				default : // do nothing in this case, the text is left by default.
			}
			glut.glutStrokeString(FONT,text);
		}
	}

	public void setColor(double[] color) {
		this.setParam(PARAM_COLOR,color);
	}

	public double[] getColor() {
		return this.getParamDoubleArray(PARAM_COLOR);
	}
}
