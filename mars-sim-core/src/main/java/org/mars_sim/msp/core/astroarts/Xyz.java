/**
 * 3-Dimensional Vector
 */
package org.mars_sim.msp.core.astroarts;

import org.mars_sim.msp.core.astroarts.Matrix;

public class Xyz {
	
	public double fX, fY, fZ;

	/**
	 * Default Constructor
	 */
	public Xyz() {
		this.fX = this.fY = this.fZ = 0.0;
	}

	/**
	 * Constructor with Initializer
	 */
	public Xyz(double fX, double fY, double fZ) {
		this.fX = fX;
		this.fY = fY;
		this.fZ = fZ;
	}

	/**
	 * Rotation of Vector with Matrix
	 */
	public Xyz Rotate(Matrix mtx) {
		double fX = mtx.fA11 * this.fX + mtx.fA12 * this.fY
			+ mtx.fA13 * this.fZ;
		double fY = mtx.fA21 * this.fX + mtx.fA22 * this.fY
			+ mtx.fA23 * this.fZ;
		double fZ = mtx.fA31 * this.fX + mtx.fA32 * this.fY
			+ mtx.fA33 * this.fZ;
		return new Xyz(fX, fY, fZ);
	}

	/**
	 * V := V1 + V2
	 */
	public Xyz Add(Xyz xyz) {
		double fX = this.fX + xyz.fX;
		double fY = this.fY + xyz.fY;
		double fZ = this.fZ + xyz.fZ;
		return new Xyz(fX, fY, fZ);
	}

	/**
	 * V := V1 - V2
	 */
	public Xyz Sub(Xyz xyz) {
		double fX = this.fX - xyz.fX;
		double fY = this.fY - xyz.fY;
		double fZ = this.fZ - xyz.fZ;
		return new Xyz(fX, fY, fZ);
	}

	/**
	 * V := x * V;
	 */
	public Xyz Mul(double fA) {
		double fX = this.fX * fA;
		double fY = this.fY * fA;
		double fZ = this.fZ * fA;
		return new Xyz(fX, fY, fZ);
	}

	/**
	 * x := abs(V);
	 */
	public double Abs() {
		return Math.sqrt(this.fX * this.fX
					   + this.fY * this.fY
					   + this.fZ * this.fZ);
	}
}
