/**
 * Mars Simulation Project
 * GlobeDisplay.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.time;

import org.mars_sim.msp.core.time.MarsClock;

import javax.swing.*;
import java.awt.*;

/** 
 * The Mars Calendar Display class shows the current month 
 * in a panel for the {@link TimeWindow} class.
 */
class MarsCalendarDisplay
extends JComponent {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The Martian clock instance. */
	private MarsClock marsTime;
	/** The Sol of month cache. */
	private int solOfMonthCache;

	/** 
	 * Constructs a MarsCalendarDisplay object.
	 * @param marsTime Martian clock instance 
	 */
	public MarsCalendarDisplay(MarsClock marsTime) {

		// Initialize data members
		this.marsTime = marsTime;        
		solOfMonthCache = marsTime.getSolOfMonth();

		// Set component size
		setPreferredSize(new Dimension(140, 90));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());
	}

	/** 
	 * Updates the calendar display 
	 */
	public void update() {
		if (solOfMonthCache != marsTime.getSolOfMonth()) {
			solOfMonthCache = marsTime.getSolOfMonth();
			repaint();
		}
	}

	/** 
	 * Overrides paintComponent method.
	 * @param g graphics context
	 */
	@Override
	public void paintComponent(Graphics g) {

		// Paint dark green background
		g.setColor(new Color(0, 95, 0));
		g.fillRect(0, 0, 140, 90);

		// Paint mid green week day name boxes
		g.setColor(new Color(0, 127, 0));
		g.fillRect(0, 0, 140, 10);

		int solsInMonth = MarsClock.getSolsInMonth(marsTime.getMonth(), marsTime.getOrbit());

		// If sols in month are 27, black out lower left square
		if (solsInMonth == 27) {
			g.setColor(Color.black);
			g.fillRect(121, 71, 138, 88);
		}

		// Paint green rectangle
		g.setColor(Color.green);
		g.drawRect(0, 0, 139, 89);

		// Paint vertical day lines
		for (int x=1; x < 7; x++) {
			g.drawLine(20 * x, 0, 20 * x, 89);
		}

		// Paint horizontal lines
		for (int x=0; x < 4; x++) {
			g.drawLine(0, (20 * x) + 10, 139, (20 * x) + 10);
		}

		// Set up week letter font
		Font weekFont = new Font("SansSerif", Font.PLAIN, 8);
		FontMetrics weekMetrics = getFontMetrics(weekFont);
		int weekHeight = weekMetrics.getAscent();        

		// Draw week letters
		g.setFont(weekFont);
		char[] weekLetters = {'S', 'P', 'D', 'T', 'H', 'V', 'J'};
		for (int x=0; x < 7; x++) {
			int letterWidth = weekMetrics.charWidth(weekLetters[x]);
			g.drawString("" + weekLetters[x], (20 * x) + 11 - (letterWidth / 2), weekHeight - 1);
		}

		// Set up Sol number font
		Font solFont = new Font("SansSerif", Font.BOLD, 10);
		FontMetrics solMetrics = getFontMetrics(solFont);
		int solHeight = solMetrics.getAscent();

		// Draw sol letters
		g.setFont(solFont);
		for (int y=0; y < 4; y++) {
			for (int x=0; x < 7; x++) {
				int solNumber = (y * 7) + x + 1; 
				int solNumberWidth = solMetrics.stringWidth("" + solNumber);
				int xPos = (20 * x) + 11 - (solNumberWidth / 2);
				int yPos = (20 * y) + 30 - (solHeight / 2);
				if (solNumber <= solsInMonth) 
					g.drawString(Integer.toString(solNumber), xPos, yPos);
				if (solNumber == marsTime.getSolOfMonth()) {
					g.fillRect((20 * x) + 2, (20 * y) + 12, 17, 17);
					g.setColor(Color.black);
					g.drawString(Integer.toString(solNumber), xPos, yPos);
					g.setColor(Color.green);
				}
			}
		}
	}
}