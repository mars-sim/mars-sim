package org.mars_sim.msp.ui.ogl.sandbox.scene.sphere;

import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;

import java.io.IOException;
import java.net.URL;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import jogamp.opengl.glu.GLUquadricImpl;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class SphereTextured
extends SphereAbstract {

	protected GLU glu;
	protected Texture texture;
	protected int displaylist;

	/**
	 * @param center {@link double}[] expects a three dimensional vector.
	 * @param rotation {@link double}[] expects a three dimensional vector.
	 * @param deltaRotation {@link double}[] expects a three dimensional vector.
	 * @param radius {@link double} should be greater or equal zero
	 * @param texture {@link String} path to a texture file
	 */
	public SphereTextured(
		double[] center,
		double[] rotation,
		double[] deltaRotation,
		double radius,
		String texture,
		double[] color
	) {
		super(center,rotation,deltaRotation,radius);
		this.setTexture(texture);
		this.setColor(color);
		this.glu = new GLU();
	}

	public final void setTexture(String texture) {
		this.setParam(PARAM_TEXTURE,texture);
	}

	public final String getTexture() {
		return this.getParamString(PARAM_TEXTURE);
	}

	@Override
	public void preinit(GL2 gl) {
		// loading textures was taken from some nehe tutorial.
		// Load texture from image
		try {
			// Create a OpenGL Texture object from (URL, mipmap, file suffix)
			// Use URL so that can read from JAR and disk file.
			Class<?> c = getClass();
			ClassLoader cl = c.getClassLoader();
			String textureFile = getTexture();
			URL url = cl.getResource(textureFile);
			String textureFileType = textureFile.substring(textureFile.lastIndexOf("."));
			texture = TextureIO.newTexture(
				url, // relative to project root 
				false,
				textureFileType
			);

			// Use linear filter for texture if image is larger than the original texture
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			// Use linear filter for texture if image is smaller than the original texture
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		} catch (GLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// taken from http://www.opengl.org/wiki/Texturing_a_Sphere#Java
		
		GLUquadric sphere = new GLUquadricImpl(gl,false,null,0);
		glu.gluQuadricDrawStyle(sphere, GLU.GLU_FILL);
		glu.gluQuadricTexture(sphere, true);
		glu.gluQuadricNormals(sphere, GLU.GLU_SMOOTH);
		// Making a display list
		displaylist = gl.glGenLists(1);
		gl.glNewList(displaylist, GL2.GL_COMPILE);
		texture.enable(gl);
		texture.bind(gl);
		glu.gluSphere(sphere, 1.0d, 32, 32);
		texture.disable(gl);
		gl.glEndList();
		glu.gluDeleteQuadric(sphere);
	}

	@Override
	protected void prerender(GL2 gl) {
		super.prerender(gl);
		double diameter = this.getDiameter();
		double color[] = this.getColor();
		gl.glScaled(
			diameter,
			diameter,
			diameter
		);
		gl.glColor4d(
			color[0],
			color[1],
			color[2],
			color[3]
		);
		// Bind the texture to the current OpenGL graphics context.
		texture.bind(gl);
		texture.enable(gl);
		gl.glCallList(displaylist);
	}

	@Override
	public void close(GL2 gl) {
		super.close(gl);
		gl.glDeleteLists(displaylist,1);
		texture.destroy(gl);
	}

	public void setColor(double[] color) {
		this.setParam(PARAM_COLOR,color);
	}

	public double[] getColor() {
		return this.getParamDoubleArray(PARAM_COLOR);
	}
}
