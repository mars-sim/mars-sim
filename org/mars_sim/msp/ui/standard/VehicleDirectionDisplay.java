/**
 * Mars Simulation Project
 * VehicleDirectionDisplay.java
 * @version 2.71 2000-10-30
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import org.mars_sim.msp.simulation.*;  
import java.awt.*;
import javax.swing.*;

/** The VehicleDirectionDisplay class displays a UI graph of the direction of travel
 *  for a ground vehicle's detail window.
 */
public class VehicleDirectionDisplay extends JComponent {

	// Data members
	private Direction direction; // Direction of travel
	private boolean showLine;    // True if direction line is to be shown

	/** Constructs a VehicleDirectionDisplay object 
     *  @param direction the vehicle's current direction
     *  @param park true if vehicle is parked
     */
	public VehicleDirectionDisplay(Direction direction, boolean park) {
		super();
		
		// Set component size	
		setPreferredSize(new Dimension(50, 50));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());

		// Initialize data members
		this.direction = direction;
		if (park) showLine = false;
		else showLine = true;
	}

	/** Update direction and redraw if necessary 
     *  @param newDirection vehicle's current direction
     *  @param park true if vehicle is currently parked
     */
	public void updateDirection(Direction newDirection, boolean park) {

		if (!newDirection.equals(direction)) {
			direction = (Direction) newDirection.clone();
			if (park) showLine = false;
			else showLine = true;
			
			repaint();
		}
	}

	/** Override paintComponent method 
     *  @param g graphics context
     */
	public void paintComponent(Graphics g) {

		// Draw black background
		g.setColor(Color.black);
		g.fillRect(0, 0, 50, 50);

		// Draw dark green background circle 
		g.setColor(new Color(0, 62, 0));
		g.fillOval(3, 3, 43, 43);

		// Draw bright green out circle
		g.setColor(Color.green);
		g.drawOval(3, 3, 43, 43);

		// Draw center dot
		g.drawRect(25, 25, 1, 1);

		// Prepare letter font
		Font tempFont = new Font("Helvetica", Font.PLAIN, 9);
		g.setFont(tempFont);
		FontMetrics tempMetrics = getFontMetrics(tempFont);
		int fontHeight = tempMetrics.getAscent();

		// Draw 'N'
		int nWidth = tempMetrics.charWidth('N');
		g.drawString("N", 25 - (nWidth / 2), (fontHeight / 2) + 10);

		// Draw 'S'
		int sWidth = tempMetrics.charWidth('S');
		g.drawString("S", 25 - (sWidth / 2), 39 + (fontHeight / 2));

		// Draw 'W'
		int wWidth = tempMetrics.charWidth('W');
		g.drawString("W", 10 - (wWidth / 2), 25 + (fontHeight / 2));

		// Draw 'E'
		int eWidth = tempMetrics.charWidth('E');
		g.drawString("E", 39 - (eWidth / 2), 25 + (fontHeight / 2));

		// Draw direction line if necessary
		if (showLine) {
			double hyp = (double)(22);
			int newX = (int)Math.round(hyp * direction.getSinDirection());
			int newY = -1 * (int)Math.round(hyp * direction.getCosDirection());
			g.drawLine(25, 25, newX + 25, newY + 25);
		}
	}
}
