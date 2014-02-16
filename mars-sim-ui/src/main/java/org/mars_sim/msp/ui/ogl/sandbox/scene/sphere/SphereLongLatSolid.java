package org.mars_sim.msp.ui.ogl.sandbox.scene.sphere;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class SphereLongLatSolid
extends SphereLongLatMesh {

	protected int displaylist2;
	/** true when color and color2 are not equal. */
	protected boolean showMesh;
	
	/**
	 * @param center {@link Double}[3]
	 * @param rotation {@link Double}[3]
	 * @param deltaRotation {@link Double}[3]
	 * @param radius {@link Double}
	 * @param longitud {@link Integer}
	 * @param latitud {@link Integer}
	 * @param color1 {@link Double}[4]
	 * @param color2 {@link Double}[4]
	 */
	public SphereLongLatSolid(
		double[] center,
		double[] rotation,
		double[] deltaRotation,
		double radius,
		int longitud,
		int latitud,
		double[] color1,
		double[] color2
	) {
		super(center, rotation, deltaRotation, radius, longitud, latitud, color1);
		setColor2(color2);
	}

	@Override
	public void preinit(GL2 gl) {
		int x = this.getLatitude();
		int y = this.getLongitude();
		double[][][] pix = pix();
		// create a display list with quad faces
		this.displaylist = gl.glGenLists(1);
		gl.glNewList(this.displaylist,GL2.GL_COMPILE);
		for (int i = 0; i < x; i++) {
			gl.glBegin(GL2.GL_QUADS);
			for (int j = 0; j < y; j++) {
				int i1 = (i + 1) % x;
				int j1 = (j + 1) % y;
				gl.glVertex3d(pix[i][j1][0],pix[i][j1][1],pix[i][j1][2]);
				gl.glVertex3d(pix[i1][j1][0],pix[i1][j1][1],pix[i1][j1][2]);
				gl.glVertex3d(pix[i1][j][0],pix[i1][j][1],pix[i1][j][2]);
				gl.glVertex3d(pix[i][j][0],pix[i][j][1],pix[i][j][2]);
			}
			gl.glEnd();
		}
		gl.glEndList();
		// create a display list with mesh lines
		this.displaylist2 = gl.glGenLists(1);
		gl.glNewList(this.displaylist2,GL2.GL_COMPILE);
		for (int i = 0; i < x; i++) {
			gl.glBegin(GL.GL_LINE_LOOP);
			for (int j = 0; j < y; j++) {
				gl.glVertex3d(
					pix[i][j][0],
					pix[i][j][1],
					pix[i][j][2]
				);
			}
			gl.glEnd();
		}
		for (int j = 0; j < y; j++) {
			gl.glBegin(GL.GL_LINE_STRIP);
			for (int i = 0; i < x; i++) {
				gl.glVertex3d(
					pix[i][j][0],
					pix[i][j][1],
					pix[i][j][2]
				);
			}
			gl.glEnd();
		}
		gl.glEndList();
	}
	
	@Override
	public void prerender(GL2 gl) {
		super.prerender(gl);
		if (showMesh) {
			double[] color2 = this.getColor2();
			gl.glColor4d(
				color2[0],
				color2[1],
				color2[2],
				color2[3]
			);
			gl.glCallList(displaylist2);
		}
	}

	@Override
	public final void close(GL2 gl) {
		super.close(gl);
		gl.glDeleteLists(displaylist2,1);
	}

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
