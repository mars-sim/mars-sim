/**
 * Comet Class
 */
package org.mars_sim.msp.core.astroarts;

public class Comet {
	private String	strName;
	private double	fT, fE, fQ;
	private double	fPeri, fNode, fIncl;
	private double	fEquinox;
	private ATime	atimeEquinox;
	private Matrix	mtxVC;		// Vector Constant

	private static final double TOLERANCE = 1.0e-12;
	private static final int    MAXAPPROX = 80;

	/**
	 * Constructor
	 */
	public Comet(String strName, double fT, double fE, double fQ,
				 double fPeri, double fNode, double fIncl,
				 double fEquinox) {
		this.strName = strName;
		this.fT = fT;
		this.fE = fE;
		this.fQ = fQ;
		this.fPeri = fPeri;
		this.fNode = fNode;
		this.fIncl = fIncl;
		this.fEquinox = fEquinox;	// ex. 2000.0
		// Equinox -> ATime
		int    nEqnxYear  = (int)Math.floor(fEquinox);
		double fEqnxMonth = (fEquinox - (double)nEqnxYear) * 12.0;
		int    nEqnxMonth = (int)Math.floor(fEqnxMonth);
		double fEqnxDay   = (fEqnxMonth - (double)nEqnxMonth) * 30.0;
		this.atimeEquinox = new ATime(nEqnxYear, nEqnxMonth, fEqnxDay, 0.0);
		// Vector Constant
		mtxVC = Matrix.VectorConstant(fPeri, fNode, fIncl, atimeEquinox);
	}

	/**
	 * Get Position on Orbital Plane for Elliptical Orbit
	 */
	private Xyz CometStatusEllip(double fJd) {
		if (this.fQ == 0.0) {
			throw new ArithmeticException();
		}
		double fAxis = this.fQ / (1.0 - this.fE);
		double fM = Astro.GAUSS * (fJd - this.fT) / (Math.sqrt(fAxis) * fAxis);
		double fE1 = fM + this.fE * Math.sin(fM);
		int nCount = MAXAPPROX;
		if (this.fE < 0.6) {
			double fE2;
			do {
				fE2 = fE1;
				fE1 = fM + this.fE * Math.sin(fE2);
			} while (Math.abs(fE1 - fE2) > TOLERANCE && --nCount > 0);
		} else {
			double fDv;
			do {
				double fDv1 = (fM + this.fE * Math.sin(fE1) - fE1);
				double fDv2 = (1.0 - this.fE * Math.cos(fE1));
				if (Math.abs(fDv1) < TOLERANCE || Math.abs(fDv2) < TOLERANCE) {
					break;
				}
				fDv = fDv1 / fDv2;
				fE1 += fDv;
			} while (Math.abs(fDv) > TOLERANCE && --nCount > 0);
		}
		if (nCount == 0) {
			throw new ArithmeticException();
		}
		double fX = fAxis * (Math.cos(fE1) - this.fE);
		double fY = fAxis * Math.sqrt(1.0 - this.fE * this.fE) * Math.sin(fE1);

		return new Xyz(fX, fY, 0.0);
	}

	/**
	 * Get Position on Orbital Plane for Parabolic Orbit
	 */
	private Xyz CometStatusPara(double fJd) {
		if (this.fQ == 0.0) {
			throw new ArithmeticException();
		}
		double fN = Astro.GAUSS * (fJd - this.fT)
			/ (Math.sqrt(2.0) * this.fQ * Math.sqrt(this.fQ));
		double fTanV2 = fN;
		double fOldTanV2, fTan2V2;
		int nCount = MAXAPPROX;
		do {
			fOldTanV2 = fTanV2;
			fTan2V2 = fTanV2 * fTanV2;
			fTanV2 = (fTan2V2 * fTanV2 * 2.0 / 3.0 + fN) / (1.0 + fTan2V2);
		} while (Math.abs(fTanV2 - fOldTanV2) > TOLERANCE && --nCount > 0);
		if (nCount == 0) {
			throw new ArithmeticException();
		}
		fTan2V2 = fTanV2 * fTanV2;
		double fX = this.fQ * (1.0 - fTan2V2);
		double fY = 2.0 * this.fQ * fTanV2;

		return new Xyz(fX, fY, 0.0);
	}

	/**
	 * Get Position on Orbital Plane for Nearly Parabolic Orbit
	 */
	private Xyz CometStatusNearPara(double fJd) {
		if (this.fQ == 0.0) {
			throw new ArithmeticException();
		}
		double fA = Math.sqrt((1.0 + 9.0 * this.fE) / 10.0);
		double fB = 5.0 * (1 - this.fE) / (1.0 + 9.0 * this.fE);
		double fA1, fB1, fX1, fA0, fB0, fX0, fN;
		fA1 = fB1 = fX1 = 1.0;
		int nCount1 = MAXAPPROX;
		do {
			fA0 = fA1;
			fB0 = fB1;
			fN = fB0 * fA * Astro.GAUSS * (fJd - this.fT)
				/ (Math.sqrt(2.0) * this.fQ * Math.sqrt(this.fQ));
			int nCount2 = MAXAPPROX;
			do {
				fX0 = fX1;
				double fTmp = fX0 * fX0;
				fX1 = (fTmp * fX0 * 2.0 / 3.0 + fN) / (1.0 + fTmp);
			} while (Math.abs(fX1 - fX0) > TOLERANCE && --nCount2 > 0);
			if (nCount2 == 0) {
				throw new ArithmeticException();
			}
			fA1 = fB * fX1 * fX1;
			fB1 = (-3.809524e-03 * fA1 - 0.017142857) * fA1 * fA1 + 1.0;
		} while (Math.abs(fA1 - fA0) > TOLERANCE && --nCount1 > 0);
		if (nCount1 == 0) {
			throw new ArithmeticException();
		}
		double fC1 = ((0.12495238 * fA1 + 0.21714286) * fA1 + 0.4) * fA1 + 1.0;
		double fD1 = ((0.00571429 * fA1 + 0.2       ) * fA1 - 1.0) * fA1 + 1.0;
		double fTanV2 = Math.sqrt(5.0 * (1.0 + this.fE)
				  / (1.0 + 9.0 * this.fE)) * fC1 * fX1;
		double fX = this.fQ * fD1 * (1.0 - fTanV2 * fTanV2);
		double fY = 2.0 * this.fQ * fD1 * fTanV2;
		return new Xyz(fX, fY, 0.0);
	}

	/**
	 * Get Position in Heliocentric Equatorial Coordinates 2000.0
	 */
	public Xyz GetPos(double fJd) {
		Xyz xyz;
		// CometStatus' may be throw ArithmeticException
		if (this.fE < 0.98) {
			xyz = CometStatusEllip(fJd);
		} else if (Math.abs(this.fE - 1.0) < TOLERANCE) {
			xyz = CometStatusPara(fJd);
		} else {
			xyz = CometStatusNearPara(fJd);
		}
		xyz = xyz.Rotate(mtxVC);
		Matrix mtxPrec = Matrix.PrecMatrix(this.atimeEquinox.getJd(),
										   Astro.JD2000);
		return xyz.Rotate(mtxPrec);
	}

	/**
	 * Get Internal Variables
	 */
	public String getName() {
		return this.strName;
	}
	public double getT() {
		return this.fT;
	}
	public double getE() {
		return this.fE;
	}
	public double getQ() {
		return this.fQ;
	}
	public double getPeri() {
		return this.fPeri;
	}
	public double getNode() {
		return this.fNode;
	}
	public double getIncl() {
		return this.fIncl;
	}
	public double getEquinox() {
		return this.fEquinox;
	}
	public double getEquinoxJd() {
		return this.atimeEquinox.getJd();
	}
	public Matrix getVectorConstant() {
		return this.mtxVC;
	}
}
