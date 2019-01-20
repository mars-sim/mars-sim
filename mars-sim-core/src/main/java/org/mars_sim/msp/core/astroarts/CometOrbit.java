/**
 * CometOrbit Class
 */
package org.mars_sim.msp.core.astroarts;

import org.mars_sim.msp.core.astroarts.Xyz;

public class CometOrbit {

	private Xyz orbit[];			// actual orbit data
	private int nDivision;			// number of division

	static private final double fMaxOrbit = 90.0;
	static private final double fTolerance = 1.0e-16;

	/**
	 *  Elliptical Orbit
	 */
	private void GetOrbitEllip(Comet comet) {
		double fAxis = comet.getQ() / (1.0 - comet.getE());
		double fae2 = -2.0 * fAxis * comet.getE();
		double ft = Math.sqrt(1.0 - comet.getE() * comet.getE());
		if (fAxis * (1.0 + comet.getE()) > fMaxOrbit) {
			double fdE = Math.acos((1.0 - fMaxOrbit / fAxis) / comet.getE())
				/ ((this.nDivision / 2) * (this.nDivision / 2));
			int nIdx1, nIdx2;
			nIdx1 = nIdx2 = this.nDivision / 2;
			for (int i = 0; i <= (this.nDivision / 2); i++) {
				double fE = fdE * i * i;
				double fRCosV = fAxis * (Math.cos(fE) - comet.getE());
				double fRSinV = fAxis * ft * Math.sin(fE);
				orbit[nIdx1++] = new Xyz(fRCosV,  fRSinV, 0.0);
				orbit[nIdx2--] = new Xyz(fRCosV, -fRSinV, 0.0);
			}
		} else {
			int nIdx1, nIdx2, nIdx3, nIdx4;
			nIdx1 = 0;
			nIdx2 = nIdx3 = this.nDivision / 2;
			nIdx4 = this.nDivision;
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
	 * Hyperbolic Orbit
	 */
	private void GetOrbitHyper(Comet comet) {
		int nIdx1, nIdx2;
		nIdx1 = nIdx2 = this.nDivision / 2;
		double ft = Math.sqrt(comet.getE() * comet.getE() - 1.0);
		double fAxis = comet.getQ() / (comet.getE() - 1.0);
		double fdF = UdMath.arccosh((fMaxOrbit + fAxis)
					 / (fAxis * comet.getE())) / (this.nDivision / 2);
		double fF = 0.0;
		for (int i = 0; i <= (this.nDivision / 2); i++, fF += fdF) {
			double fRCosV = fAxis * (comet.getE() - UdMath.cosh(fF));
			double fRSinV = fAxis * ft * UdMath.sinh(fF);
			orbit[nIdx1++] = new Xyz(fRCosV,  fRSinV, 0.0);
			orbit[nIdx2--] = new Xyz(fRCosV, -fRSinV, 0.0);
		}
	}

	/**
	 * Parabolic Orbit
	 */
	private void GetOrbitPara(Comet comet) {
		int nIdx1, nIdx2;
		nIdx1 = nIdx2 = nDivision / 2;
		double fdV = (Math.atan(Math.sqrt(fMaxOrbit / comet.getQ() - 1.0))
					  * 2.0) / (nDivision / 2);
		double fV = 0.0;
		for (int i = 0; i <= (nDivision / 2); i++, fV += fdV) {
			double fTanV2 = Math.sin(fV / 2.0) / Math.cos(fV / 2.0);
			double fRCosV = comet.getQ() * (1.0 - fTanV2 * fTanV2);
			double fRSinV = 2.0 * comet.getQ() * fTanV2;
			orbit[nIdx1++] = new Xyz(fRCosV,  fRSinV, 0.0);
			orbit[nIdx2--] = new Xyz(fRCosV, -fRSinV, 0.0);
		}
	}
	
	/**
	 * Constructor
	 */
	public CometOrbit(Comet comet, int nDivision) {
		this.nDivision = nDivision;
		orbit = new Xyz[nDivision+1];
		if (comet.getE() < 1.0 - fTolerance) {
			GetOrbitEllip(comet);
		} else if (comet.getE() > 1.0 + fTolerance) {
			GetOrbitHyper(comet);
		} else {
			GetOrbitPara(comet);
		}
		
		Matrix vec = comet.getVectorConstant();
		Matrix prec = Matrix.PrecMatrix(comet.getEquinoxJd(), Astro.JD2000);
		for (int i = 0; i <= nDivision; i++) {
			orbit[i] = orbit[i].Rotate(vec).Rotate(prec);
		}
	}

	/**
	 * Get Division Count
	 */
	public int getDivision() {
		return nDivision;
	}

	/**
	 * Get Orbit Point
	 */
	public Xyz getAt(int nIndex) {
		return orbit[nIndex];
	}
}
