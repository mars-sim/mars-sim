/* 
 * Mars Simulation Project
 * OrbitCanvas.java
 * @date 2022-07-10
 * @author Manny Kung
 * @note Original work by Osamu Ajiki and Ron Baalke (NASA/JPL)
 * http://www.astroarts.com/products/orbitviewer/
 * http://neo.jpl.nasa.gov/
 */

package com.mars_sim.ui.swing.astroarts;

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

import com.mars_sim.core.astroarts.ATime;
import com.mars_sim.core.astroarts.Astro;
import com.mars_sim.core.astroarts.Comet;
import com.mars_sim.core.astroarts.CometOrbit;
import com.mars_sim.core.astroarts.Matrix;
import com.mars_sim.core.astroarts.Planet;
import com.mars_sim.core.astroarts.PlanetOrbit;
import com.mars_sim.core.astroarts.Xyz;

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
	private Xyz[] planetPos;
	private int centerObjectSelected;
	private boolean[] orbitDisplay;	
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
	private int nX0;
	private int nY0;	// Origin
	
	/**
	 * Size of Canvas
	 */
	private Dimension sizeCanvas;
	
	/**
	 * Colors
	 */
	private static final Color OBJECT_ORBIT_UPPER_COLOR = new Color(0x00f5ff);
	private static final Color OBJECT_ORBIT_LOWER_COLOR = new Color(0x0000ff);
	private static final Color OBJECT_COLOR           = new Color(0x00ffff);
	private static final Color OBJECT_NAME_COLOR       = new Color(0x00cccc);
	private static final Color PLANET_ORBIT_UPPER_COLOR = new Color(0xffffff);
	private static final Color PLANET_ORBIT_LOWER_COLOR = new Color(0x808080);
	private static final Color PLANET_COLOR			= new Color(0x00ff00);
	private static final Color PLANET_NAME_COLOR		= new Color(0x00aa00);
	private static final Color SUN_COLOR              = new Color(0xd04040);
	private static final Color AXIS_PLUS_COLOR         = new Color(0xffff00);
	private static final Color AXIS_MINUS_COLOR        = new Color(0x555500);
	private static final Color INFO_COLOR      = new Color(0xffffff);
	
	/**
	 * Fonts
	 */
	private static final Font OBJECT_NAME_FONT  = new Font("Helvetica", Font.BOLD, 14);
	private static final Font PLANET_NAME_FONT  = new Font("Helvetica", Font.PLAIN, 14);
	private static final Font INFO_FONT = new Font("Helvetica", Font.BOLD, 14);
	
	/**
	 * off-screen Image
	 */
	private Image offscreen;
	
	/**
	 * Object Name Drawing Flag
	 */
	private boolean bPlanetName;
	private boolean bObjectName;
	private boolean bDistanceLabel;
	private boolean bDateLabel;
	
	/**
	 * Constructor
	 */
	public OrbitCanvas(Comet object, ATime atime) {
		planetPos = new Xyz[9];
        orbitDisplay = new boolean[11];
		this.object = object;
		this.objectOrbit = new CometOrbit(object, 120);
		this.planetOrbit = new PlanetOrbit[9];
		updatePlanetOrbit(atime);
		updateRotationMatrix(atime);
		// Set Initial Date
		this.atime = atime;
		setDate(this.atime);
		// no off screen image
		offscreen = null;
		// no name labels
		bPlanetName = false;
		bObjectName = false;
		bDistanceLabel = true;
		bDateLabel = true;
		
		revalidate();
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
		Matrix mtxPrec = Matrix.getPrecMatrix(Astro.JD2000, atime.getJd());
		Matrix mtxEqt2Ecl = Matrix.rotateX(ATime.getEp(atime.getJd()));
		this.mtxToEcl = mtxEqt2Ecl.mul(mtxPrec);
		this.epochToEcl = atime.getJd();
	}
	
	/**
	 * Horizontal Rotation Parameter Set
	 */
	public void setRotateHorz(int nRotateH) {
		this.fRotateH = nRotateH;
	}
	
	/**
	 * Vertical Rotation Parameter Set
	 */
	public void setRotateVert(int nRotateV) {
		this.fRotateV = nRotateV;
	}
	
	/**
	 * Zoom Parameter Set
	 */
	public void setZoom(int nZoom) {
		this.fZoom = nZoom;
	}
	
	/**
	 * Date Parameter Set
	 */
	public void setDate(ATime atime) {
		this.atime = atime;
		objectPos = object.getPos(atime.getJd());
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
	public void selectOrbits(boolean[] orbitDisplay) {
	   for (int i=0; i< orbitDisplay.length; i++) {
	        this.orbitDisplay[i] = orbitDisplay[i];
	   }
	}
	
	/**
	 * Select Center Object
	 */
	public void selectCenterObject(int index) {
		this.centerObjectSelected = index;
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
		double fMul = this.fZoom * sizeCanvas.width / 600.0
							* (1.0 + xyz.getfZ() / 250.0);		// Parse // 250 to 400
		int nX = this.nX0 + (int)Math.round(xyz.getfX() * fMul);
		int nY = this.nY0 - (int)Math.round(xyz.getfY() * fMul);
		return new Point(nX, nY);
	}
	
	/**
	 * Draw Planets' Orbit
	 */
	private void drawPlanetOrbit(Graphics g, PlanetOrbit planetOrbit,
						 Color colorUpper, Color colorLower) {
		Point point1;
		Point point2;
		Xyz xyz = planetOrbit.getAt(0).rotate(this.mtxToEcl)
							  .rotate(this.mtxRotate);
		point1 = getDrawPoint(xyz);
		for (int i = 1; i <= planetOrbit.getDivision(); i++) {
			xyz = planetOrbit.getAt(i).rotate(this.mtxToEcl);
			if (xyz.getfZ()  >= 0.0) {
				g.setColor(colorUpper);
			} else {
				g.setColor(colorLower);
			}
			xyz = xyz.rotate(this.mtxRotate);
			point2 = getDrawPoint(xyz);
			g.drawLine(point1.x, point1.y, point2.x, point2.y);
			point1 = point2;
		}
	}

	/**
	 * Draw Earth's Orbit
	 */
	private void drawEarthOrbit(Graphics g, PlanetOrbit planetOrbit,
	                                         Color colorUpper) {
		Point point1;
		Point point2;
		Xyz xyz = planetOrbit.getAt(0).rotate(this.mtxToEcl)
		                                          .rotate(this.mtxRotate);
		point1 = getDrawPoint(xyz);
		for (int i = 1; i <= planetOrbit.getDivision(); i++) {
		        xyz = planetOrbit.getAt(i).rotate(this.mtxToEcl);
		        g.setColor(colorUpper);
		        xyz = xyz.rotate(this.mtxRotate);
		        point2 = getDrawPoint(xyz);
		        g.drawLine(point1.x, point1.y, point2.x, point2.y);
		        point1 = point2;
		}
	}
	
	/**
	 * Draw Planets' Body
	 */
	private void drawPlanetBody(Graphics og, Xyz planetPos, String strName) {
		Xyz xyz = planetPos.rotate(this.mtxRotate);
		Point point = getDrawPoint(xyz);
		og.setColor(PLANET_COLOR);
		og.fillArc(point.x - 2, point.y - 2, 5, 5, 0, 360);
		if (bPlanetName) {
			og.setColor(PLANET_NAME_COLOR);
			og.drawString(strName, point.x + 5, point.y);
		}
	}
	
	/**
	 * Draw Ecliptic Axis
	 */
	private void drawEclipticAxis(Graphics og) {
		Xyz xyz;
		Point point;
		
		og.setColor(AXIS_MINUS_COLOR);
		// -X
		xyz = (new Xyz(-50.0, 0.0,  0.0)).rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
		
		// -Z
		xyz = (new Xyz(0.0, 00.0, -50.0)).rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
		
		og.setColor(AXIS_PLUS_COLOR);
		// +X
		xyz = (new Xyz( 50.0, 0.0,  0.0)).rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
		// +Z
		xyz = (new Xyz(0.0, 00.0,  50.0)).rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
	}
	
	/**
	 * update (paint without clearing background)
	 */
	@Override
	public void update(Graphics g) {
                 Point point3;
                 Xyz xyz;

		// Calculate Drawing Parameter
		Matrix mtxRotH = Matrix.rotateZ(this.fRotateH * Math.PI / 180.0);
		Matrix mtxRotV = Matrix.rotateX(this.fRotateV * Math.PI / 180.0);
		this.mtxRotate = mtxRotV.mul(mtxRotH);

		this.nX0 = this.sizeCanvas.width  / 2;
		this.nY0 = this.sizeCanvas.height / 2;

        if (Math.abs(epochToEcl - atime.getJd()) > 365.2422 * 5) {
           updateRotationMatrix(atime);
        }

        // If center object is comet/asteroid  
        if (centerObjectSelected == 1) {
           xyz = this.objectPos.rotate(this.mtxToEcl).rotate(this.mtxRotate);	
           point3 = getDrawPoint(xyz);

           this.nX0 = this.sizeCanvas.width - point3.x;
           this.nY0 = this.sizeCanvas.height - point3.y;

           if (Math.abs(epochToEcl - atime.getJd()) > 365.2422 * 5) {
                updateRotationMatrix(atime);
           } 
        }
        // If center object is one of the planets
        else if (centerObjectSelected > 1) {
           xyz = planetPos[centerObjectSelected - 2].rotate(this.mtxRotate);
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
	
		// Draw Frame
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, sizeCanvas.width - 1, sizeCanvas.height - 1);
		
		// Draw Ecliptic Axis
		drawEclipticAxis(g2d);
		
		// Draw Sun
		g2d.setColor(SUN_COLOR);
		g2d.fillArc(this.nX0 - 2, this.nY0 - 2, 5, 5, 0, 360);
		
		// Draw Orbit of Object

		xyz = this.objectOrbit.getAt(0).rotate(this.mtxToEcl)
								   .rotate(this.mtxRotate);
		Point point2;
		
		Point point1 = getDrawPoint(xyz);
		
		if (orbitDisplay[0] || orbitDisplay[1]) {

			for (int i = 1; i <= this.objectOrbit.getDivision(); i++) {
				xyz = this.objectOrbit.getAt(i).rotate(this.mtxToEcl);
				if (xyz.getfZ()  >= 0.0) {
					g2d.setColor(OBJECT_ORBIT_UPPER_COLOR);
				} else {
					g2d.setColor(OBJECT_ORBIT_LOWER_COLOR);
				}
				xyz = xyz.rotate(this.mtxRotate);
				point2 = getDrawPoint(xyz);
				g2d.drawLine(point1.x, point1.y, point2.x, point2.y);
				point1 = point2;
		   }
		}
		
		// Draw Object Body
		xyz = this.objectPos.rotate(this.mtxToEcl).rotate(this.mtxRotate);
		point1 = getDrawPoint(xyz);
		g2d.setColor(OBJECT_COLOR);
		g2d.fillArc(point1.x - 2, point1.y - 2, 5, 5, 0, 360);
		g2d.setFont(OBJECT_NAME_FONT);
		
		if (bObjectName) {
			g2d.setColor(OBJECT_NAME_COLOR);
			g2d.drawString(object.getName(), point1.x + 5, point1.y);
		}
		
		//  Draw Orbit of Planets
		if (Math.abs(epochPlanetOrbit - atime.getJd()) > 365.2422 * 5) {
			updatePlanetOrbit(atime);
		}
		g2d.setFont(PLANET_NAME_FONT);
		
		if (orbitDisplay[0] || orbitDisplay[10]) {
			drawPlanetOrbit(g2d, planetOrbit[Planet.PLUTO-Planet.MERCURY],
							PLANET_ORBIT_UPPER_COLOR, PLANET_ORBIT_LOWER_COLOR);
		}
		drawPlanetBody(g2d, planetPos[8], "Pluto");
		
		if (orbitDisplay[0] || orbitDisplay[9]) {			
			drawPlanetOrbit(g2d, planetOrbit[Planet.NEPTUNE-Planet.MERCURY],
							PLANET_ORBIT_UPPER_COLOR, PLANET_ORBIT_LOWER_COLOR);
		}
		drawPlanetBody(g2d, planetPos[7], "Neptune");
		
		if (orbitDisplay[0] || orbitDisplay[8]) {
			drawPlanetOrbit(g2d, planetOrbit[Planet.URANUS-Planet.MERCURY],
							PLANET_ORBIT_UPPER_COLOR, PLANET_ORBIT_LOWER_COLOR);
		}
		drawPlanetBody(g2d, planetPos[6], "Uranus");
		
		if (orbitDisplay[0] || orbitDisplay[7]) {
			drawPlanetOrbit(g2d, planetOrbit[Planet.SATURN-Planet.MERCURY],
							PLANET_ORBIT_UPPER_COLOR, PLANET_ORBIT_LOWER_COLOR);
		}
		drawPlanetBody(g2d, planetPos[5], "Saturn");
		
		if (orbitDisplay[0] || orbitDisplay[6]) {
			drawPlanetOrbit(g2d, planetOrbit[Planet.JUPITER-Planet.MERCURY],
							PLANET_ORBIT_UPPER_COLOR, PLANET_ORBIT_LOWER_COLOR);
		}
		drawPlanetBody(g2d, planetPos[4], "Jupiter");
		
		if (fZoom * 1.524 >= 7.5) {
			if (orbitDisplay[0] || orbitDisplay[5]) {
				
				drawPlanetOrbit(g2d, planetOrbit[Planet.MARS-Planet.MERCURY],
								PLANET_ORBIT_UPPER_COLOR, PLANET_ORBIT_LOWER_COLOR);
			}
			drawPlanetBody(g2d, planetPos[3], "Mars");
		}
		if (fZoom >= 7.5) {
			if (orbitDisplay[0] || orbitDisplay[4]) {
			   drawEarthOrbit(g2d, planetOrbit[Planet.EARTH-Planet.MERCURY],
						PLANET_ORBIT_UPPER_COLOR);
                        }
			drawPlanetBody(g2d, planetPos[2], "Earth");
                        
		}
		if (fZoom * 0.723 >= 7.5) {
			if (orbitDisplay[0] || orbitDisplay[3]) {
			   drawPlanetOrbit(g2d, planetOrbit[Planet.VENUS-Planet.MERCURY],
						PLANET_ORBIT_UPPER_COLOR, PLANET_ORBIT_LOWER_COLOR);
                        }
			drawPlanetBody(g2d, planetPos[1], "Venus");
		}
		if (fZoom * 0.387 >= 7.5) {
			if (orbitDisplay[0] || orbitDisplay[2]) {
			   drawPlanetOrbit(g2d, planetOrbit[0],
						PLANET_ORBIT_UPPER_COLOR, PLANET_ORBIT_LOWER_COLOR);
                        }
			drawPlanetBody(g2d, planetPos[0], "Mercury");
		}
		
		// Information
		g2d.setFont(INFO_FONT);
		g2d.setColor(INFO_COLOR);
		FontMetrics fm = g2d.getFontMetrics();
		
		// Object Name String
		point1.x = fm.charWidth('A');
		point1.y = 2 * fm.charWidth('A');
		g2d.drawString(object.getName(), point1.x, point1.y);
		
		if (bDistanceLabel) {
			// Earth, Mars, Sun Distance
			String strDist;
			xyz  = this.objectPos.rotate(this.mtxToEcl).rotate(this.mtxRotate);
			
			// Earth
			var xyz1 = planetPos[2].rotate(this.mtxRotate);
			
			// Mars
			var xyz2 = planetPos[3].rotate(this.mtxRotate);
						
			// Sun
			double sdistance = Math.sqrt((xyz.getfX() * xyz.getfX()) + (xyz.getfY() * xyz.getfY()) + 
								  (xyz.getfZ() * xyz.getfZ())) + .0005;
			sdistance = (int)(sdistance * 1000.0)/1000.0;
			
			// Earth
			double xdiff = xyz.getfX() - xyz1.getfX();
			double ydiff = xyz.getfY() - xyz1.getfY();
			double zdiff = xyz.getfZ() - xyz1.getfZ();
			double edistance = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) +
								  (zdiff * zdiff)) + .0005;  
			edistance = (int)(edistance * 1000.0)/1000.0;
			
			// Mars
			double xdiff2 = xyz.getfX() - xyz2.getfX();
			double ydiff2 = xyz.getfY() - xyz2.getfY();
			double zdiff2 = xyz.getfZ() - xyz2.getfZ();
			double mdistance = Math.sqrt((xdiff2 * xdiff2) + (ydiff2 * ydiff2) +
								(zdiff2 * zdiff2)) + .0005;  
			mdistance = (int)(mdistance * 1000.0)/1000.0;
			
			// Mars
			strDist = " Mars Distance : " + mdistance + " AU";
			point1.x = fm.charWidth('A'); 
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight()* 11/6;
			g2d.drawString(strDist, point1.x, point1.y);

			// Earth
			strDist = "Earth Distance : " + edistance + " AU";
			point1.x = fm.charWidth('A'); 
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight();
			g2d.drawString(strDist, point1.x, point1.y);
			
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
			g2d.drawString(strDate, point1.x, point1.y);
		}
		
		// Border
		g2d.clearRect(0,                    sizeCanvas.height - 1,
					 sizeCanvas.width,     sizeCanvas.height     );
		g2d.clearRect(sizeCanvas.width - 1, 0,
					 sizeCanvas.width,     sizeCanvas.height     );
		
		g2d.drawImage(offscreen, 0, 0, null);
		
//		offscreen.flush();
//		g2d.dispose();
	}
	
	/**
	 * paint if invalidate the canvas
	 */
	@Override
	public void paint(Graphics g) {
		if (offscreen == null) {
			this.sizeCanvas = getSize();
			offscreen = createImage(this.sizeCanvas.width,
									this.sizeCanvas.height);
			update(g);
		} else {
			g.drawImage(offscreen, 0, 0, null);
		}
	}
	
}
