package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stpa
 * 2014-02-24
 */
public class ParametrizedAbstract
implements ParametrizedInterface {

	protected List<ForceParameter> parameters = new ArrayList<ForceParameter>();
	
	public void add(ForceParameter parameter) {
		this.parameters.add(parameter);
	}

	public List<ForceParameter> getParameters() {
		return this.parameters;
	}
	
	public ForceParameter get(int parameterIndex) {
		return this.parameters.get(parameterIndex);
	}
	
	public void set(int parameterIndex, double value) {
		this.parameters.get(parameterIndex).setValue(value);
	}
}
