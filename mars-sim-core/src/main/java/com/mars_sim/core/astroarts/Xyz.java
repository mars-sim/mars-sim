/*
 * Mars Simulation Project
 * Xyz.java
 * @date 2021-06-20
 * @author Manny Kung
 */

/**
 * 3-Dimensional Vector
 */
package com.mars_sim.core.astroarts;

public class Xyz {
	
	public double fX;
	public double fY;
	public double fZ;

	/**
	 * Default Constructor.
	 */
	public Xyz() {
		this.fX = this.fY = this.fZ = 0.0;
	}

	/**
	 * Constructor with Initializer.
	 */
	public Xyz(double fX, double fY, double fZ) {
		this.fX = fX;
		this.fY = fY;
		this.fZ = fZ;
	}

	/**
	 * Rotation of Vector with Matrix
	 */
	public Xyz rotate(Matrix mtx) {
		double newFx = mtx.fA11 * this.fX + mtx.fA12 * this.fY
			+ mtx.fA13 * this.fZ;
		double newfY = mtx.fA21 * this.fX + mtx.fA22 * this.fY
			+ mtx.fA23 * this.fZ;
		double newfZ = mtx.fA31 * this.fX + mtx.fA32 * this.fY
			+ mtx.fA33 * this.fZ;
		return new Xyz(newFx, newfY, newfZ);
	}

	/**
	 * V := V1 + V2.
	 */
	public Xyz add(Xyz xyz) {
		double newfX = this.fX + xyz.fX;
		double newfY = this.fY + xyz.fY;
		double newfZ = this.fZ + xyz.fZ;
		return new Xyz(newfX, newfY, newfZ);
	}

	/**
	 * V := V1 - V2.
	 */
	public Xyz sub(Xyz xyz) {
		double newfX = this.fX - xyz.fX;
		double newfY = this.fY - xyz.fY;
		double newfZ = this.fZ - xyz.fZ;
		return new Xyz(newfX, newfY, newfZ);
	}

	/**
	 * V := x * V.
	 */
	public Xyz mul(double fA) {
		double newfX = this.fX * fA;
		double newfY = this.fY * fA;
		double newfZ = this.fZ * fA;
		return new Xyz(newfX, newfY, newfZ);
	}

	/**
	 * x := abs(V).
	 */
	public double abs() {
		return Math.sqrt(this.fX * this.fX
					   + this.fY * this.fY
					   + this.fZ * this.fZ);
	}
}
