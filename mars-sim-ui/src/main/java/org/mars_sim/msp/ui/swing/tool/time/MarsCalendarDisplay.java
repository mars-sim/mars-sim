/**
 * Mars Simulation Project
 * GlobeDisplay.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.time;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.extended.WebComponent;
import com.alee.managers.style.StyleId;


/**
 * The Mars Calendar Display class shows the current month in a panel for the
 * {@link TimeWindow} class.
 */
@SuppressWarnings("serial")
public class MarsCalendarDisplay extends WebComponent {

	// Data members
	/** The Martian clock instance. */
	private MarsClock marsTime;

	private MainDesktopPane desktop;

//	private MainScene mainScene;
	
	/** The Sol of month cache. */
	private int solOfMonthCache;
	private int weekHeight;
	private int solHeight;
	private int solsInMonth;
	private int solCache;
	
//	private int themeCache = -1;
	
	private char[] weekLetters = { 'S', 'P', 'D', 'T', 'H', 'V', 'J' };
	
	private FontMetrics solMetrics;
	private FontMetrics weekMetrics;
	
	private Color baseColor = Color.white;
	private Color headerColor = new Color(218, 165, 32); 
//	private Color darkColor = new Color(112, 95, 76);
	private Color lightColor = new Color(255, 222, 173);
	private Color numberColor = new Color(139, 69, 19);

	private Color numberHighlightColor = new Color(218, 165, 32);
	
	// Pick color at https://www.html.am/html-codes/color/color-scheme.cfm?rgbColor=112,128,144
	// 99, 125, 150 // dull blue
	// 101, 139, 210 // blue
	// 85, 152, 212 // blue
	// 112, 95, 76 // dull brown
	// 140, 94, 74 // brown
	// 210, 180, 140 // light tan
	// 255, 248, 220 // cornsilk
	// 218, 165, 32 // Goldenrod
	// 139, 69, 19 // SaddleBrown
	// 255, 222, 173 // navajo white (pale yellow)
	
	// 73, 97, 0 // dull green 
	// 74, 140, 94 // green
	// 210, 117, 101 // dull pinkish red
	
	private Font solFont = new Font(Font.SANS_SERIF, Font.BOLD, 9);
	private Font weekFont = new Font("Arial", Font.ITALIC, 10);
	
	/**
	 * Constructs a MarsCalendarDisplay object.
	 * 
	 * @param marsTime Martian clock instance
	 * @param desktop the main desktop
	 */
	public MarsCalendarDisplay(MarsClock marsTime, MainDesktopPane desktop) {

		// Initialize data members
		this.marsTime = marsTime;
		this.desktop = desktop;
//		mainScene = desktop.getMainScene();
		
		// Set component size
		setPreferredSize(new Dimension(140, 100));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());
		
		// Set up week letter font
		weekMetrics = getFontMetrics(weekFont);
		weekHeight = weekMetrics.getAscent();

		// Set up Sol number font
		solMetrics = getFontMetrics(solFont);
		solHeight = solMetrics.getAscent();
		
		solOfMonthCache = marsTime.getSolOfMonth();
		solsInMonth = MarsClock.getSolsInMonth(marsTime.getMonth(), marsTime.getOrbit());
	}

	/**
	 * Updates the calendar display
	 */
	public void update() {

//		if (mainScene != null) {
//			int theme = 0;//MainScene.getTheme();
//
//			if (themeCache != theme) {
//				themeCache = theme;
//
//				if (theme == 1) {
//					// green theme
//					baseColor = Color.orange;
//					midColor = new Color(104, 114, 77); // 74, 140, 94
//					darkColor = new Color(73, 97, 0);
//				} else if (theme == 2) {
//					// red theme
//					baseColor = Color.red;
//					midColor = new Color(255, 102, 102); // pink orange
//					darkColor = new Color(51, 25, 0); // dark brown
//				} else if (theme == 3) {
//					// brownish theme
//					baseColor = Color.orange;
//					midColor = new Color(210, 117, 101); // orange pink
//					darkColor = new Color(140, 94, 74); // greyish brown pink
//				}
//
//				else if (theme == 4) {
//					// grey theme
//					baseColor = Color.gray;
//					midColor = Color.lightGray;
//					darkColor = Color.DARK_GRAY;
//				} else if (theme == 5) {
//					// purple theme
//					baseColor = Color.magenta;
//					midColor = new Color(112, 76, 103);
//					darkColor = new Color(51, 0, 51);
//				} else if (theme == 6 || theme == 0) {
//					// blue theme
//					baseColor = Color.white;//new Color(85, 152, 212);//Color.cyan;
//					midColor = new Color(99, 125, 150);
//					darkColor = new Color(101, 139, 210);
//				} else if (theme == 7) {
//					// pale olive theme
//					baseColor = Color.orange;
//					midColor = new Color(152, 149, 92);
//					darkColor = new Color(138, 141, 74);
//				}
//
//				SwingUtilities.invokeLater(() -> repaint());
//			}
////		}

		// check for the passing of each day
		int newSol = marsTime.getMissionSol();
		if (solCache != newSol) {

			solsInMonth = MarsClock.getSolsInMonth(marsTime.getMonth(), marsTime.getOrbit());
			
			if (solOfMonthCache != marsTime.getSolOfMonth()) {
				solOfMonthCache = marsTime.getSolOfMonth();
				SwingUtilities.invokeLater(() -> repaint());
			}
			
			solCache = newSol;
		}
	}

	/**
	 * Overrides paintComponent method.
	 * 
	 * @param g graphics context
	 */
	@Override
	public void paintComponent(Graphics g) {

		// Paint background
		g.setColor(lightColor);
		g.fillRect(0, 0, 140, 95);

		// Paint mid week day name header
		g.setColor(headerColor);
		g.fillRect(0, 0, 140, 15);


		// If sols in month are 27, black out lower left square
		if (solsInMonth == 27) {
			g.setColor(Color.black);
			g.fillRect(121, 71, 138, 93);
		}

		// Paint grid lines
		g.setColor(baseColor);
		g.drawRect(0, 0, 139, 94);

		// Paint vertical day lines
		for (int x = 1; x < 7; x++) {
			g.drawLine(20 * x, 0, 20 * x, 94);
		}

		// Paint horizontal lines
		for (int x = 0; x < 4; x++) {
			g.drawLine(0, (20 * x) + 15, 139, (20 * x) + 15);
		}

		// Draw week letters
		g.setFont(weekFont);
		for (int x = 0; x < 7; x++) {
			int letterWidth = weekMetrics.charWidth(weekLetters[x]);
			g.drawString("" + weekLetters[x], (20 * x) + 11 - (letterWidth / 2), weekHeight + 1);
		}

		// Draw sol letters
		g.setFont(solFont);
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 7; x++) {
				int solNumber = (y * 7) + x + 1;
				int solNumberWidth = solMetrics.stringWidth("" + solNumber);
				int xPos = (20 * x) + 11 - (solNumberWidth / 2);
				int yPos = (20 * y) + 35 - (solHeight / 2);
				if (solNumber <= solsInMonth)
					g.drawString(Integer.toString(solNumber), xPos, yPos);
				if (solNumber == marsTime.getSolOfMonth()) {
					g.fillRect((20 * x) + 2, (20 * y) + 17, 17, 17);
					g.setColor(numberHighlightColor);
					g.drawString(Integer.toString(solNumber), xPos, yPos);
					g.setColor(numberColor);
				}
			}
		}
	}

	@Override
	public StyleId getDefaultStyleId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateUI() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUIClassID() {
		// TODO Auto-generated method stub
		return null;
	}
}
