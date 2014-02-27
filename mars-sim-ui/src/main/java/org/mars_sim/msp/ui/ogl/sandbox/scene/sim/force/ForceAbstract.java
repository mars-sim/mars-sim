package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force;

import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;

/**
 * @author stpa
 * 2014-02-24
 */
public abstract class ForceAbstract
extends ParametrizedAbstract
implements ForceInterface {

	protected boolean active;
	
	public void init(Simulation sim) {
		// do nothing
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(this.getName());
		s.append("\n");
		Double d;
		for (ForceParameter parameter : this.getParameters()) {
			s.append(parameter.getName());
			s.append(" [min:");
			d = parameter.getMinValue();
			if (d != null) {
				s.append(Double.toString(d));
			} else {
				s.append("null");
			}
			s.append("; max:");
			d = parameter.getMaxValue();
			if (d != null) {
				s.append(Double.toString(d));
			} else {
				s.append("null");
			}
			s.append("; val:");
			d = parameter.getValue();
			if (d != null) {
				s.append(Double.toString(d));
			} else {
				s.append("null");
			}
			s.append("]\n");
		}
		return s.toString();
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
}
