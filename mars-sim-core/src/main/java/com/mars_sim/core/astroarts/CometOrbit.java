/*
 * Mars Simulation Project
 * CometOrbit.java
 * @date 2021-06-20
 * @author Manny Kung
 */

/**
 * CometOrbit Class.
 */
package com.mars_sim.core.astroarts;

public class CometOrbit {

	private Xyz[] orbit;			// actual orbit data
	private int nDivision;			// number of division

	private static final double MAX_ORBIT = 90.0;
	private static final double TOLERANCE = 1.0e-16;

	/**
	 * Constructor.
	 */
	public CometOrbit(Comet comet, int nDivision) {
		this.nDivision = nDivision;
		orbit = new Xyz[nDivision+1];
		if (comet.getE() < 1.0 - TOLERANCE) {
			getOrbitEllop(comet);
		} else if (comet.getE() > 1.0 + TOLERANCE) {
			getOrbitHyper(comet);
		} else {
			getOrbitPara(comet);
		}

		Matrix vec = comet.getVectorConstant();
		Matrix prec = Matrix.getPrecMatrix(comet.getEquinoxJd(), Astro.JD2000);
		for (int i = 0; i <= nDivision; i++) {
			orbit[i] = orbit[i].rotate(vec).rotate(prec);
		}
	}


	/**
	 * Gets the Elliptical Orbit.
	 */
	private void getOrbitEllop(Comet comet) {
		double fAxis = comet.getQ() / (1.0 - comet.getE());
		double fae2 = -2.0 * fAxis * comet.getE();
		double ft = Math.sqrt(1.0 - comet.getE() * comet.getE());
		if (fAxis * (1.0 + comet.getE()) > MAX_ORBIT) {
			double fdE = Math.acos((1.0 - MAX_ORBIT / fAxis) / comet.getE())
				/ (this.nDivision * this.nDivision / 4.0);
			int nIdx2;
			int nIdx1 = nIdx2 = this.nDivision / 2;
			for (int i = 0; i <= nIdx1; i++) {
				double fE = fdE * i * i;
				double fRCosV = fAxis * (Math.cos(fE) - comet.getE());
				double fRSinV = fAxis * ft * Math.sin(fE);
				orbit[nIdx1++] = new Xyz(fRCosV,  fRSinV, 0.0);
				orbit[nIdx2--] = new Xyz(fRCosV, -fRSinV, 0.0);
			}
		} else {
			int nIdx3;
			int nIdx1 = 0;
			int nIdx2 = nIdx3 = this.nDivision / 2;
			int nIdx4 = this.nDivision;
			double fE = 0.0;
			for (int i = 0; i <= (this.nDivision / 4);
				 i++, fE += (2.0 * Math.PI / this.nDivision)) {
				double fRCosV = fAxis * (Math.cos(fE) - comet.getE());
				double fRSinV = fAxis * ft * Math.sin(fE);
				orbit[nIdx1++] = new Xyz(fRCosV,         fRSinV, 0.0);
				orbit[nIdx2--] = new Xyz(fae2 - fRCosV,  fRSinV, 0.0);
				orbit[nIdx3++] = new Xyz(fae2 - fRCosV, -fRSinV, 0.0);
				orbit[nIdx4--] = new Xyz(fRCosV,        -fRSinV, 0.0);
			}
		}
	}

	/**
	 * Gets the Hyperbolic Orbit.
	 */
	private void getOrbitHyper(Comet comet) {
		int nIdx2;
		int nIdx1 = nIdx2 = this.nDivision / 2;
		double ft = Math.sqrt(comet.getE() * comet.getE() - 1.0);
		double fAxis = comet.getQ() / (comet.getE() - 1.0);
		double fdF = UdMath.arccosh((MAX_ORBIT + fAxis)
					 / (fAxis * comet.getE())) / (nDivision / 2.0);
		double fF = 0.0;
		for (int i = 0; i <= nIdx1; i++, fF += fdF) {
			double fRCosV = fAxis * (comet.getE() - UdMath.cosh(fF));
			double fRSinV = fAxis * ft * UdMath.sinh(fF);
			orbit[nIdx1++] = new Xyz(fRCosV,  fRSinV, 0.0);
			orbit[nIdx2--] = new Xyz(fRCosV, -fRSinV, 0.0);
		}
	}

	/**
	 * Gets the Parabolic Orbit.
	 */
	private void getOrbitPara(Comet comet) {
		int nIdx2;
		int nIdx1 = nIdx2 = nDivision / 2;
		double fdV = (Math.atan(Math.sqrt(MAX_ORBIT / comet.getQ() - 1.0))
					  * 2.0) / (nDivision / 2.0);
		double fV = 0.0;
		for (int i = 0; i <= nIdx1; i++, fV += fdV) {
			double fTanV2 = Math.sin(fV / 2.0) / Math.cos(fV / 2.0);
			double fRCosV = comet.getQ() * (1.0 - fTanV2 * fTanV2);
			double fRSinV = 2.0 * comet.getQ() * fTanV2;
			orbit[nIdx1++] = new Xyz(fRCosV,  fRSinV, 0.0);
			orbit[nIdx2--] = new Xyz(fRCosV, -fRSinV, 0.0);
		}
	}

	/**
	 * Gets Division Count.
	 */
	public int getDivision() {
		return nDivision;
	}

	/**
	 * Gets Orbit Point.
	 */
	public Xyz getAt(int nIndex) {
		return orbit[nIndex];
	}
}
