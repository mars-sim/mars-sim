package org.mars_sim.msp.ui.ogl.sandbox.scene.sphere;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;

public class SphereLongLatMesh
extends SphereAbstract {

	protected int displaylist;

	/**
	 * Alvokas la konstruktoron de abstrakta sfero -
	 * {@link AbstraktaSfero#AbstraktaSfero(double[], double)}.
	 * @param center {@link double}[] expects a three dimensional vector.
	 * @param rotation {@link double}[] expects a three dimensional vector.
	 * @param radius {@link double} should be greater or equal zero
	 * @param longitud {@link Integer} should be greater than one
	 * @param latitud {@link Integer} should be greater than one
	 * @param color {@link Double}[]
	 */
	public SphereLongLatMesh(
		double[] center,
		double[] rotation,
		double radius,
		int longitud,
		int latitud,
		double[] color
	) {
		super(center,rotation,radius);
		this.setLongitude(longitud);
		this.setLatitude(latitud);
		this.setColor(color);
	}

	protected double[][][] pix() {
		int x = this.getLatitude();
		int y = this.getLongitude();
		double dx = Math.PI / (x - 1);
		double dy = 2 * Math.PI / y;
		double xs = 0;
		double[][][] pix = new double[x][y][3];
		for (int i = 0; i < x; i++) {
			double ys = 0;
			for (int j = 0; j < y; j++) {
				double h = Math.sin(xs);
				double[] v = new double[] {
					h * Math.sin(ys),
					h * Math.cos(ys),
					Math.cos(xs)
				};
				Util.normalize3(v);
				pix[i][j][0] = v[0];
				pix[i][j][1] = v[1];
				pix[i][j][2] = v[2];
				ys = ys + dy;
			}
			xs = xs + dx;
		}
		return pix;
	}

	@Override
	public void preinit(GL2 gl) {
		int x = this.getLatitude();
		int y = this.getLongitude();
		double[][][] pix = pix();
		this.displaylist = gl.glGenLists(1);
		gl.glNewList(this.displaylist,GL2.GL_COMPILE);
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
	protected void prerender(GL2 gl) {
		super.prerender(gl);
		double radius = this.getRadius();
		double color[] = this.getColor();
		gl.glScaled(
			radius,
			radius,
			radius
		);
		gl.glColor4d(
			color[0],
			color[1],
			color[2],
			color[3]
		);
		gl.glCallList(displaylist);
	}

	public void setColor(double[] color) {
		this.setParam(PARAM_COLOR,color);
	}

	public double[] getColor() {
		return this.getParamDoubleArray(PARAM_COLOR);
	}

	public void setLongitude(int longitude) {
		this.setParam(PARAM_LONGITUDE,longitude);
	}
	
	public void setLatitude(int latitude) {
		this.setParam(PARAM_LATITUDE,latitude);
	}
	
	public int getLongitude() {
		return this.getParamInt(PARAM_LONGITUDE);
	}
	
	public int getLatitude() {
		return this.getParamInt(PARAM_LATITUDE);
	}
}
