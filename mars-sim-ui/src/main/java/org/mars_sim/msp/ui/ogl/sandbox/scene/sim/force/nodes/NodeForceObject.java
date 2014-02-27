package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes;

import java.util.Arrays;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;

/**
 * @author stpa
 * 2014-02-24
 */
public class NodeForceObject {

	protected static final boolean DEFAULT_FIXED = false;
	protected static final double DEFAULT_MASS = 1.0f;
	protected static final double[] DEFAULT_VECTOR = Util.nul3();
	
	protected double mass;
	protected double[] position;
	protected double[] velocity;
	protected double[] acceleration;
	protected boolean fixed;

	/**
	 * constructor.
	 * @param mass
	 * @param position
	 * @param velocity
	 * @param acceleration
	 * @param fixed
	 */
	public NodeForceObject(double mass, double[] position, double[] velocity, double[] acceleration, boolean fixed) {
		this.acceleration = new double[acceleration.length];
		this.position = new double[position.length];
		this.velocity = new double[velocity.length];
		this.setMass(mass);
		this.setPosition(position);
		this.setVelocity(velocity);
		this.setAcceleration(acceleration);
		this.setFixed(fixed);
	}

	/**
	 * constructor.
	 * @param position
	 * @param velocity
	 * @param acceleration
	 * @param fixed
	 */
	public NodeForceObject(double[] position, double[] velocity, double[] acceleration, boolean fixed) {
		this(DEFAULT_MASS,position,velocity,acceleration,fixed);
	}

	/**
	 * constructor.
	 * @param position
	 */
	public NodeForceObject(double[] position) {
		this(DEFAULT_MASS,position,DEFAULT_VECTOR,DEFAULT_VECTOR,DEFAULT_FIXED);
	}

	/**
	 * constructor.
	 * @param position
	 * @param fixed
	 */
	public NodeForceObject(double[] position, boolean fixed) {
		this(DEFAULT_MASS,position,DEFAULT_VECTOR,DEFAULT_VECTOR,fixed);
	}

	/**
	 * constructor.
	 * @param fixed
	 */
	public NodeForceObject(boolean fixed) {
		this(DEFAULT_MASS,DEFAULT_VECTOR,DEFAULT_VECTOR,DEFAULT_VECTOR,fixed);
	}

	/**
	 * constructor.
	 */
	public NodeForceObject() {
		this(DEFAULT_MASS,DEFAULT_VECTOR,DEFAULT_VECTOR,DEFAULT_VECTOR,DEFAULT_FIXED);
	}

	public double getMass() {
		return mass;
	}

	public double[] getPosition() {
		return position;
	}

	public double[] getVelocity() {
		return velocity;
	}

	public double[] getAcceleration() {
		return acceleration;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public void setPosition(double[] position) {
		for (int i = 0; i < position.length; i++) {
			this.position[i] = position[i];
		}
	}

	public void setVelocity(double[] velocity) {
		for (int i = 0; i < velocity.length; i++) {
			this.velocity[i] = velocity[i];
		}
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public void setAcceleration(double[] force) {
		for (int i = 0; i < force.length; i++) {
			this.acceleration[i] = force[i];
		}
	}

	public boolean isFixed() {
		return this.fixed;
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(Util.toString(acceleration));
		s.append(",");
		s.append(Util.toString(position));
		s.append(",");
		s.append(Util.toString(velocity));
		return s.toString();
	}

	public void nullForce() {
		Arrays.fill(this.acceleration,0);
	}
}
