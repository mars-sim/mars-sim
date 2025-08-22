/*
 * Mars Simulation Project
 * Matrix.java
 * @date 2021-06-20
 * @author Manny Kung
 */


package com.mars_sim.core.astroarts;

/**
 * Matrix (3x3)
 */
public class Matrix {
	double fA11;
	double fA12;
	double fA13;
	double fA21;
	double fA22;
	double fA23;
	double fA31;
	double fA32;
	double fA33;

	/**
	 * Default Constructor.
	 */
	public Matrix() {
		this.fA11 = this.fA12 = this.fA13 =
		this.fA21 = this.fA22 = this.fA23 =
		this.fA31 = this.fA32 = this.fA33 = 0.0;
	}

	/**
	 * Constructor with Initializer.
	 */
	public Matrix(double fA11, double fA12, double fA13,
				  double fA21, double fA22, double fA23,
				  double fA31, double fA32, double fA33) {
		this.fA11 = fA11;	this.fA12 = fA12;	this.fA13 = fA13;
		this.fA21 = fA21;	this.fA22 = fA22;	this.fA23 = fA23;
		this.fA31 = fA31;	this.fA32 = fA32;	this.fA33 = fA33;
	}

	/**
	 * Gets Multiplication of Matrix.
	 */
	public Matrix mul(Matrix mtx) {
		double newfA11 = this.fA11 * mtx.fA11 + this.fA12 * mtx.fA21
			+ this.fA13 * mtx.fA31;
		double newfA21 = this.fA21 * mtx.fA11 + this.fA22 * mtx.fA21
			+ this.fA23 * mtx.fA31;
		double newfA31 = this.fA31 * mtx.fA11 + this.fA32 * mtx.fA21
			+ this.fA33 * mtx.fA31;

		double newfA12 = this.fA11 * mtx.fA12 + this.fA12 * mtx.fA22
			+ this.fA13 * mtx.fA32;
		double newfA22 = this.fA21 * mtx.fA12 + this.fA22 * mtx.fA22
			+ this.fA23 * mtx.fA32;
		double newfA32 = this.fA31 * mtx.fA12 + this.fA32 * mtx.fA22
			+ this.fA33 * mtx.fA32;

		double newfA13 = this.fA11 * mtx.fA13 + this.fA12 * mtx.fA23
			+ this.fA13 * mtx.fA33;
		double newfA23 = this.fA21 * mtx.fA13 + this.fA22 * mtx.fA23
			+ this.fA23 * mtx.fA33;
		double newfA33 = this.fA31 * mtx.fA13 + this.fA32 * mtx.fA23
			+ this.fA33 * mtx.fA33;

		return new Matrix(newfA11, newfA12, newfA13,
						  newfA21, newfA22, newfA23,
						  newfA31, newfA32, newfA33);
	}

	/**
	 * Gets Multiplication of Matrix by double.
	 */
	public Matrix mul(double fX) {
		double newfA11 = this.fA11 * fX;
		double newfA21 = this.fA21 * fX;
		double newfA31 = this.fA31 * fX;

		double newfA12 = this.fA12 * fX;
		double newfA22 = this.fA22 * fX;
		double newfA32 = this.fA32 * fX;

		double newfA13 = this.fA13 * fX;
		double newfA23 = this.fA23 * fX;
		double newfA33 = this.fA33 * fX;

		return new Matrix(newfA11, newfA12, newfA13,
						  newfA21, newfA22, newfA23,
						  newfA31, newfA32, newfA33);
	}

	/**
	 * Creates Rotation Matrix Around X-Axis.
	 */
	public static Matrix rotateX(double fAngle) {
		double fA11 =  1.0;
		double fA12 =  0.0;
		double fA13 =  0.0;
		double fA21 =  0.0;
		double fA22 =  Math.cos(fAngle);
		double fA23 =  Math.sin(fAngle);
		double fA31 =  0.0;
		double fA32 = -Math.sin(fAngle);
		double fA33 =  Math.cos(fAngle);

		return new Matrix(fA11, fA12, fA13,
						  fA21, fA22, fA23,
						  fA31, fA32, fA33);
	}

	/**
	 *  Creates Rotation Matrix Around Y-Axis.
	 */
	public static Matrix rotateY(double fAngle) {
		double fA11 =  Math.cos(fAngle);
		double fA12 =  0.0;
		double fA13 = -Math.sin(fAngle);
		double fA21 =  0.0;
		double fA22 =  1.0;
		double fA23 =  0.0;
		double fA31 =  Math.sin(fAngle);
		double fA32 =  0.0;
		double fA33 =  Math.cos(fAngle);

		return new Matrix(fA11, fA12, fA13,
						  fA21, fA22, fA23,
						  fA31, fA32, fA33);
	}

	/**
	 * Creates Rotation Matrix Around Z-Axis.
	 */
	public static Matrix rotateZ(double fAngle) {
		double fA11 =  Math.cos(fAngle);
		double fA12 =  Math.sin(fAngle);
		double fA13 =  0.0;
		double fA21 = -Math.sin(fAngle);
		double fA22 =  Math.cos(fAngle);
		double fA23 =  0.0;
		double fA31 =  0.0;
		double fA32 =  0.0;
		double fA33 =  1.0;

		return new Matrix(fA11, fA12, fA13,
						  fA21, fA22, fA23,
						  fA31, fA32, fA33);
	}

	/**
	 * Inverts Matrix.
	 */
	public void invert() {
		double fA = 1.0 /
			(this.fA11 * (this.fA22 * this.fA33 - this.fA23 * this.fA32)
		   - this.fA12 * (this.fA21 * this.fA33 - this.fA23 * this.fA31)
		   + this.fA13 * (this.fA21 * this.fA32 - this.fA22 * this.fA31));
	
		double newfA11 =  1.0 * fA
			* (this.fA22 * this.fA33 - this.fA23 * this.fA32);
		double newfA12 = -1.0 * fA
			* (this.fA12 * this.fA33 - this.fA13 * this.fA32);
		double newfA13 =  1.0 * fA
			* (this.fA12 * this.fA23 - this.fA13 * this.fA22);
	
		double newfA21 = -1.0 * fA
			* (this.fA21 * this.fA33 - this.fA23 * this.fA31);
		double newfA22 =  1.0 * fA
			* (this.fA11 * this.fA33 - this.fA13 * this.fA31);
		double newfA23 = -1.0 * fA
			* (this.fA11 * this.fA23 - this.fA13 * this.fA21);
	
		double newfA31 =  1.0 * fA
			* (this.fA21 * this.fA32 - this.fA22 * this.fA31);
		double newfA32 = -1.0 * fA
			* (this.fA11 * this.fA32 - this.fA12 * this.fA31);
		double newfA33 =  1.0 * fA
			* (this.fA11 * this.fA22 - this.fA12 * this.fA21);

		this.fA11 = newfA11;
		this.fA12 = newfA12;
		this.fA13 = newfA13;
		this.fA21 = newfA21;
		this.fA22 = newfA22;
		this.fA23 = newfA23;
		this.fA31 = newfA31;
		this.fA32 = newfA32;
		this.fA33 = newfA33;
	}
	

	/**
	 * Creates Precession Matrix.
	 */
	private static final double GENERAL_PERC = 360.0/25920;
	private static final double PRECISION_LIMIT = 30.0;

	public static Matrix getPrecMatrix(double fOldEpoch, double fNewEpoch) {
		double fJd = 0.0;
		boolean	bSwapEpoch = false;
		boolean bOuterNewcomb = false;
		if (fNewEpoch == fOldEpoch) {
			return new Matrix(1.0, 0.0, 0.0,
							  0.0, 1.0, 0.0,
							  0.0, 0.0, 1.0);
		}
		double fT = (fOldEpoch - Astro.JD2000) / 36525.0;
		if (fT < -PRECISION_LIMIT || PRECISION_LIMIT < fT) {
			bSwapEpoch = true;
			double fTmp = fNewEpoch;
			fNewEpoch = fOldEpoch;
			fOldEpoch = fTmp;
			fT = (fOldEpoch - Astro.JD2000) / 36525.0;
		}
		
		double fT2 = fT * fT;
		double ft;
		double ftt = ft = (fNewEpoch - fOldEpoch) / 36525.0;
		if (ftt < -PRECISION_LIMIT) {
			bOuterNewcomb = true;
			ft = -PRECISION_LIMIT;
			fJd = -PRECISION_LIMIT * 36525.0 + Astro.JD2000;
		}
		if (PRECISION_LIMIT < ftt) {
			bOuterNewcomb = true;
			ft = PRECISION_LIMIT;
			fJd =  PRECISION_LIMIT * 36525.0 + Astro.JD2000;
		}
		
		double ft2 = ft * ft;
		double ft3 = ft2 * ft;
		
		double fzeta0 = ( (2306.2181 + 1.39656*fT - 0.000139*fT2)*ft
				 + (0.30188 - 0.000344*fT)*ft2 + 0.017998*ft3 ) / 3600.0;
		double fzpc   = ( (2306.2181 + 1.39656*fT - 0.000139*fT2)*ft
				 + (1.09468 + 0.000066*fT)*ft2 + 0.018203*ft3 ) / 3600.0;
		double ftheta = ( (2004.3109 - 0.85330*fT - 0.000217*fT2)*ft
				 - (0.42665 + 0.000217*fT)*ft2 - 0.041833*ft3 ) / 3600.0;

		Matrix mtx1 = rotateZ((90.0 - fzeta0) * Math.PI / 180.0);
		Matrix mtx2 = rotateX(     ftheta     * Math.PI / 180.0);
		Matrix mtx3 = mtx2.mul(mtx1);
		mtx1 = rotateZ((  -90 - fzpc ) * Math.PI / 180.0);
		
		Matrix mtxPrec;
		mtxPrec = mtx1.mul(mtx3);
		
		if (bOuterNewcomb) {
			double fDjd;
			if (ftt < -PRECISION_LIMIT) {
				fDjd = (fNewEpoch - fOldEpoch) + PRECISION_LIMIT * 36525.0;
			} else {
				fDjd = (fNewEpoch - fOldEpoch) - PRECISION_LIMIT * 36525.0;
			}
			double fPrecPrm = -fDjd / 365.24 * GENERAL_PERC * Math.PI / 180.0;
			double fEps = ATime.getEp(fJd);
			mtx1 = rotateX(fEps);
			mtx2 = rotateZ(fPrecPrm);
			mtx3 = mtx2.mul(mtx1);
			mtx2 = rotateX(-fEps);
			mtx1 = mtx2.mul(mtx3);
			mtxPrec = mtx1.mul(mtxPrec);
		}
		
		if(bSwapEpoch){
			mtxPrec.invert();
		}
		
		return mtxPrec;
	}
	
	/**
	 * Gets Vector Constant from Angle Elements.
	 */
	public static Matrix getVectorConstant(double fPeri, double fNode,
										double fIncl, ATime equinox) {
		// Equinox
		double fT1 = equinox.getT();
		double fT2 = equinox.getT2();

		// Obliquity of Ecliptic
		double fEps;
		if (fT2 < -40.0) {
			fEps = 23.83253 * Math.PI / 180.0;
		} else if(fT2 > 40.0) {
			fEps = 23.05253 * Math.PI / 180.0;
		} else{
			fEps = 23.44253 - 0.00013 * fT1
				+ 0.00256 * Math.cos((249.0 -  19.3 * fT1) * Math.PI / 180.0)
				+ 0.00015 * Math.cos((198.0 + 720.0 * fT1) * Math.PI / 180.0);
			fEps *= Math.PI / 180.0;
		}
		double fSinEps = Math.sin(fEps);
		double fCosEps = Math.cos(fEps);
		
		double fSinPeri = Math.sin(fPeri);
		double fSinNode = Math.sin(fNode);
		double fSinIncl = Math.sin(fIncl);
		double fCosPeri = Math.cos(fPeri);
		double fCosNode = Math.cos(fNode);
		double fCosIncl = Math.cos(fIncl);
		double fWa =  fCosPeri * fSinNode + fSinPeri * fCosIncl * fCosNode;
		double fWb = -fSinPeri * fSinNode + fCosPeri * fCosIncl * fCosNode;
		
		double fA11 = fCosPeri * fCosNode  - fSinPeri * fCosIncl * fSinNode;
		double fA21 = fWa * fCosEps - fSinPeri * fSinIncl * fSinEps;
		double fA31 = fWa * fSinEps + fSinPeri * fSinIncl * fCosEps;
		double fA12 = -fSinPeri * fCosNode - fCosPeri * fCosIncl * fSinNode;
		double fA22 = fWb * fCosEps - fCosPeri * fSinIncl * fSinEps;
		double fA32 = fWb * fSinEps + fCosPeri * fSinIncl * fCosEps;
		
		return new Matrix(fA11, fA12, 0.0,
						  fA21, fA22, 0.0,
						  fA31, fA32, 0.0);
	}
}
