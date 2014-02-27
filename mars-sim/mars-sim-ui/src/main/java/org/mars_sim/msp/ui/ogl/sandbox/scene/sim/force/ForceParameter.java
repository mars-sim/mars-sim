package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force;

/**
 * @author stpa
 * 2014-02-24
 */
public class ForceParameter {

	protected String name;
	protected Double minValue;
	protected Double maxValue;
	protected Double value;
	
	public ForceParameter(String name, Double minValue, Double maxValue, Double value) {
		this.name = name;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.value = value;
	}

	public Double getMinValue() {
		return minValue;
	}

	public Double getMaxValue() {
		return maxValue;
	}

	public Double getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}

	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public void setName(String name) {
		this.name = name;
	}
}
