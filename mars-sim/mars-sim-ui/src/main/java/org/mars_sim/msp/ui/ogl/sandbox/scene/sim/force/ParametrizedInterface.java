package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force;

import java.util.List;

/**
 * @author stpa
 * 2014-02-24
 */
public interface ParametrizedInterface {

	public List<ForceParameter> getParameters();
	public void add(ForceParameter parameter);
	public void set(int parameterIndex, double value);
	public ForceParameter get(int parameterIndex);
}
