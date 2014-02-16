package org.mars_sim.msp.ui.ogl.sandbox.scene.sphere;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class SphereIcosahedronSolid
extends SphereIcosahedronMesh {

	protected int displaylist2;

	/** true when color and color2 are not equal. */
	protected boolean showMesh;

	public SphereIcosahedronSolid(
		double[] center,
		double[] rotation,
		double[] deltaRotation,
		double radius,
		int recursion,
		double[] color,
		double[] color2
	) {
		super(center, rotation, deltaRotation, radius, recursion, color);
		setColor2(color2);
	}
	
	@Override
	public void preinit(GL2 gl) {
		super.preinit(gl);
		this.displaylist2 = gl.glGenLists(1);
		gl.glNewList(this.displaylist2,GL2.GL_COMPILE);
		for (int i = 0; i < triangles.size(); i++) {
			int[] t = triangles.get(i);
			gl.glBegin    (GL.GL_LINE_LOOP);
			double[] p0 = vertices.get(t[0]);
			double[] p1 = vertices.get(t[1]);
			double[] p2 = vertices.get(t[2]);
			gl.glVertex4d (p1[0],p1[1],p1[2], 1.0d);
			gl.glVertex4d (p0[0],p0[1],p0[2], 1.0d);
			gl.glVertex4d (p2[0],p2[1],p2[2], 1.0d);
			gl.glEnd();
		}
		gl.glEndList();
	}

	@Override
	public void prerender(GL2 gl) {
		super.prerender(gl);
		if (showMesh) {
			double[] koloro2 = this.getColor2();
			gl.glColor4d(
				koloro2[0],
				koloro2[1],
				koloro2[2],
				koloro2[3]
			);
			gl.glCallList(displaylist2);
		}
	}

	@Override
	public final void close(GL2 gl) {
		super.close(gl);
		gl.glDeleteLists(displaylist2,1);
	}

	/** expects a four component color vector with r, g, b and alpha channels. */
	public void setColor2(double[] color2) {
		this.setParam(PARAM_COLOR2,color2);
		double[] color = this.getColor();
		boolean equals = (color != null) && (color2 != null) && (color.length == color2.length);
		if (equals) {
			for (int i = 0; i < color.length; i++) {
				equals = equals && (color[i] == color2[i]);
			}
		}
		this.showMesh = !equals;
	}

	public double[] getColor2() {
		return this.getParamDoubleArray(PARAM_COLOR2);
	}

}
