package org.mars_sim.msp.ui.ogl.sandbox.scene.sphere;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;

/**
 * represents a recursively subdivided icosahedron,
 * a so called tesselation. only the wire mesh is
 * displayed, this is not a solid body.
 * @author stpa
 */
public class SphereIcosahedronMesh
extends SphereAbstract {

	/** folder for caching meshes . */
	private static final String MESH_PATH = "src/main/resources/3d/icosa-";
	
	/** file suffix for caching meshes. */
	private static final String SUFFIX = ".icosa";
	
	/** vertex coordinates of a simple icosahedron. */
	private static final double VERTICES[][] = {
		{-0.525731112119133606d,  0.0d,  0.850650808352039932d},
		{ 0.525731112119133606d,  0.0d,  0.850650808352039932d}, 
		{-0.525731112119133606d,  0.0d, -0.850650808352039932d}, 
		{ 0.525731112119133606d,  0.0d, -0.850650808352039932d},
		{ 0.0d,  0.850650808352039932d,  0.525731112119133606d}, 
		{ 0.0d,  0.850650808352039932d, -0.525731112119133606d}, 
		{ 0.0d, -0.850650808352039932d,  0.525731112119133606d}, 
		{ 0.0d, -0.850650808352039932d, -0.525731112119133606d},
		{ 0.850650808352039932d,  0.525731112119133606d,  0.0d}, 
		{-0.850650808352039932d,  0.525731112119133606d,  0.0d}, 
		{ 0.850650808352039932d, -0.525731112119133606d,  0.0d}, 
		{-0.850650808352039932d, -0.525731112119133606d,  0.0d}
	};
	
	/** triangles of a simple icosahedron. */
	private static final int TRIANGLES[][] = {
		{0,4,1}, {0,9,4}, {9,5,4}, {4,5,8}, {4,8,1},
		{8,10,1}, {8,3,10}, {5,3,8}, {5,2,3}, {2,7,3},
		{7,10,3}, {7,6,10}, {7,11,6}, {11,0,6}, {0,1,6},
		{6,1,10}, {9,0,11}, {9,11,2}, {9,2,5}, {7,2,11}
	};

	/** openGL display list to speed up displaying things. */
	protected int displaylist;
	
	/** list of triangles of the subdivided icosehedron. */
	protected List<int[]> triangles = new ArrayList<int[]>();
	
	/** list of all vertices of the triangulated icosahedron. */
	protected List<double[]> vertices = new ArrayList<double[]>();

	/**
	 * constructor.
	 * @param center {@link Double}[]
	 * @param rotation {@link Double}[]
	 * @param radius {@link Double}
	 * @param recursions {@link Integer} should be in [0 .. 5]
	 * @param color {@link Double}[] expects a four component color vector with r, g, b and alpha channels
	 */
	public SphereIcosahedronMesh(
		double[] center,
		double[] rotation,
		double[] deltaRotation,
		double radius,
		int recursions,
		double[] color
	) {
		super(center, rotation, deltaRotation, radius);
		this.setColor(color);
		this.setRecursion(recursions);
	}

	private int registerVertex(double[] v) {
		int i = vertices.indexOf(v);	// lets see if the vertex already exists
		if (i < 0) {					// if not:
			vertices.add(v);			//    add it as new
			return vertices.size() - 1;	//    and return its index
		} else {						// if yes:
			return i;					//    return existing index
		}
	}

	private void registerTriangle(double[] v0, double[] v1, double[] v2) {
		int[] t = new int[] {
			registerVertex(v0),
			registerVertex(v1),
			registerVertex(v2)
		};
		triangles.add(t);
	}

	/** performs a recursive subdivision of triangles. */
	private void subdivide(double[] v0, double[] v1, double[] v2, int step) {
		if (step == 0) {
			// end of recursion
			registerTriangle(v0,v1,v2);
		} else {
			double[] v01 = Util.add(v0,v1);	// calculate vertices for triangulation
			double[] v12 = Util.add(v1,v2);
			double[] v20 = Util.add(v2,v0);
			Util.length3(v01);           // normalize vertices to unit sphere
			Util.length3(v12);
			Util.length3(v20);
			subdivide(v0,  v01, v20, step - 1);	// recursion
			subdivide(v1,  v12, v01, step - 1);
			subdivide(v2,  v20, v12, step - 1);
			subdivide(v01, v12, v20, step - 1);
		}
	}

	/**
	 * load existing mesh file to avoid unneccessarily recalculating everything.
	 * returns <code>true</code> if loading mesh was successfull.
	 */
	private boolean loadMesh(int recursion) {
		vertices.clear();
		triangles.clear();
		boolean success = false;
		String targetPath = MESH_PATH + recursion + SUFFIX;
		try {
			BufferedReader r = new BufferedReader(new FileReader(targetPath));
			int x = Integer.parseInt(r.readLine());
			for (int i = 0; i < x; i++) {
				int p0 = Integer.parseInt(r.readLine());
				int p1 = Integer.parseInt(r.readLine());
				int p2 = Integer.parseInt(r.readLine());
				triangles.add(new int[] {p0,p1,p2});
			}
			x = Integer.parseInt(r.readLine());
			for (int i = 0; i < x; i++) {
				double v0 = Double.parseDouble(r.readLine());
				double v1 = Double.parseDouble(r.readLine());
				double v2 = Double.parseDouble(r.readLine());
				vertices.add(new double[] {v0,v1,v2});
			}
			r.close();
			success = true;
		} catch (IOException e) {
			vertices.clear();
			triangles.clear();
		}
		return success;
	}
	
	/**
	 * save mesh to file to be reused later.
	 */
	private void saveMesh(int recursion) {
		String targetPath = MESH_PATH + recursion + SUFFIX;
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(targetPath));
			w.write(Integer.toString(triangles.size()));
			w.newLine();
			for (int i = 0; i < triangles.size(); i++) {
				for (int j = 0; j < 3; j++) {
					w.write(Integer.toString(triangles.get(i)[j]));
					w.newLine();
				}
			}
			w.write(Integer.toString(vertices.size()));
			w.newLine();
			for (int i = 0; i < vertices.size(); i++) {
				for (int j = 0; j < 3; j++) {
					w.write(Double.toString(vertices.get(i)[j]));
					w.newLine();
				}
			}
			w.close();
		} catch (IOException e) {
			// maybe should react to this exception
		}
	}
	
	@Override
	public void preinit(GL2 gl) {
		int recursion = this.getRecursion();
		if (loadMesh(recursion) == false) {
			for (int i = 0; i<20; i++) {
				subdivide(
					VERTICES[TRIANGLES[i][0]],
					VERTICES[TRIANGLES[i][1]],
					VERTICES[TRIANGLES[i][2]],
					recursion
				);
			}
			saveMesh(recursion);
		}
		// create a new display list
		this.displaylist = gl.glGenLists(1);
		gl.glNewList(this.displaylist,GL2.GL_COMPILE);
		for (int i = 0; i < triangles.size(); i++) {
			int[] t = triangles.get(i);
			gl.glBegin    (GL2.GL_TRIANGLES);
			gl.glVertex3d (vertices.get(t[1])[0],vertices.get(t[1])[1],vertices.get(t[1])[2]);
			gl.glVertex3d (vertices.get(t[0])[0],vertices.get(t[0])[1],vertices.get(t[0])[2]);
			gl.glVertex3d (vertices.get(t[2])[0],vertices.get(t[2])[1],vertices.get(t[2])[2]);
			gl.glEnd();
		}
		gl.glEndList();
	}
	
	@Override
	public void prerender(GL2 gl) {
		super.prerender(gl);
		double diameter = this.getDiameter();
		double color[] = this.getColor();
		gl.glScaled(
			diameter,
			diameter,
			diameter
		);
		gl.glColor4d(color[0],color[1],color[2],color[3]);
		gl.glCallList(displaylist);
	}

	@Override
	public void close(GL2 gl) {
		super.close(gl);
		gl.glDeleteLists(displaylist,1);
	}
	
	public int getRecursion() {
		return this.getParamInt(PARAM_RECURSION);
	}
	
	/** does not allow <code>recursion</code> values outside [0 .. 5]. */
	public void setRecursion(int recursion) {
		this.setParam(
			PARAM_RECURSION,
			Math.max(0,Math.min(recursion,5))
		);
	}

	/** expects a four component color vector with r, g, b and alpha channels. */
	public void setColor(double[] color) {
		this.setParam(PARAM_COLOR,color);
	}
	
	public double[] getColor() {
		return this.getParamDoubleArray(PARAM_COLOR);
	}
}
