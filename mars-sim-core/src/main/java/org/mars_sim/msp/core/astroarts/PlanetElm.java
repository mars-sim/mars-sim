/**
 * PlanetElm Class
 */
package org.mars_sim.msp.core.astroarts;
import org.mars_sim.msp.core.astroarts.UdMath;

class PlanetElm {
	double L;		/* M+peri+node */
	double node;	/* Ascending Node */
	double peri;	/* Argument of Perihelion */
	double axis;	/* Semimajor Axis */
	double e;		/* Eccentricity */
	double incl;	/* Inclination */
	//
	// Mercury
	//
	static final PlanetElmP1 MercuryE = new PlanetElmP1(
		 182.27175,		149474.07244,		 2.01944E-3,		0.0,
		  75.89717,		     1.553469,		 3.08639E-4,		0.0,
		  47.144736,	     1.18476,		 2.23194E-4,		0.0,
		   7.003014,	     1.73833E-3,	-1.55555E-5,		0.0,
		   0.20561494,		 0.0203E-3,		-0.04E-6,			0.0,
		   0.3870984
	);
	//
	// Venus
	//
	static final PlanetElmP1 VenusE = new PlanetElmP1(
		 344.36936,		 58519.2126,		 9.8055E-4,			0.0,
		 130.14057,		     1.37230,		-1.6472E-3,			0.0,
		  75.7881,			 0.91403,		 4.189E-4,			0.0,
		   3.3936,			 1.2522E-3,		-4.333E-6,			0.0,
		   0.00681636,		-0.5384E-4,		 0.126E-6,			0.0,
		   0.72333015
	);
	//
	// Mars
	//
	static final PlanetElmP1 MarsE = new PlanetElmP1(
		 294.26478,		 19141.69625,		 3.15028E-4,		0.0,
		 334.21833,		     1.840394,		 3.35917E-4,		0.0,
		  48.78670,		     0.776944,		-6.02778E-4,		0.0,
		   1.85030,			-6.49028E-4,	 2.625E-5,			0.0,
		   0.0933088,		 0.095284E-3,	-0.122E-6,			0.0,
		   1.5236781
	);
	//
	// Jupiter
	//
	static final PlanetElmP1 JupiterE = new PlanetElmP1(
		 238.132386,	  3036.301986,		 3.34683E-4,		-1.64889E-6,
		  12.720972,		 1.6099617,		 1.05627E-3,		-3.4333E-6,
		  99.443414,		 1.01053,		 3.52222E-4,		-8.51111E-6,
		   1.308736,		-5.69611E-3,	 3.88889E-6,		 0.0,
		   0.0483348,		 0.164180E-3,	-0.4676E-6,			-1.7E-9,
		   5.202805
	);
	//
	// Saturn
	//
	static final PlanetElmP1 SaturnE = new PlanetElmP1(
		 266.597875,	  1223.50988,		 3.24542E-4,		-5.83333E-7,
		  91.09821,			 1.958416,		 8.26361E-4,		 4.61111E-6,
		 112.790414,		 0.873195,		-1.521810E-4,		-5.30555E-6,
		   2.49252,			-3.91889E-3,	-1.54889E-5,		 4.44444E-8,
		   0.05589231,		-0.34550E-3,	-0.728E-6,			 0.74E-9,
		   9.55474
	);
	//
	// Uranus
	//
	static final PlanetElmP2 UranusE = new PlanetElmP2(
		 314.055005,		 0.01176903644,	 0.0003043,
		 173.005159,		 1.4863784,		 0.0002145,
		  74.005947,		 0.5211258,		 0.0013399,
		  19.2184461,		-0.000000037,	 0.0,
		   0.04629590,		-0.000027337,	 0.000000079,
		   0.773196,		 0.0007744,		 0.0000375
	);
	//
	// Neptune
	//
	static final PlanetElmP2 NeptuneE = new PlanetElmP2(
		 304.348665,	 0.00602007691,	 0.0003093,
		  48.123691,	 1.4262678,		 0.0003792,
		 131.784057,	 1.1022035,		 0.0002600,
		  30.1103869,	-0.000000166,	 0.0,
		   0.00898809,	 0.000006408,	-0.000000001,
		   1.769952,	-0.0093082,		-0.0000071
	);
	//
	// Pluto
	//
	static final PlanetElmP2 PlutoE = new PlanetElmP2(
		 238.467028,	 0.00401595755,	-0.0090561,
		 224.141630,	 1.3900789,		 0.0003019,
		 110.318223,	 1.3506963,		 0.0004014,
		  39.5403429,	 0.00313105,	-0.00003792,
		   0.24900535,	 0.000038850,	-0.000000562,
		  17.145104,	-0.0054981,		-0.0000384
	);
	
	//
	// Perturbation correction for Jupiter
	//
	static private int perturbJup1[] = {
		-20, -27, -44, -36, -20,  10,  21,  27,  33,  25,  18,   8, -20,
		-14, -25, -57, -75, -70, -55, -25, -15,  -2,   8,   1,  -4, -15,
		5,  -5, -21, -55, -67, -72, -55, -28, -13,   0,   7,  10,   5,
		24,  21,   9, -11, -37, -57, -55, -37, -15,   3,  13,  18,  23,
		27,  29,  27,  15,   4, -25, -45, -38, -22,  -5,  10,  25,  30,
		15,  27,  39,  33,  25,  -5, -27, -34, -30, -19,  -6,  20,  21,
		7,  15,  25,  31,  24,   8, -11, -26, -32, -27, -19,  -6,  16,
		-3,   3,  15,  23,  22,  15,   0, -15, -26, -29, -25, -20,  -4,
		-15,  -5,   3,  17,  22,  20,  11,   5, -11, -26, -27, -25, -16,
		-17,  -4,  10,  20,  25,  31,  25,  24,  15,  -6, -15, -18, -13,
		0,   2,  13,  28,  39,  49,  48,  38,  33,  27,  13,  -1,  -2,
		-1,   0,   6,  23,  39,  49,  63,  53,  48,  41,  35,  17,   4,
		-26, -30, -30, -25,  -9,  17,  31,  34,  34,  25,  22,  13,   6
	};
	static private int perturbJup2[] = {
		4,  15,  30,  40,  40,  25,   6,   8, -27, -43, -43, -28,  -5,
		-24,  -9,   7,  10,  27,  30,  31,  17,  -4, -29, -43, -40, -27,
		-31, -24, -25,  -5,  14,  31,  43,  43,  19,  -6, -29, -43, -32,
		-39, -29, -21, -13,  -4,  19,  36,  52,  35,  15, -11, -30, -36,
		-31, -30, -24, -19, -13,   0,  20,  35,  46,  31,   9, -17, -30,
		-26, -28, -28, -20, -17, -15,   0,  24,  46,  45,  25,   0, -28,
		-10, -23, -27, -23, -21, -22, -14,   4,  29,  40,  37,  17,  -5,
		15,  -9, -20, -22, -23, -27, -21, -13,  12,  31,  40,  33,  15,
		29,  13, -10, -18, -22, -27, -30, -25, -11,  16,  36,  42,  31,
		45,  28,   8, -10, -20, -28, -33, -33, -26,   9,  22,  45,  44,
		41,  45,  19,   9,  -9, -21, -34, -34, -34, -19,  -4,  26,  42,
		22,  36,  42,  25,  14,   0, -18, -27, -34, -32, -21,  -7,  26,
		0,  11,  26,  39,  36,  25,   8,  -8, -26, -38, -38, -28,  -2,
	};
	static private int perturbJup3[] = {
		41,  33,  19,   4, -13, -28, -37, -42, -27,  -9,  16,  30,  44,
		27,  33,  33,  23,  15,   3, -22, -36, -43, -25, -10,  14,  27,
		13,  23,  32,  33,  27,  22,   8, -22, -37, -42, -27, -10,  12,
		-5,  10,  18,  23,  34,  32,  25,   5, -26, -45, -47, -26,  -5,
		-17,  -2,  10,  18,  26,  35,  37,  22,  -4, -27, -44, -42, -27,
		-33, -15,  -1,   7,  16,  22,  36,  35,  16,  -7, -28, -40, -36,
		-44, -27, -12,  -6,   4,  16,  32,  54,  31,  12, -10, -31, -43,
		-37, -37, -24, -12,  -2,   7,  17,  30,  42,  24,  11, -15, -33,
		-31, -36, -35, -24, -13,  -4,   7,  21,  35,  38,  20,   6, -15,
		-19, -32, -40, -31, -21, -18,  -5,  12,  25,  38,  42,  26,  -6,
		11, -14, -30, -44, -33, -27, -13,  -1,  15,  29,  42,  39,  18,
		31,  13,  -6, -22, -34, -29, -27, -27,   9,  15,  25,  40,  35,
		40,  31,  18,   6, -15, -28, -38, -40, -29, -13,  15,  25,  40,
	};
	
	//
	// Perturbation correction for Saturn
	//
	static private int perturbSat1[] = {
		57,  59,  57,  60,  56,  48,  42,  41,  41,  42,  46,  50,  55,
		61,  64,  70,  73,  74,  66,  61,  57,  55,  55,  55,  56,  56,
		58,  61,  65,  71,  76,  76,  72,  66,  63,  61,  60,  58,  56,
		55,  55,  58,  63,  68,  74,  73,  71,  67,  63,  61,  57,  55,
		52,  51,  51,  55,  61,  67,  70,  70,  67,  62,  58,  55,  53,
		49,  48,  47,  48,  52,  58,  63,  65,  63,  60,  56,  52,  50,
		48,  46,  44,  43,  45,  49,  54,  57,  58,  56,  53,  50,  48,
		46,  44,  41,  40,  39,  40,  45,  48,  50,  51,  50,  48,  46,
		44,  42,  39,  37,  36,  35,  36,  39,  43,  45,  46,  45,  44,
		42,  40,  36,  34,  32,  31,  31,  33,  37,  39,  41,  42,  44,
		42,  39,  37,  33,  30,  29,  29,  30,  32,  34,  37,  40,  44,
		45,  45,  43,  39,  35,  30,  29,  30,  33,  35,  38,  42,  45,
		55,  57,  61,  56,  49,  45,  42,  40,  42,  43,  46,  50,  54,
	};
	static private int perturbSat2[] = {
		33,  37,  44,  52,  60,  66,  67,  65,  57,  46,  37,  32,  31,
		34,  40,  50,  60,  67,  70,  67,  60,  50,  40,  33,  29,  31,
		36,  42,  50,  60,  68,  72,  68,  59,  47,  38,  34,  34,  37,
		45,  48,  52,  57,  62,  65,  63,  55,  45,  40,  39,  42,  44,
		54,  55,  54,  53,  54,  55,  54,  49,  45,  43,  44,  48,  54,
		57,  60,  55,  51,  46,  45,  44,  46,  47,  48,  51,  55,  57,
		57,  59,  56,  50,  43,  39,  39,  44,  49,  52,  55,  57,  57,
		53,  54,  52,  49,  44,  40,  41,  45,  51,  55,  57,  54,  54,
		46,  44,  45,  47,  47,  48,  48,  51,  55,  57,  55,  51,  47,
		37,  35,  37,  45,  52,  57,  60,  59,  58,  56,  52,  45,  39,
		31,  29,  33,  43,  55,  65,  69,  66,  60,  55,  48,  40,  34,
		32,  30,  35,  45,  56,  68,  72,  69,  60,  52,  43,  36,  32,
		33,  36,  43,  51,  59,  65,  68,  65,  57,  47,  38,  34,  31,
	};
	static private int perturbSat3[] = {
		51,  60,  66,  67,  62,  56,  46,  40,  34,  31,  37,  45,  53,
		59,  66,  70,  67,  60,  51,  40,  33,  30,  33,  40,  50,  60,
		60,  65,  67,  66,  59,  50,  38,  31,  30,  35,  43,  52,  59,
		58,  59,  60,  59,  55,  49,  40,  36,  36,  43,  50,  55,  57,
		55,  52,  50,  50,  49,  47,  45,  45,  45,  50,  55,  56,  55,
		53,  48,  44,  42,  43,  46,  50,  53,  55,  56,  57,  55,  53,
		51,  47,  41,  38,  40,  47,  55,  59,  61,  59,  56,  53,  51,
		48,  42,  44,  42,  44,  48,  55,  58,  58,  55,  51,  50,  48,
		45,  49,  50,  50,  50,  51,  53,  55,  54,  50,  45,  43,  45,
		46,  52,  59,  62,  61,  56,  53,  50,  46,  42,  39,  38,  41,
		45,  54,  65,  71,  71,  63,  53,  43,  39,  35,  34,  35,  42,
		48,  55,  65,  71,  70,  63,  51,  40,  34,  31,  33,  38,  44,
		51,  60,  66,  68,  65,  58,  46,  38,  33,  32,  37,  46,  54,
	};
	static private int perturbSat4[] = {
		83,  82,  80,  78,  75,  74,  73,  73,  75,  77,  79,  81,  83,
		81,  82,  82,  81,  80,  77,  75,  72,  72,  75,  77,  80,  81,
		77,  70,  77,  75,  75,  75,  70,  67,  65,  64,  65,  68,  70,
		50,  51,  54,  58,  60,  61,  59,  56,  52,  49,  47,  47,  49,
		30,  32,  34,  37,  40,  42,  42,  40,  36,  31,  30,  29,  30,
		17,  18,  19,  20,  22,  24,  27,  26,  21,  19,  17,  15,  17,
		13,  13,  12,  12,  14,  15,  17,  18,  17,  16,  15,  14,  13,
		20,  19,  18,  17,  17,  18,  20,  21,  24,  24,  23,  21,  20,
		31,  31,  32,  32,  31,  31,  32,  35,  37,  38,  36,  34,  32,
		50,  50,  53,  53,  52,  51,  51,  52,  53,  53,  52,  50,  50,
		68,  69,  71,  72,  72,  70,  69,  68,  68,  68,  70,  70,  67,
		80,  80,  79,  80,  80,  79,  77,  76,  74,  76,  77,  80,  80,
		83,  83,  80,  78,  75,  75,  76,  76,  76,  76,  79,  81,  83,
	};
	
	/**
	 * Correction for Perturbation
	 */
	double perturbationElement(double eta, double zeta, int[] tbl) {
		int e1 = (int)(eta/30.0);
		int e2 = e1 + 1;
		int z1 = (int)(zeta/30.0);
		int z2 = z1 + 1;
		
		if(e1 >= 12 && z1 >= 12){
			return (double)tbl[z1*13 + e1];
		}
		
		if(e1 >= 12){
			double v1 = (double)tbl[z1*13 + e1];
			double v3 = (double)tbl[z2*13 + e1];
			double p3 = v1 + (v3 - v1)*(zeta/30.0 - (double)z1);
			return p3;
		}
		
		if(z1 >= 12){
			double v1 = (double)tbl[z1*13 + e1];
			double v2 = (double)tbl[z1*13 + e2];
			double p3 = v1 + (v2 - v1)*(eta/30.0 - (double)e1);
			return p3;
		}
		
		double v1 = (double)tbl[z1*13 + e1];
		double v2 = (double)tbl[z1*13 + e2];
		double v3 = (double)tbl[z2*13 + e1];
		double v4 = (double)tbl[z2*13 + e2];
		double p1 = v1 + (v3 - v1)*(zeta/30.0 - (double)z1);
		double p2 = v2 + (v4 - v2)*(zeta/30.0 - (double)z1);
		double p3 = p1 + (p2 - p1)*(eta/30.0 - (double)e1);
		return p3;
	}
	
	/**
	 * Mean orbital element of Jupiter with perturbation
	 */
	private void perturbationJupiter(double jd) {
		int year = (int)((jd - 1721423.5) / 365.244 + 1.0);
		double T = year/1000.0;
		
		double L7 = (0.42 - 0.075*T + 0.015*T*T - 0.003*T*T*T) 
			* UdMath.udsin( (T - 0.62)*360.0/0.925 );
		double PS7 = 0.02 * UdMath.udsin( (T + 0.1)*360.0/0.925 );
		double PH7 = 0.03 * UdMath.udsin( (T + 0.36)*360.0/0.925 );
		double ETA = UdMath.degmal(86.1 + 0.033459
								   * ( jd - 1721057.0 ));
		double ZETA = UdMath.degmal(89.1 + 0.049630
									* ( jd - 1721057.0 ));
		double L8 = perturbationElement(ETA, ZETA, perturbJup1)/1000.0;
		double PS8 = perturbationElement(ETA, ZETA, perturbJup2)/1000.0;
		double PH8 = perturbationElement(ETA, ZETA, perturbJup3)/1000.0;
		double PH  = 2.58 + 0.1*T;
		if (PH > 3.5) {
			PH = 3.5;
		}
		if (PH < 1.5) {
			PH = 1.5;
		}
		L += ( L7 + L8 );
		peri += (PS7 + PS8) / UdMath.udsin(PH);
		e = UdMath.udsin(PH + PH7 + PH8);
	}
	
	/**
	 * Mean orbital element of Saturn with perturbation
	 */
	void perturbationSaturn(double jd) {
		int year = (int)((jd - 1721423.5) / 365.244 + 1.0);
		double T = year/1000.0;
		
		double AT = 0.88 - 0.0633*T + 0.03*T*T - 0.0006*T*T*T;
		double L7 = -0.50 + AT*UdMath.udsin((T - 0.145)*360.0/0.95);
		double PS7 = -0.50 + (0.10 - 0.005*T)
			* UdMath.udsin((T - 0.54)*360.0/0.95);
		double PH7 = -0.50 + (0.10 - 0.005*T)
			* UdMath.udsin((T - 0.32)*360.0/0.95);
		double AX7 = -0.050 + (0.004 - 0.0005*T)
			* UdMath.udsin((T - 0.35)*360.0/0.95);
		double ETA = UdMath.degmal(86.1 + 0.033459
								   * ( jd - 1721057.0 ));
		double ZETA = UdMath.degmal(89.1 + 0.049630
									* ( jd - 1721057.0 ));
		double L8 = perturbationElement(ETA, ZETA, perturbSat1)/100.0;
		double PS8 = perturbationElement(ETA, ZETA, perturbSat2)/100.0;
		double PH8 = perturbationElement(ETA, ZETA, perturbSat3)/100.0;
		double AX8 = perturbationElement(ETA, ZETA, perturbSat4)/1000.0;
		double PH  = 3.56 - 0.175*T - 0.005*T*T;
		/* if year > 7000 then PH < 2.0 */
		if (PH < 2.0) {
			PH = 2.0;
		}
		L += ( L7 + L8 );
		peri += (PS7 + PS8) / UdMath.udsin(PH);
		e = UdMath.udsin(PH + PH7 + PH8);
		axis += AX7 + AX8;
	}
	
	/**
	 * Get mean orbital elements (Mercury, Venus, Mars, Jupiter, Saturn)
	 */
	private void getPlanetElm1(int planetNo, double jd) {
		double C1 = (jd - Astro.JD1900) / 36525.0;
		double C2 = C1 * C1;
		PlanetElmP1 elmCf;
		switch (planetNo) {
		case Planet.MERCURY:
			elmCf = MercuryE;
			break;
		case Planet.VENUS:
			elmCf = VenusE;
			break;
		case Planet.MARS:
			elmCf = MarsE;
			break;
		case Planet.JUPITER:
			elmCf = JupiterE;
			break;
		case Planet.SATURN:
			elmCf = SaturnE;
			break;
		default:
			throw new ArithmeticException();
		}
		/* M+peri+node */
		L = UdMath.degmal(elmCf.L    + elmCf.L1 * C1 
						  + elmCf.L2 * C2 + elmCf.L3 * C1 * C2);
		/* Ascending Node */
		node = UdMath.degmal(elmCf.node + elmCf.n1 * C1 
							 + elmCf.n2 * C2 + elmCf.n3 * C1 * C2);
		/* Argument of Perihelion */
		peri = UdMath.degmal(elmCf.peri + elmCf.p1 * C1 
							 + elmCf.p2 * C2 + elmCf.p3 * C1 * C2
							 - node);
		/* Semimajor Axis */
		axis = elmCf.axis;
		/* Eccentricity */
		e    = UdMath.degmal(elmCf.e    + elmCf.e1 * C1 
							 + elmCf.e2 * C2 + elmCf.e3 * C1 * C2 );
		/* Inclination */
		incl = UdMath.degmal(elmCf.incl + elmCf.i1 * C1 
							 + elmCf.i2 * C2 + elmCf.i3 * C1 * C2);
		
		switch (planetNo) {
		case Planet.JUPITER:
			perturbationJupiter(jd);
			break;
		case Planet.SATURN:
			perturbationSaturn(jd);
			break;
		}
	}
	
	/**
	 * Get mean orbital elements (Uranus, Neptune, Pluto)
	 */
	private void getPlanetElm2(int planetNo, double jd) {
		double T1 = ( jd - Astro.JD2000 ) / 36525.0;
		double T2 = T1 * T1;
		double d  = T1 * 36525.0;
		PlanetElmP2 elmCf = null;
		switch (planetNo) {
		case Planet.URANUS:
			elmCf = UranusE;
			break;
		case Planet.NEPTUNE:
			elmCf = NeptuneE;
			break;
		case Planet.PLUTO:
			elmCf = PlutoE;
			break;
		default:
			throw new ArithmeticException();
		}
		/* M+peri+node */
		L    =	UdMath.degmal(elmCf.L    + elmCf.L1 * d
							  + elmCf.L2 *T2);
		/* Ascending Node */
		node =	UdMath.degmal(elmCf.node + elmCf.n1 * T1
							  + elmCf.n2 *T2);
		/* Argument of Perihelion */
		peri =	UdMath.degmal(elmCf.peri + elmCf.p1 * T1
							  + elmCf.p2 *T2 - node);
		/* Semimajor Axis */
		axis = UdMath.degmal(elmCf.axis + elmCf.a1 * T1
							 + elmCf.a2 *T2);
		/* Eccentricity */
		e    =	UdMath.degmal(elmCf.e    + elmCf.e1 * T1
							  + elmCf.e2 *T2);
		/* Inclination */
		incl =	UdMath.degmal(elmCf.incl + elmCf.i1 * T1
							  + elmCf.i2 *T2);
	}
	
	/**
	 * Get mean orbital elements (Earth)
	 */
	private void getPlanetElmEarth(double jd) {
		double c = (jd - Astro.JD1900)/36525.0;
		double c2 = c * c;
		L    = 180.0 + UdMath.degmal(280.6824 + 36000.769325*c
									 + 7.22222e-4*c2);
		peri = 180.0 + UdMath.degmal(281.2206 +     1.717697*c
									 + 4.83333e-4*c2 + 2.77777e-6*c*c2);
		node = 0.0;	/* no ascending node for the Earth */
		incl = 0.0;	/* no inclination    for the Earth */
		e    = 0.0167498 - 4.258e-5*c - 1.37e-7*c2;
		axis = 1.00000129;
	}
	
	public PlanetElm(int planetNo, ATime atime) {
		switch (planetNo) {
		case Planet.EARTH:
			getPlanetElmEarth(atime.getJd());
			break;
		case Planet.MERCURY:
		case Planet.VENUS:
		case Planet.MARS:
		case Planet.JUPITER:
		case Planet.SATURN:
			getPlanetElm1(planetNo, atime.getJd());
			break;
		case Planet.URANUS:
		case Planet.NEPTUNE:
		case Planet.PLUTO:
			getPlanetElm2(planetNo, atime.getJd());
			break;
		default:
			throw new ArithmeticException();
		}
	}
	
	/**
	 * Orbital element to Xyz
	 */
	public Xyz getPos() {
		double re = this.e * 180.0 / Math.PI;
		double E, M, oldE;
		E = M = this.L - (this.peri + this.node);
		do {
			oldE = E;
			E = M + re * UdMath.udsin(oldE);
		} while (Math.abs(E - oldE) > 1.0e-5 * 180.0 / Math.PI);
		double px = this.axis * (UdMath.udcos(E) - this.e);
		double py = this.axis * Math.sqrt(1.0 - this.e * this.e)
			* UdMath.udsin(E);
		
		double sinperi = UdMath.udsin(this.peri);
		double cosperi = UdMath.udcos(this.peri);
		double sinnode = UdMath.udsin(this.node);
		double cosnode = UdMath.udcos(this.node);
		double sinincl = UdMath.udsin(this.incl);
		double cosincl = UdMath.udcos(this.incl);
		
		double xc =  px * (cosnode * cosperi - sinnode * cosincl * sinperi)
			- py * (cosnode * sinperi + sinnode * cosincl * cosperi);
		double yc =  px * (sinnode * cosperi + cosnode * cosincl * sinperi)
			- py * (sinnode * sinperi - cosnode * cosincl * cosperi);
		double zc =  px * (sinincl * sinperi)
			+ py * (sinincl * cosperi);
		
		return new Xyz(xc, yc, zc);
	}
}

class PlanetElmP1 {
	public double L,    L1, L2, L3;
	public double peri, p1, p2, p3;
	public double node, n1, n2, n3;
	public double incl, i1, i2, i3;
	public double e,    e1, e2, e3;
	public double axis;
	
	public PlanetElmP1(double L,    double L1, double L2, double L3,
						double peri, double p1, double p2, double p3,
						double node, double n1, double n2, double n3,
						double incl, double i1, double i2, double i3,
						double e,    double e1, double e2, double e3,
						double axis) {
		this.L    = L;    this.L1 = L1; this.L2 = L2; this.L3 = L3;
		this.peri = peri; this.p1 = p1; this.p2 = p2; this.p3 = p3;
		this.node = node; this.n1 = n1; this.n2 = n2; this.n3 = n3;
		this.incl = incl; this.i1 = i1; this.i2 = i2; this.i3 = i3;
		this.e    = e;    this.e1 = e1; this.e2 = e2; this.e3 = e3;
		this.axis = axis;
	}
}

class PlanetElmP2 {
	public double L,    L1, L2;
	public double peri, p1, p2;
	public double node, n1, n2;
	public double axis, a1, a2;
	public double e,    e1, e2;
	public double incl, i1, i2;
	
	public PlanetElmP2(double L,    double L1, double L2,
						double peri, double p1, double p2,
						double node, double n1, double n2,
						double axis, double a1, double a2,
						double e,    double e1, double e2,
						double incl, double i1, double i2) {
		this.L = L;       this.L1 = L1; this.L2 = L2;
		this.peri = peri; this.p1 = p1; this.p2 = p2;
		this.node = node; this.n1 = n1; this.n2 = n2;
		this.axis = axis; this.a1 = a1; this.a2 = a2;
		this.e = e;       this.e1 = e1; this.e2 = e2;
		this.incl = incl; this.i1 = i1; this.i2 = i2;
	}
}
