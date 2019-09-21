/* Mars Simulation Project
 * DateDialog.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 * Original work by Osamu Ajiki and Ron Baalke (NASA/JPL)
 * http://www.astroarts.com/products/orbitviewer/
 * http://neo.jpl.nasa.gov/
 */

package org.mars_sim.msp.ui.astroarts;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;

import org.mars_sim.msp.core.astroarts.ATime;
import org.mars_sim.msp.core.astroarts.Astro;
import org.mars_sim.msp.core.astroarts.Comet;
import org.mars_sim.msp.core.astroarts.CometOrbit;
import org.mars_sim.msp.core.astroarts.Matrix;
import org.mars_sim.msp.core.astroarts.Planet;
import org.mars_sim.msp.core.astroarts.PlanetOrbit;
import org.mars_sim.msp.core.astroarts.Xyz;

@SuppressWarnings("serial")
public 
/*
/**
 * Orbit Canvas
 */
class OrbitCanvas extends Canvas {

	/**
	 * Orbital Element (Initialized in Constructor)
	 */
	private Comet object;
	
	/**
	 * Orbital Curve Class (Initialized in Constructor)
	 */
	private CometOrbit  objectOrbit;
	private PlanetOrbit planetOrbit[];
	private double epochPlanetOrbit;
	
	/**
	 * Date
	 */
	private ATime atime;
	
	/**
	 * Position of the Object and Planets
	 */
	private Xyz objectPos;
	private Xyz planetPos[];
        private int CenterObjectSelected;
        private boolean OrbitDisplay[];	
	/**
	 * Projection Parameters
	 */
	private double fRotateH = 0.0;
	private double fRotateV = 0.0;
	private double fZoom = 5.0;
	
	/**
	 * Rotation Matrix
	 */
	private Matrix mtxToEcl;
	private double epochToEcl;
	private Matrix mtxRotate;
	private int nX0, nY0;	// Origin
	
	/**
	 * Size of Canvas
	 */
	private Dimension sizeCanvas;
	
	/**
	 * Colors
	 */
	private Color colorObjectOrbitUpper = new Color(0x00f5ff);
	private Color colorObjectOrbitLower = new Color(0x0000ff);
	private Color colorObject           = new Color(0x00ffff);
	private Color colorObjectName       = new Color(0x00cccc);
	private Color colorPlanetOrbitUpper = new Color(0xffffff);
	private Color colorPlanetOrbitLower = new Color(0x808080);
	private Color colorPlanet			= new Color(0x00ff00);
	private Color colorPlanetName		= new Color(0x00aa00);
	private Color colorSun              = new Color(0xd04040);
	private Color colorAxisPlus         = new Color(0xffff00);
	private Color colorAxisMinus        = new Color(0x555500);
	private Color colorInformation      = new Color(0xffffff);
	
	/**
	 * Fonts
	 */
	private Font fontObjectName  = new Font("Helvetica", Font.BOLD, 14);
	private Font fontPlanetName  = new Font("Helvetica", Font.PLAIN, 14);
	private Font fontInformation = new Font("Helvetica", Font.BOLD, 14);
	
	/**
	 * off-screen Image
	 */
	Image offscreen;
	
	/**
	 * Object Name Drawing Flag
	 */
	boolean bPlanetName;
	boolean bObjectName;
	boolean bDistanceLabel;
	boolean bDateLabel;
	
	/**
	 * Constructor
	 */
	public OrbitCanvas(Comet object, ATime atime) {
		planetPos = new Xyz[9];
                OrbitDisplay = new boolean[11];
		this.object = object;
		this.objectOrbit = new CometOrbit(object, 120);
		this.planetOrbit = new PlanetOrbit[9];
		updatePlanetOrbit(atime);
		updateRotationMatrix(atime);
		// Set Initial Date
		this.atime = atime;
		setDate(this.atime);
		// no offscreen image
		offscreen = null;
		// no name labels
		bPlanetName = false;
		bObjectName = false;
		bDistanceLabel = true;
		bDateLabel = true;
		repaint();
	}
	
	/**
	 * Make Planet Orbit
	 */
	private void updatePlanetOrbit(ATime atime) {
		for (int i = Planet.MERCURY; i <= Planet.PLUTO; i++) {
			this.planetOrbit[i - Planet.MERCURY]
				= new PlanetOrbit(i, atime, 48);
		}
		this.epochPlanetOrbit = atime.getJd();
	}
	
	/**
	 * Rotation Matrix Equatorial(2000)->Ecliptic(DATE)
	 */
	private void updateRotationMatrix(ATime atime) {
		Matrix mtxPrec = Matrix.PrecMatrix(Astro.JD2000, atime.getJd());
		Matrix mtxEqt2Ecl = Matrix.RotateX(ATime.getEp(atime.getJd()));
		this.mtxToEcl = mtxEqt2Ecl.Mul(mtxPrec);
		this.epochToEcl = atime.getJd();
	}
	
	/**
	 * Horizontal Rotation Parameter Set
	 */
	public void setRotateHorz(int nRotateH) {
		this.fRotateH = (double)nRotateH;
	}
	
	/**
	 * Vertical Rotation Parameter Set
	 */
	public void setRotateVert(int nRotateV) {
		this.fRotateV = (double)nRotateV;
	}
	
	/**
	 * Zoom Parameter Set
	 */
	public void setZoom(int nZoom) {
		this.fZoom = (double)nZoom;
	}
	
	/**
	 * Date Parameter Set
	 */
	public void setDate(ATime atime) {
		this.atime = atime;
		objectPos = object.GetPos(atime.getJd());
		for (int i = 0; i < 9; i++) {
			planetPos[i] = Planet.getPos(Planet.MERCURY+i, atime);
		}
	}
	
	/**
	 * Switch Planet Name ON/OFF
	 */
	public void switchPlanetName(boolean bPlanetName) {
		this.bPlanetName = bPlanetName;
	}


        /**
         * Select Orbits
         */
        public void SelectOrbits(boolean OrbitDisplay[], int OrbitCount) {
           for (int i=0; i< OrbitCount; i++)
           {
                this.OrbitDisplay[i] = OrbitDisplay[i];
           }
        }

        /**
         * Select Center Object
         */
        public void SelectCenterObject(int CenterObjectSelected) {
                this.CenterObjectSelected = CenterObjectSelected;
        }
	
	/**
	 * Switch Object Name ON/OFF
	 */
	public void switchObjectName(boolean bObjectName) {
		this.bObjectName = bObjectName;
	}
	
	/**
	 * Switch Distance JLabel ON/OFF
	 */
	public void switchDistanceLabel(boolean bDistanceLabel) {
		this.bDistanceLabel = bDistanceLabel;
	}
	
	/**
	 * Switch Date JLabel ON/OFF
	 */
	public void switchDateLabel(boolean bDateLabel) {
		this.bDateLabel = bDateLabel;
	}
	
	/**
	 * Get (X,Y) on Canvas from Xyz
	 */
	private Point getDrawPoint(Xyz xyz) {
		// 600 means 5...fZoom...100 -> 120AU...Width...6AU
		double fMul = this.fZoom * (double)sizeCanvas.width / 600.0
							* (1.0 + xyz.fZ / 250.0);		// Parse // 250 to 400
		int nX = this.nX0 + (int)Math.round(xyz.fX * fMul);
		int nY = this.nY0 - (int)Math.round(xyz.fY * fMul);
		return new Point(nX, nY);
	}
	
	/**
	 * Draw Planets' Orbit
	 */
	private void drawPlanetOrbit(Graphics g, PlanetOrbit planetOrbit,
						 Color colorUpper, Color colorLower) {
		Point point1, point2;
		Xyz xyz = planetOrbit.getAt(0).Rotate(this.mtxToEcl)
							  .Rotate(this.mtxRotate);
		point1 = getDrawPoint(xyz);
		for (int i = 1; i <= planetOrbit.getDivision(); i++) {
			xyz = planetOrbit.getAt(i).Rotate(this.mtxToEcl);
			if (xyz.fZ >= 0.0) {
				g.setColor(colorUpper);
			} else {
				g.setColor(colorLower);
			}
			xyz = xyz.Rotate(this.mtxRotate);
			point2 = getDrawPoint(xyz);
			g.drawLine(point1.x, point1.y, point2.x, point2.y);
			point1 = point2;
		}
	}

        /**
         * Draw Earth's Orbit
         */
        private void drawEarthOrbit(Graphics g, PlanetOrbit planetOrbit,
                                                 Color colorUpper, Color colorLower) {
                Point point1, point2;
                Xyz xyz = planetOrbit.getAt(0).Rotate(this.mtxToEcl)
                                                          .Rotate(this.mtxRotate);
                point1 = getDrawPoint(xyz);
                for (int i = 1; i <= planetOrbit.getDivision(); i++) {
                        xyz = planetOrbit.getAt(i).Rotate(this.mtxToEcl);
                        g.setColor(colorUpper);
                        xyz = xyz.Rotate(this.mtxRotate);
                        point2 = getDrawPoint(xyz);
                        g.drawLine(point1.x, point1.y, point2.x, point2.y);
                        point1 = point2;
                }
        }
	
	/**
	 * Draw Planets' Body
	 */
	private void drawPlanetBody(Graphics og, Xyz planetPos, String strName) {
		Xyz xyz = planetPos.Rotate(this.mtxRotate);
		Point point = getDrawPoint(xyz);
		og.setColor(colorPlanet);
		og.fillArc(point.x - 2, point.y - 2, 5, 5, 0, 360);
		if (bPlanetName) {
			og.setColor(colorPlanetName);
			og.drawString(strName, point.x + 5, point.y);
		}
	}
	
	/**
	 * Draw Ecliptic Axis
	 */
	private void drawEclipticAxis(Graphics og) {
		Xyz xyz;
		Point point;
		
		og.setColor(colorAxisMinus);
		// -X
		xyz = (new Xyz(-50.0, 0.0,  0.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
		
		// -Z
		xyz = (new Xyz(0.0, 00.0, -50.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
		
		og.setColor(colorAxisPlus);
		// +X
		xyz = (new Xyz( 50.0, 0.0,  0.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
		// +Z
		xyz = (new Xyz(0.0, 00.0,  50.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
	}
	
	/**
	 * update (paint without clearing background)
	 */
	public void update(Graphics g) {
                 Point point3;
                 Xyz xyz, xyz1, xyz2;

		// Calculate Drawing Parameter
		Matrix mtxRotH = Matrix.RotateZ(this.fRotateH * Math.PI / 180.0);
		Matrix mtxRotV = Matrix.RotateX(this.fRotateV * Math.PI / 180.0);
		this.mtxRotate = mtxRotV.Mul(mtxRotH);

		this.nX0 = this.sizeCanvas.width  / 2;
		this.nY0 = this.sizeCanvas.height / 2;

                if (Math.abs(epochToEcl - atime.getJd()) > 365.2422 * 5) {
                        updateRotationMatrix(atime);
                }

                // If center object is comet/asteroid  
                if (CenterObjectSelected == 1 )   {
                   xyz = this.objectOrbit.getAt(0).Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
                   xyz = this.objectPos.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);	
                   point3 = getDrawPoint(xyz);

                   this.nX0 = this.sizeCanvas.width - point3.x;
                   this.nY0 = this.sizeCanvas.height - point3.y;

                   if (Math.abs(epochToEcl - atime.getJd()) > 365.2422 * 5) {
                        updateRotationMatrix(atime);
                   } 
                }
                // If center object is one of the planets
                else if (CenterObjectSelected > 1 )   {
                   xyz = planetPos[CenterObjectSelected -2].Rotate(this.mtxRotate);

                   point3 = getDrawPoint(xyz);

                   this.nX0 = this.sizeCanvas.width - point3.x;
                   this.nY0 = this.sizeCanvas.height - point3.y;

                   if (Math.abs(epochToEcl - atime.getJd()) > 365.2422 * 5) {
                        updateRotationMatrix(atime);
                   }
                }

		// Get Off-Screen Image Graphics Context
		Graphics gg = offscreen.getGraphics();
		Graphics2D g2d = (Graphics2D) gg;
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//		g2d.setRenderingHint( RenderingHints.  KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	
		// Draw Frame
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, sizeCanvas.width - 1, sizeCanvas.height - 1);
		
		// Draw Ecliptic Axis
		drawEclipticAxis(g2d);
		
		// Draw Sun
		g2d.setColor(colorSun);
		g2d.fillArc(this.nX0 - 2, this.nY0 - 2, 5, 5, 0, 360);
		
		// Draw Orbit of Object

		xyz = this.objectOrbit.getAt(0).Rotate(this.mtxToEcl)
								   .Rotate(this.mtxRotate);
		Point point1, point2;
		
		point1 = getDrawPoint(xyz);
		
		if (OrbitDisplay[0] || OrbitDisplay[1]) {

			for (int i = 1; i <= this.objectOrbit.getDivision(); i++) {
				xyz = this.objectOrbit.getAt(i).Rotate(this.mtxToEcl);
				if (xyz.fZ >= 0.0) {
					g2d.setColor(colorObjectOrbitUpper);
				} else {
					g2d.setColor(colorObjectOrbitLower);
				}
				xyz = xyz.Rotate(this.mtxRotate);
				point2 = getDrawPoint(xyz);
				g2d.drawLine(point1.x, point1.y, point2.x, point2.y);
				point1 = point2;
		   }
		}
		
		// Draw Object Body
		xyz = this.objectPos.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
		point1 = getDrawPoint(xyz);
		g2d.setColor(colorObject);
		g2d.fillArc(point1.x - 2, point1.y - 2, 5, 5, 0, 360);
		g2d.setFont(fontObjectName);
		if (bObjectName) {
			g2d.setColor(colorObjectName);
			g2d.drawString(object.getName(), point1.x + 5, point1.y);
		}
		
		//  Draw Orbit of Planets
		if (Math.abs(epochPlanetOrbit - atime.getJd()) > 365.2422 * 5) {
			updatePlanetOrbit(atime);
		}
		g2d.setFont(fontPlanetName);
		
		if (OrbitDisplay[0] || OrbitDisplay[10]) {
			drawPlanetOrbit(g2d, planetOrbit[Planet.PLUTO-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(g2d, planetPos[8], "Pluto");
		
		if (OrbitDisplay[0] || OrbitDisplay[9]) {
			
			drawPlanetOrbit(g2d, planetOrbit[Planet.NEPTUNE-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(g2d, planetPos[7], "Neptune");
		
		if (OrbitDisplay[0] || OrbitDisplay[8]) {
			drawPlanetOrbit(g2d, planetOrbit[Planet.URANUS-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(g2d, planetPos[6], "Uranus");
		
		if (OrbitDisplay[0] || OrbitDisplay[7]) {
			drawPlanetOrbit(g2d, planetOrbit[Planet.SATURN-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(g2d, planetPos[5], "Saturn");
		
		if (OrbitDisplay[0] || OrbitDisplay[6]) {
			drawPlanetOrbit(g2d, planetOrbit[Planet.JUPITER-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(g2d, planetPos[4], "Jupiter");
		
		if (fZoom * 1.524 >= 7.5) {
			if (OrbitDisplay[0] || OrbitDisplay[5]) {
				
				drawPlanetOrbit(g2d, planetOrbit[Planet.MARS-Planet.MERCURY],
								colorPlanetOrbitUpper, colorPlanetOrbitLower);
			}
			drawPlanetBody(g2d, planetPos[3], "Mars");
		}
		if (fZoom * 1.000 >= 7.5) {
                        if (OrbitDisplay[0] || OrbitDisplay[4]) {

			   drawEarthOrbit(g2d, planetOrbit[Planet.EARTH-Planet.MERCURY],
						colorPlanetOrbitUpper, colorPlanetOrbitUpper);
                        }
			drawPlanetBody(g2d, planetPos[2], "Earth");
                        
		}
		if (fZoom * 0.723 >= 7.5) {
                        if (OrbitDisplay[0] || OrbitDisplay[3]) {
			   drawPlanetOrbit(g2d, planetOrbit[Planet.VENUS-Planet.MERCURY],
						colorPlanetOrbitUpper, colorPlanetOrbitLower);
                        }
			drawPlanetBody(g2d, planetPos[1], "Venus");
		}
		if (fZoom * 0.387 >= 7.5) {
                        if (OrbitDisplay[0] || OrbitDisplay[2]) {
			   drawPlanetOrbit(g2d, planetOrbit[Planet.MERCURY-Planet.MERCURY],
						colorPlanetOrbitUpper, colorPlanetOrbitLower);
                        }
			drawPlanetBody(g2d, planetPos[0], "Mercury");
		}
		
		// Information
		g2d.setFont(fontInformation);
		g2d.setColor(colorInformation);
		FontMetrics fm = g2d.getFontMetrics();
		
		// Object Name String
		point1.x = fm.charWidth('A');
//		point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
		point1.y = 2 * fm.charWidth('A');
		g2d.drawString(object.getName(), point1.x, point1.y);
		
		if (bDistanceLabel) {
			// Earth, Mars, Sun Distance
			double edistance, sdistance, mdistance;
			double xdiff, ydiff, zdiff, xdiff2, ydiff2, zdiff2;
//			BigDecimal a,v;
			String strDist;
			xyz  = this.objectPos.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
			
			// Earth
			xyz1 = planetPos[2].Rotate(this.mtxRotate);
			
			// Mars
			xyz2 = planetPos[3].Rotate(this.mtxRotate);
						
			// Sun
			sdistance = Math.sqrt((xyz.fX * xyz.fX) + (xyz.fY * xyz.fY) + 
								  (xyz.fZ * xyz.fZ)) + .0005;
			sdistance = (int)(sdistance * 1000.0)/1000.0;
			
			// Earth
			xdiff = xyz.fX - xyz1.fX;
			ydiff = xyz.fY - xyz1.fY;
			zdiff = xyz.fZ - xyz1.fZ;
			edistance = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) +
								  (zdiff * zdiff)) + .0005;  
			edistance = (int)(edistance * 1000.0)/1000.0;
			
			// Mars
			xdiff2 = xyz.fX - xyz2.fX;
			ydiff2 = xyz.fY - xyz2.fY;
			zdiff2 = xyz.fZ - xyz2.fZ;
			mdistance = Math.sqrt((xdiff2 * xdiff2) + (ydiff2 * ydiff2) +
								(zdiff2 * zdiff2)) + .0005;  
			mdistance = (int)(mdistance * 1000.0)/1000.0;
									
//			a = new BigDecimal (edistance);
//			v = a.setScale (3, BigDecimal.ROUND_HALF_UP);
			
			// Mars
			strDist = " Mars Distance : " + mdistance + " AU";
			point1.x = fm.charWidth('A'); 
//			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight()* 11/6;
			g2d.drawString(strDist, point1.x, point1.y);

			// Earth
			strDist = "Earth Distance : " + edistance + " AU";
			point1.x = fm.charWidth('A'); 
//			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight();
			g2d.drawString(strDist, point1.x, point1.y);
						
//			a = new BigDecimal (sdistance);
//			v = a.setScale (3, BigDecimal.ROUND_HALF_UP);
			
			// Sun
			strDist = "   Sun Distance : " + sdistance + " AU";
			point1.x = fm.charWidth('A');
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() * 2 / 7;
			g2d.drawString(strDist, point1.x, point1.y);
		}
		
		if (bDateLabel) {
			// Date String
			String strDate = ATime.getMonthAbbr(atime.getMonth())
				+ " " + atime.getDay() + ", " + atime.getYear();
			point1.x = this.sizeCanvas.width  - fm.stringWidth(strDate)
				- fm.charWidth('A');
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
//			point1.y = 2 * fm.charWidth('A');
			g2d.drawString(strDate, point1.x, point1.y);
		}
		
		// Border
		g2d.clearRect(0,                    sizeCanvas.height - 1,
					 sizeCanvas.width,     sizeCanvas.height     );
		g2d.clearRect(sizeCanvas.width - 1, 0,
					 sizeCanvas.width,     sizeCanvas.height     );
		
		g.drawImage(offscreen, 0, 0, null);
	}
	
	/**
	 * paint if invalidate the canvas
	 */
	public void paint(Graphics g) {
		if (offscreen == null) {
			this.sizeCanvas = size();
			offscreen = createImage(this.sizeCanvas.width,
									this.sizeCanvas.height);
			update(g);
		} else {
			g.drawImage(offscreen, 0, 0, null);
		}
	}
	
}