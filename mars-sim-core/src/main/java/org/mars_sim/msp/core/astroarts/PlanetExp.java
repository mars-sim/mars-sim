/**
 * Planet Position by Expansion
 */
package org.mars_sim.msp.core.astroarts;
import org.mars_sim.msp.core.astroarts.UdMath;

/**
 * planet position by expansion
 */
public class PlanetExp {
	//
	// Mercury
	//
	static final PlanetExpP0 MercuryLambda[] = {
		new PlanetExpP0(0.5258,  448417.55,   74.38),
		new PlanetExpP0(0.1796,  298945.77,  137.84),
		new PlanetExpP0(0.1061,  597890.10,  249.2),
		new PlanetExpP0(0.0850,  149473.3,   143.0),
		new PlanetExpP0(0.0760,  448418.3,   312.6),
		new PlanetExpP0(0.0256,  597890.8,   127.4),
		new PlanetExpP0(0.0230,  747362.6,    64.0),
		new PlanetExpP0(0.0081,  747363.0,   302.0),
		new PlanetExpP0(0.0069,       1.0,   148.0),
		new PlanetExpP0(0.0052,  896835.0,   239.0),
		new PlanetExpP0(0.0023,  896836.0,   117.0),
		new PlanetExpP0(0.0019,    6356.0,    85.0),
		new PlanetExpP0(0.0011, 1046308.0,    54.0),
	};
	static final PlanetExpP0 MercuryBeta[] = {
		new PlanetExpP0(0.3123,  448417.92, 103.51),
		new PlanetExpP0(0.0753,  597890.4,  278.3),
		new PlanetExpP0(0.0367,  149472.1,   55.7),
		new PlanetExpP0(0.0187,  747362.9,   93.1),
		new PlanetExpP0(0.0050,  298945.0,  230.0),
		new PlanetExpP0(0.0047,  896835.0,  268.0),
		new PlanetExpP0(0.0028,  448419.0,  342.0),
		new PlanetExpP0(0.0023,  298946.0,  347.0),
		new PlanetExpP0(0.0020,  597891.0,  157.0),
		new PlanetExpP0(0.0012, 1046308.0,   83.0),
		new PlanetExpP0(0.0009,  747364.0,  331.0),
		new PlanetExpP0(0.0009,  448717.0,   45.0),
	};
	static final PlanetExpP0 MercuryR[] = {
		new PlanetExpP0(0.001214,	448417.55,	344.38),
		new PlanetExpP0(0.000218,	597890.1,	159.20),
		new PlanetExpP0(0.000042,	747363.0,	334.0),
		new PlanetExpP0(0.000006,	896835.0,	149.0),
	};
	//
	// Venus
	//
	static final PlanetExpP0 VenusL0[] = {
		new PlanetExpP0(-0.0048,	248.6,	-19.34),
		new PlanetExpP0(-0.0004,	198.0,	720.0 ),
	};
	static final PlanetExpP0 VenusL1[] = {
		new PlanetExpP0(0.0033, 357.9, 1170.35),
		new PlanetExpP0(0.0031, 242.3,  450.37),
		new PlanetExpP0(0.0020, 273.5,  675.55),
		new PlanetExpP0(0.0014,  31.1,  225.18),
	};
	static final PlanetExpP0 VenusQ[] = {
		new PlanetExpP0(-0.000015,	357.9,	1170.35),
		new PlanetExpP0( 0.000010,	 62.3,	 450.37),
		new PlanetExpP0(-0.000008,	 93.0,	 675.6 ),
	};
	static final PlanetExpP1 VenusP = new PlanetExpP1(
		310.1735,		585.19212,
		 -0.0503,		107.44,		1170.37,
		 0.7775,		-0.00005,	178.954,	585.178,
		 0.05922,		233.72,		585.183,
		 -0.002947,		0.00000021,	178.954,	585.178,	-0.140658
	);
	//
	// Mars
	//
	static final PlanetExpP0 MarsL0[] = {
		new PlanetExpP0(-0.0048,	248.6,	-19.34),
		new PlanetExpP0(-0.0004,    198.0,	720.0 ),
	};
	static final PlanetExpP0 MarsL1[] = {
		new PlanetExpP0(0.6225,  187.54,  382.797),
		new PlanetExpP0(0.0503,  101.31,  574.196),
		new PlanetExpP0(0.0146,   62.31,    0.198),
		new PlanetExpP0(0.0071,   71.8,   161.05 ),
		new PlanetExpP0(0.0061,  230.2,   130.71 ),
		new PlanetExpP0(0.0046,   15.1,   765.59 ),
		new PlanetExpP0(0.0045,  147.5,   322.11 ),
		new PlanetExpP0(0.0039,  279.3,   -22.81 ),
		new PlanetExpP0(0.0024,  207.7,   168.59 ),
		new PlanetExpP0(0.0020,  140.1,   145.78 ),
		new PlanetExpP0(0.0018,  224.7,    10.98 ),
		new PlanetExpP0(0.0014,  221.8,   -45.62 ),
		new PlanetExpP0(0.0010,   91.4,   -30.34 ),
		new PlanetExpP0(0.0009,  268,     100.4  ),
	};
	static final PlanetExpP0 MarsQ[] = {
		new PlanetExpP0(-0.002825,	187.54,	382.797),
		new PlanetExpP0(-0.000249,	101.31,	574.196),
		new PlanetExpP0(-0.000024,	 15.1,	765.59 ),
		new PlanetExpP0( 0.000023,	251.7,	161.05 ),
		new PlanetExpP0( 0.000022,	327.6,	322.11 ),
		new PlanetExpP0( 0.000017,	 50.2,	130.71 ),
		new PlanetExpP0( 0.000007,	 27.0,	168.6  ),
		new PlanetExpP0( 0.000006,	320.0,	145.8  ),
	};
	static final PlanetExpP1 MarsP = new PlanetExpP1(
	   249.3542,	191.41696,
	   -0.0149,		40.01,			382.819,
		10.6886,	0.00010,		273.768,	191.399,
		0.03227,	200.00,			191.409,
		-0.040421,	-0.00000039,	273.768,	191.399,	0.183844
	);
	//
	// Jupiter
	//
	static final PlanetExpP0 JupiterN[] = {
		new PlanetExpP0( 0.3323,  162.78,    0.385),
		new PlanetExpP0( 0.0541,   38.46,  -36.256),
		new PlanetExpP0( 0.0447,  293.42,  -29.941),
		new PlanetExpP0( 0.0342,   44.50,   -5.907),
		new PlanetExpP0( 0.0230,  201.25,  -24.035),
		new PlanetExpP0( 0.0222,  109.99,  -18.128),
		new PlanetExpP0(-0.0048,  248.6,   -19.34 ),
		new PlanetExpP0( 0.0047,  184.6,   -11.81 ),
		new PlanetExpP0( 0.0045,  150.1,   -54.38 ),
		new PlanetExpP0( 0.0042,  130.7,   -42.16 ),
		new PlanetExpP0( 0.0039,    7.6,     6.31 ),
		new PlanetExpP0( 0.0031,  163.2,    12.22 ),
		new PlanetExpP0( 0.0031,  145.6,     0.77 ),
		new PlanetExpP0( 0.0024,  191.3,    -0.23 ),
		new PlanetExpP0( 0.0019,  148.4,    24.44 ),
		new PlanetExpP0( 0.0017,  197.9,   -29.941),
		new PlanetExpP0( 0.0010,  307.9,    36.66 ),
		new PlanetExpP0( 0.0010,  252.6,   -72.51 ),
		new PlanetExpP0( 0.0010,  269.0,   -60.29 ),
		new PlanetExpP0( 0.0010,  278.7,   -29.53 ),
		new PlanetExpP0( 0.0008,   52.0,   -66.6  ),
		new PlanetExpP0( 0.0008,   24.0,   -35.8  ),
		new PlanetExpP0( 0.0005,  356.0,    -5.5  ),
	};
	static final PlanetExpP0 JupiterB[] = {
		new PlanetExpP0(0.0010, 291.9,	 -29.94),
		new PlanetExpP0(0.0003, 196.0,   -24.0 ),
	};
	static final PlanetExpP0 JupiterQ[] = {
		new PlanetExpP0(0.000230,   38.47,  -36.256),
		new PlanetExpP0(0.000168,  293.36,  -29.941),
		new PlanetExpP0(0.000074,  200.50,  -24.03),
		new PlanetExpP0(0.000055,  110.0,   -18.13),
		new PlanetExpP0(0.000038,   39.3,    -5.91),
		new PlanetExpP0(0.000024,  150.9,   -54.38),
		new PlanetExpP0(0.000023,  336.4,     0.41),
		new PlanetExpP0(0.000019,  131.7,   -42.16),
		new PlanetExpP0(0.000009,  180.0,   -11.8),
		new PlanetExpP0(0.000007,  277.0,   -60.3),
		new PlanetExpP0(0.000006,  330.0,    24.4),
		new PlanetExpP0(0.000006,   53.0,   -66.6),
		new PlanetExpP0(0.000006,  188.0,     6.3),
		new PlanetExpP0(0.000006,  251.0,   -72.5),
		new PlanetExpP0(0.000006,  198.0,   -29.9),
		new PlanetExpP0(0.000005,  353.5,    12.22),
	};
	static final PlanetExpP2 JupiterP = new PlanetExpP2(
		13.6526,	0.01396,
		0.0075,		5.94,
		5.5280,		0.1666,		0.0070,		0.0003,
		0.022889,	272.975,	0.0128,		0.00010,		35.52,
		5.190688,	0.048254
	);
	//
	// Saturn
	//
	static final PlanetExpP0 SaturnN[] = {
		new PlanetExpP0( 0.8081,  342.74,    0.385),
		new PlanetExpP0( 0.1900,    3.57,  -11.813),
		new PlanetExpP0( 0.1173,  224.52,   -5.907),
		new PlanetExpP0( 0.0093,  176.6,     6.31),
		new PlanetExpP0( 0.0089,  218.5,   -36.26),
		new PlanetExpP0( 0.0080,   10.4,    -0.23),
		new PlanetExpP0( 0.0078,   56.8,     0.63),
		new PlanetExpP0( 0.0074,  325.4,     0.77),
		new PlanetExpP0( 0.0073,  209.4,   -24.03),
		new PlanetExpP0( 0.0064,  202.0,   -11.59),
		new PlanetExpP0(-0.0048,  248.6,   -19.34),
		new PlanetExpP0( 0.0034,  105.2,   -30.35),
		new PlanetExpP0( 0.0034,   23.6,   -15.87),
		new PlanetExpP0( 0.0025,  348.4,   -11.41),
		new PlanetExpP0( 0.0022,  102.5,    -7.94),
		new PlanetExpP0( 0.0021,   53.5,    -3.65),
		new PlanetExpP0( 0.0020,  220.4,   -18.13),
		new PlanetExpP0( 0.0018,  326.7,   -54.38),
		new PlanetExpP0( 0.0017,  173.0,    -5.50),
		new PlanetExpP0( 0.0014,  165.5,    -5.91),
		new PlanetExpP0( 0.0013,  307.9,   -42.16),
	};
	static final PlanetExpP0 SaturnB[] = {
		new PlanetExpP0( 0.0024,	  3.9,	-11.81),
		new PlanetExpP0( 0.0008,    269.0,	 -5.9),
		new PlanetExpP0( 0.0005,    135.0,	-30.3),
	};
	static final PlanetExpP0 SaturnQ[] = {
		new PlanetExpP0(0.000701,     3.43,  -11.813),
		new PlanetExpP0(0.000378,   110.54,  -18.128),
		new PlanetExpP0(0.000244,   219.13,   -5.907),
		new PlanetExpP0(0.000114,   158.22,    0.383),
		new PlanetExpP0(0.000064,   218.1,   -36.26),
		new PlanetExpP0(0.000042,   215.8,   -24.03),
		new PlanetExpP0(0.000024,   201.8,   -11.59),
		new PlanetExpP0(0.000024,     1.3,     6.31),
		new PlanetExpP0(0.000019,   307.7,    12.22),
		new PlanetExpP0(0.000015,   326.3,   -54.38),
		new PlanetExpP0(0.000010,   311.1,   -42.16),
		new PlanetExpP0(0.000010,    83.2,    24.44),
		new PlanetExpP0(0.000009,   348.0,   -11.4),
		new PlanetExpP0(0.000008,   129.0,   -30.3),
		new PlanetExpP0(0.000006,   295.0,   -29.9),
		new PlanetExpP0(0.000006,   148.0,   -48.5),
		new PlanetExpP0(0.000006,   103.0,    -7.9),
		new PlanetExpP0(0.000005,   318.0,    24.4),
		new PlanetExpP0(0.000005,    24.0,   -15.9),
	};
	static final PlanetExpP2 SaturnP = new PlanetExpP2(
		91.8560,	0.01396,
		0.0272,		135.53,
		6.4215,		0.2248,		0.0109,		0.0006,
		0.043519,	337.763,	0.0286,		0.00023,		77.06,
		9.508863,	0.056061
	);
	//
	// Uranus
	//
	static final PlanetExpP0 UranusLambda[] = {
		new PlanetExpP0(5.35857,	 460.61987,		 48.85031),
		new PlanetExpP0(0.58964,     919.0429,		188.3245),
		new PlanetExpP0(0.12397,    1065.1192,		354.5935),
		new PlanetExpP0(0.01475,    2608.702,		351.028),
		new PlanetExpP0(0.00090,    1968.3,		247.7),
		new PlanetExpP0(0.00036,    5647.4,		 10.4),
		new PlanetExpP0(0.00017,    2356.6,		183.6),
		new PlanetExpP0(0.00017,    2873.2,		321.9),
		new PlanetExpP0(0.00014,    3157.9,		308.1),
	};
	static final PlanetExpP0 UranusBeta[] = {
		new PlanetExpP0(1.15483,	 419.91739,		128.15303),
		new PlanetExpP0(0.67756,     652.9504,		273.6644),
		new PlanetExpP0(0.13490,     998.0302,		 83.3517),
		new PlanetExpP0(0.00025,    3030.9,		194.2),
	};
	static final PlanetExpP0 UranusR[] = {
		new PlanetExpP0(0.905790,	 408.729,		320.313),
		new PlanetExpP0(0.062710,    799.95,		 67.99),
		new PlanetExpP0(0.004897,   2613.7,		 80.4),
		new PlanetExpP0(0.000656,   1527.0,		202.0),
		new PlanetExpP0(0.000223,   2120.0,		321.0),
		new PlanetExpP0(0.000205,   3104.0,		 37.0),
		new PlanetExpP0(0.000120,   5652.0,		100.0),
	};
	//
	// Neptune
	//
	static final PlanetExpP0 NeptuneLambda[] = {
		new PlanetExpP0(0.97450,	 221.3904,	167.7269),
		new PlanetExpP0(0.01344,     986.281,	 50.826),
		new PlanetExpP0(0.00945,    2815.89,	  0.09),
		new PlanetExpP0(0.00235,    2266.50,	309.35),
		new PlanetExpP0(0.00225,    2279.43,	127.61),
		new PlanetExpP0(0.00023,    5851.6,	 19.2),
	};
	static final PlanetExpP0 NeptuneBeta[] = {
		new PlanetExpP0(1.76958,	 218.87906,	 83.11018),
		new PlanetExpP0(0.01366,	 447.128,	338.864),
		new PlanetExpP0(0.00015,	1107.1,		224.7),
		new PlanetExpP0(0.00015,	2596.7,		187.5),
		new PlanetExpP0(0.00012,	3035.0,		243.9),
	};
	static final PlanetExpP0 NeptuneR[] = {
		new PlanetExpP0(0.260457,	 222.371,	 79.994),
		new PlanetExpP0(0.004944,   2815.4,	 90.1),
		new PlanetExpP0(0.003364,    524.0,	308.1),
		new PlanetExpP0(0.002579,   1025.1,	104.0),
		new PlanetExpP0(0.000120,   5845.0,	111.0),
	};
	//
	// Pluto
	//
	static final PlanetExpP0 PlutoLambda[] = {
		new PlanetExpP0(15.81087,	 246.556453,	298.348019),
		new PlanetExpP0( 1.18379,	 551.34710, 	351.67676),
		new PlanetExpP0( 0.07886, 	 941.622,   	 41.989),
		new PlanetExpP0( 0.00861, 	2836.46,    	 60.35),
		new PlanetExpP0( 0.00590, 	1306.75,    	112.91),
		new PlanetExpP0( 0.00145, 	2488.14,    	 19.01),
		new PlanetExpP0( 0.00022, 	5861.8,     	 77.9),
		new PlanetExpP0( 0.00013, 	3288.8,     	293.0),
	};
	static final PlanetExpP0 PlutoBeta[] = {
		new PlanetExpP0(17.04550,	 172.554318,	 42.574982),
		new PlanetExpP0( 2.45310,	 415.60630,		 66.15350),
		new PlanetExpP0( 0.26775,	 713.1227,		105.0840),
		new PlanetExpP0( 0.01855,	1089.202,		146.660),
		new PlanetExpP0( 0.00119,	2658.22,		293.06),
		new PlanetExpP0( 0.00098,	3055.6,			 18.8),
		new PlanetExpP0( 0.00090,	1532.6,			213.7),
		new PlanetExpP0( 0.00042,	2342.3,			254.2),
	};
	static final PlanetExpP0 PlutoR[] = {
		new PlanetExpP0(8.670489,	 181.3383,		198.4973),
		new PlanetExpP0(0.333884,	 475.963,		228.717),
		new PlanetExpP0(0.008426,	 909.8,			252.9),
		new PlanetExpP0(0.004902,	2831.6,			149.4),
		new PlanetExpP0(0.001188,	1748.0,			114.1),
		new PlanetExpP0(0.000390,	3188.0,			 15.0),
		new PlanetExpP0(0.000116,	5860.0,			169.0),
	};
	//
	// Sun
	//
	static final PlanetExpP0 SunLambda[] = {
		new PlanetExpP0( 0.0200,   353.06,  719.981),
		new PlanetExpP0(-0.0048,   248.64,  -19.341),
		new PlanetExpP0( 0.0020,   285.0,   329.64),
		new PlanetExpP0( 0.0018,   334.2, -4452.67),
		new PlanetExpP0( 0.0018,   293.7,    -0.20),
		new PlanetExpP0( 0.0015,   242.4,   450.37),
		new PlanetExpP0( 0.0013,   211.1,   225.18),
		new PlanetExpP0( 0.0008,   208.0,   659.29),
		new PlanetExpP0( 0.0007,    53.5,    90.38),
		new PlanetExpP0( 0.0007,    12.1,   -30.35),
		new PlanetExpP0( 0.0006,   239.1,   337.18),
		new PlanetExpP0( 0.0005,    10.1,    -1.50),
		new PlanetExpP0( 0.0005,    99.1,   -22.81),
		new PlanetExpP0( 0.0004,   264.8,   315.56),
		new PlanetExpP0( 0.0004,   233.8,   299.30),
		new PlanetExpP0(-0.0004,   198.1,   720.02),
		new PlanetExpP0( 0.0003,   349.6,  1079.97),
		new PlanetExpP0( 0.0003,   241.2,   -44.43),
	};
	static final PlanetExpP0 SunQ[] = {
		new PlanetExpP0(-0.000091,	353.1,	 719.98),
		new PlanetExpP0( 0.000013,  205.8,	4452.67),
		new PlanetExpP0( 0.000007,   62.0,   450.4),
		new PlanetExpP0( 0.000007,  105.0,	 329.6),
	};
	/**
	 * Get Position of the Earth
	 */
	private static Xyz getPosExp0(double fT) {
		double fLambda = 279.0358 + 360.00769 * fT
			+ ( 1.9159 - 0.00005 * fT) 
				* UdMath.udsin((356.531)+ ( 359.991) * fT);
		for (int i = 0; i < SunLambda.length; i++) {
			fLambda += SunLambda[i].a * UdMath.udsin(SunLambda[i].b
													 + SunLambda[i].c * fT);
		}
		fLambda += 0.0057;
		fLambda = UdMath.deg2rad(UdMath.degmal(fLambda));
		double fBeta = 0.0;
		
		double fq = (- 0.007261+0.0000002 * fT) * UdMath.udcos((356.53)
										   + (359.991) * fT) + 0.000030;
		for (int i = 0; i < SunQ.length; i++) {
			fq += SunQ[i].a * UdMath.udcos(SunQ[i].b + SunQ[i].b * fT);
		}
		
		double fRadius = Math.pow(10.0, fq);

		return new Xyz(-fRadius * Math.cos(fBeta) * Math.cos(fLambda),
					   -fRadius * Math.cos(fBeta) * Math.sin(fLambda),
					   -fRadius * Math.sin(fBeta));
	}

	/**
	 * Get Position of Venus and Mars
	 */
	private static Xyz getPosExp1(int planetNo, double fT) {
		PlanetExpP0 ParamL0[], ParamL1[], ParamQ[];
		PlanetExpP1 ParamP;
		switch (planetNo) {
		case Planet.VENUS:
			ParamL0 = VenusL0;
			ParamL1 = VenusL1;
			ParamQ  = VenusQ;
			ParamP  = VenusP;
			break;
		case Planet.MARS:
			ParamL0 = MarsL0;
			ParamL1 = MarsL1;
			ParamQ  = MarsQ;
			ParamP  = MarsP;
			break;
		default:
			throw new ArithmeticException();
		}
		double L1 = (ParamP.L6 + ParamP.L7 * fT)
						* UdMath.udsin(ParamP.L8 + ParamP.L9 * fT);
		for (int i = 0; i < ParamL1.length; i++) {
			L1 += ParamL1[i].a * UdMath.udsin(ParamL1[i].b
											+ ParamL1[i].c * fT);
		}
		double L0 = ParamP.L1 + ParamP.L2 * fT
			+ ParamP.L3 * UdMath.udsin(ParamP.L4 + ParamP.L5 * fT + 2.0 * L1);
		for (int i = 0; i < ParamL0.length; i++) {
			L0 += ParamL0[i].a * UdMath.udsin(ParamL0[i].b
											+ ParamL0[i].c * fT);
		}
		double fLambda = UdMath.deg2rad(UdMath.degmal(L0 + L1));
		double fBeta = Math.asin(ParamP.B1 * UdMath.udsin(ParamP.B2
												+ ParamP.B3 * fT + L1));
		double fq = (ParamP.q1 + ParamP.q2 * fT)
					* UdMath.udcos(ParamP.q3 + ParamP.q4 * fT) + ParamP.q5;
		for (int i = 0; i < ParamQ.length; i++) {
			fq += ParamQ[i].a * UdMath.udcos(ParamQ[i].b + ParamQ[i].c * fT);
		}
		double fRadius = Math.pow(10.0, fq);
		
		return new Xyz(fRadius * Math.cos(fBeta) * Math.cos(fLambda),
					   fRadius * Math.cos(fBeta) * Math.sin(fLambda),
					   fRadius * Math.sin(fBeta));
	}

	/**
	 * Get Position of Jupiter and Saturn
	 */
	private static Xyz getPosExp2(int planetNo, double fT) {
		PlanetExpP0 ParamN[], ParamB[], ParamQ[];
		PlanetExpP2 ParamP;
		double fq, fN;
		switch (planetNo) {
		case Planet.JUPITER:
			ParamN = JupiterN;
			ParamB = JupiterB;
			ParamQ = JupiterQ;
			ParamP = JupiterP;
			fN  = 341.5208 + 30.34907 * fT;
			fN += (0.0350 + 0.00028 * fT)
				* UdMath.udsin(245.94 - 30.349 * fT)+ 0.0004;
			fN -= (0.0019 + 0.00002 * fT)
				* UdMath.udsin(162.78 +  0.38 * fT);
			fq  = (0.000132 + 0.0000011 * fT)
				* UdMath.udcos(245.93 - 30.349 * fT);
			break;
		case Planet.SATURN:
			ParamN = SaturnN;
			ParamB = SaturnB;
			ParamQ = SaturnQ;
			ParamP = SaturnP;
			fN  = 12.3042 +12.22117 * fT;
			fN += (0.0934 + 0.00075 * fT)
				* UdMath.udsin(250.29 + 12.221 * fT)+ 0.0008;
			fN += (0.0057 + 0.00005 * fT) * UdMath.udsin(265.8  - 11.81 * fT);
			fN += (0.0049 + 0.00004 * fT) * UdMath.udsin(162.7  +  0.38 * fT);
			fN += (0.0019 + 0.00002 * fT) * UdMath.udsin(262.0  + 24.44 * fT);
			fq  = (0.000354 + 0.0000028 * fT)
				* UdMath.udcos( 70.28 + 12.22 * fT) + 0.000183;
			fq += (0.000021 + 0.0000002 * fT)
				* UdMath.udcos(265.80 - 11.81  * fT);
			break;
		default:
			throw new ArithmeticException();
		}

		// Lambda
		for (int i = 0; i < ParamN.length; i++) {
			fN += ParamN[i].a * UdMath.udsin(ParamN[i].b + ParamN[i].c * fT);
		}
		double ff = fN + ParamP.f1 * UdMath.udsin(fN)
				+ ParamP.f2 * UdMath.udsin(2.0 * fN)
				+ ParamP.f3 * UdMath.udsin(3.0 * fN)
				+ ParamP.f4 * UdMath.udsin(4.0 * fN);
		double fV = ParamP.V1 * UdMath.udsin(2.0 * ff + ParamP.V2);
		
		double fLambda = UdMath.deg2rad(UdMath.degmal(ff + fV
										+ ParamP.L1 + ParamP.L2 * fT));
		
		// Beta
		double fBeta = Math.asin(ParamP.B1 * UdMath.udsin(ff + ParamP.B2))
							+ UdMath.deg2rad((ParamP.B3 + ParamP.B4 * fT)
							* UdMath.udsin(ff + ParamP.B5));
		for (int i = 0; i < ParamB.length; i++) {
			fBeta += ParamB[i].a * UdMath.udsin(ParamB[i].b
												+ ParamB[i].c * fT);
		}
		
		// Radius
		for (int i = 0; i < ParamQ.length; i++) {
			fq += ParamQ[i].a * UdMath.udcos(ParamQ[i].b + ParamQ[i].c * fT);
		}
		double fr = Math.pow(10.0, fq);
		double fRadius = fr * ParamP.r1
			/ ( 1.0 + ParamP.r2 * UdMath.udcos(ff));
		
		return new Xyz(fRadius * Math.cos(fBeta) * Math.cos(fLambda),
					   fRadius * Math.cos(fBeta) * Math.sin(fLambda),
					   fRadius * Math.sin(fBeta));
	}

	/**
	 * Get Position of Mercury, Uranus, Nneptune, Pluto
	 */
	private static Xyz getPosExp3(int planetNo, double fT2) {
		PlanetExpP0 ParamL[], ParamB[], ParamR[];
		double fLambda, fBeta, fRadius;
		switch (planetNo) {
		case Planet.MERCURY:
			ParamL = MercuryLambda;
			ParamB = MercuryBeta;
			ParamR = MercuryR;
			
			fLambda  = 252.2502 + 149474.0714 * fT2;
			fLambda += (23.4405 + 0.0023 * fT2)
				* UdMath.udcos(149472.5153 * fT2 + 84.7947);
			fLambda += ( 2.9818 + 0.0006 * fT2)
				* UdMath.udcos(298945.031 * fT2 + 259.589 );
			
			fBeta  = (6.7057 + 0.0017 * fT2)
				* UdMath.udcos(149472.886 * fT2 + 113.919 );
			fBeta += (1.4396 + 0.0005 * fT2)
				* UdMath.udcos(     0.37  * fT2 + 119.12  );
			fBeta += (1.3643 + 0.0005 * fT2)
				* UdMath.udcos(298945.40  * fT2 + 288.71  );
			
			fRadius  =  0.395283 + 0.000002 * fT2;
			fRadius += (0.078341 + 0.000008 * fT2)
				* UdMath.udcos(149472.515 * fT2 + 354.795);
			fRadius += (0.007955 + 0.000002 * fT2)
				* UdMath.udcos(298945.03 * fT2 + 169.59 );
			break;
		case Planet.URANUS:
			ParamL = UranusLambda;
			ParamB = UranusBeta;
			ParamR = UranusR;
			
			fLambda  = 313.33676 + 428.72880 * fT2;
			fLambda +=   3.20671 * fT2
				* UdMath.udcos( 705.15539 * fT2 + 114.02740);
			fLambda +=   2.69325 * fT2
				* UdMath.udcos( 597.77389 * fT2 + 317.76510);
			fLambda +=   0.00015 * fT2
				* UdMath.udcos(3798.6     * fT2 + 313.4    );
			
			fBeta = -0.02997;
			fBeta +=  1.78488 * fT2
				* UdMath.udcos( 507.52281 * fT2 + 188.32394);
			fBeta +=  0.56518 * fT2
				* UdMath.udcos( 892.2869  * fT2 + 354.9571 );
			fBeta +=  0.00036 * fT2
				* UdMath.udcos(1526.5     * fT2 + 263.0    );
			
			fRadius  = 19.203034 + 0.042617 * fT2;
			fRadius +=  0.361949 * fT2
				* UdMath.udcos( 440.702 * fT2 +  19.879);
			fRadius +=  0.166685 * fT2
				* UdMath.udcos( 702.024 * fT2 + 307.419);
			break;
		case Planet.NEPTUNE:
			ParamL = NeptuneLambda;
			ParamB = NeptuneBeta;
			ParamR = NeptuneR;
			
			fLambda  = - 55.13323 + 219.93503 * fT2;
			fLambda +=   0.04403 * fT2
				* UdMath.udcos( 684.128  * fT2 + 332.797  );
			fLambda +=   0.02928 * fT2
				* UdMath.udcos( 904.371  * fT2 + 342.114  );
			
			fBeta = 0.01725;
			
			fRadius = 30.073033;
			fRadius +=  0.009784 * fT2
				* UdMath.udcos( 515.2   * fT2 + 195.7  );
			break;
		case Planet.PLUTO:
			ParamL = PlutoLambda;
			ParamB = PlutoBeta;
			ParamR = PlutoR;
			
			fLambda  = 241.82574 + 179.09519 * fT2;
			fBeta    = -2.30285;
			
			fRadius = 38.662489;
			fRadius +=  0.007619 * fT2
				* UdMath.udcos(1425.9   * fT2 +  31.0   );
			fRadius +=  0.002543 * fT2
				* UdMath.udcos(2196.1   * fT2 + 199.5   );
			break;
		default:
			throw new ArithmeticException();
		}
		
		for (int i = 0; i < ParamL.length; i++) {
			fLambda += ParamL[i].a * UdMath.udcos(ParamL[i].b
												  * fT2 + ParamL[i].c);
		}
		fLambda = UdMath.deg2rad(UdMath.degmal(fLambda));
		
		for (int i = 0; i < ParamB.length; i++) {
			fBeta += ParamB[i].a * UdMath.udcos(ParamB[i].b
												* fT2 + ParamB[i].c);
		}
		fBeta = UdMath.deg2rad(fBeta);
		
		for (int i = 0; i < ParamR.length; i++) {
			fRadius += ParamR[i].a * UdMath.udcos(ParamR[i].b
												  * fT2 + ParamR[i].c);
		}
		
		return new Xyz(fRadius * Math.cos(fBeta) * Math.cos(fLambda),
					   fRadius * Math.cos(fBeta) * Math.sin(fLambda),
					   fRadius * Math.sin(fBeta));
	}
	
	public static Xyz getPos(int planetNo, ATime atime) {
		switch (planetNo) {
		case Planet.EARTH:
			return getPosExp0(atime.getT());
		case Planet.VENUS:
		case Planet.MARS:
			return getPosExp1(planetNo, atime.getT());
		case Planet.JUPITER:
		case Planet.SATURN:
			return getPosExp2(planetNo, atime.getT());
		case Planet.MERCURY:
		case Planet.URANUS:
		case Planet.NEPTUNE:
		case Planet.PLUTO:
			return getPosExp3(planetNo, atime.getT2());
		}
		return null;
	}
}

class PlanetExpP0 {
	public double a;
	public double b;
	public double c;

	public PlanetExpP0(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
}

class PlanetExpP1 {		/* Venus and Mars */
	public double L1, L2, L3, L4, L5, L6, L7, L8, L9;
	public double B1, B2, B3;
	public double q1, q2, q3, q4, q5;

	public PlanetExpP1(double L1, double L2, double L3,
						double L4, double L5, double L6,
						double L7, double L8, double L9,
						double B1, double B2, double B3,
						double q1, double q2, double q3,
						double q4, double q5) {
		this.L1 = L1;	this.L2 = L2;	this.L3 = L3;
		this.L4 = L4;	this.L5 = L5;	this.L6 = L6;
		this.L7 = L7;	this.L8 = L8;	this.L9 = L9;
		this.B1 = B1;	this.B2 = B2;	this.B3 = B3;
		this.q1 = q1;	this.q2 = q2;	this.q3 = q3;
		this.q4 = q4;	this.q5 = q5;
	}
}

class PlanetExpP2 {		/* Jupiter and Saturn */
	public double L1, L2;
	public double V1, V2;
	public double f1, f2, f3, f4;
	public double B1, B2, B3, B4, B5;
	public double r1, r2;

	public PlanetExpP2(double L1, double L2,
						double V1, double V2,
						double f1, double f2, double f3, double f4,
						double B1, double B2, double B3, double B4, double B5,
						double r1, double r2) {
		this.L1 = L1;	this.L2 = L2;
		this.V1 = V1;	this.V2 = V2;
		this.f1 = f1;	this.f2 = f2;	this.f3 = f3;	this.f4 = f4;
		this.B1 = B1;	this.B2 = B2;	this.B3 = B3;	this.B4 = B4;
		this.B5 = B5;
		this.r1 = r1;	this.r2 = r2;
	}
}
