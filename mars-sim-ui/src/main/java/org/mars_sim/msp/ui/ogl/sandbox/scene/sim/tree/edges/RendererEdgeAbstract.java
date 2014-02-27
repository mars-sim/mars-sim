package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.edges;

import javax.media.opengl.GL2;

import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.SimulationContainer;

/**
 * @author stpa
 * 2014-02-24
 */
public abstract class RendererEdgeAbstract
implements RendererEdgeInterface {

	protected GL2 gl;

	/**
	 * called from {@link SimulationContainer#preinit(GL2)}.
	 */
	public void init(Simulation sim) {
		// do nothing.
	}

	/** called before iteration through force objects. */
	public void postrender() {} // do nothing.

	/** called after iteration through force objects. */
	public void prerender() {} // do nothing.

	public void update(long deltaTime) {} // do nothing.

	public void setGl(GL2 gl) {
		this.gl = gl;
	}
}
