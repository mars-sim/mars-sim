package org.mars_sim.msp.ui.ogl.sandbox.lsys;

/**
 * @author stpa
 */
public class Rule {

	protected String source;
	protected String target;
	protected double probability;
	
	public Rule(String source,String target,double probability) {
		this.source = source;
		this.target = target;
		this.probability = probability;
	}

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	public double getProbability() {
		return probability;
	}
	
}
