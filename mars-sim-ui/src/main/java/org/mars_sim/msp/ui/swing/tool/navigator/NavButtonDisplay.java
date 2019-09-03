/**
 * Mars Simulation Project
 * NavButtonDisplay.java
 * @version 3.1.0 2018-07-23
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.ImageLoader;

import com.alee.extended.WebComponent;
import com.alee.managers.style.StyleId;

/** 
 * The NavButtonDisplay class is a component that displays and
 * implements the behavior of the navigation buttons which control
 * the globe and map.
 */
public class NavButtonDisplay
extends WebComponent
implements MouseListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(NavButtonDisplay.class.getName());

	// Constant data members
	/** Circular degree unit. */
	static final double RAD_PER_DEGREE = Math.PI / 180D;

	// Data members
	/** Parent NavigatorWindow. */
	private NavigatorWindow parentNavigator;
	/** Button currently lit, -1 otherwise. */
	private int buttonLight;
	/** Unlit buttons image. */
	private Image navMain;
	/** Current coordinates. */
	private Coordinates centerCoords;
	/** Lit button images. */
	private final Image[] lightUpButtons;
	/** Regions encompassing images. */
	private final Rectangle[] hotSpots;

	/**
	 * Constructor.
	 * @param parentNavigator the navigator window pane
	 */
	public NavButtonDisplay(NavigatorWindow parentNavigator) {

		// Set component size
		setPreferredSize(new Dimension(150, 150));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());

		// Set mouse listener
		addMouseListener(this);

		// Initialize globals
		centerCoords = new Coordinates(Math.PI / 2D, 0D);
		buttonLight = -1;
		lightUpButtons = new Image[9];
		this.parentNavigator = parentNavigator;

		// Load Button Images
		navMain = ImageLoader.getImage(Msg.getString("img.nav.main")); //$NON-NLS-1$
		lightUpButtons[0] = ImageLoader.getImage(Msg.getString("img.nav.plus.main")); //$NON-NLS-1$
		lightUpButtons[1] = ImageLoader.getImage(Msg.getString("img.nav.north")); //$NON-NLS-1$
		lightUpButtons[2] = ImageLoader.getImage(Msg.getString("img.nav.south")); //$NON-NLS-1$
		lightUpButtons[3] = ImageLoader.getImage(Msg.getString("img.nav.east")); //$NON-NLS-1$
		lightUpButtons[4] = ImageLoader.getImage(Msg.getString("img.nav.west")); //$NON-NLS-1$
		lightUpButtons[5] = ImageLoader.getImage(Msg.getString("img.nav.plus.north")); //$NON-NLS-1$
		lightUpButtons[6] = ImageLoader.getImage(Msg.getString("img.nav.plus.south")); //$NON-NLS-1$
		lightUpButtons[7] = ImageLoader.getImage(Msg.getString("img.nav.plus.east")); //$NON-NLS-1$
		lightUpButtons[8] = ImageLoader.getImage(Msg.getString("img.nav.plus.west")); //$NON-NLS-1$

		MediaTracker mtrack = new MediaTracker(this);

		mtrack.addImage(navMain, 0);
		for (int x = 0; x < 9; x++) {
			mtrack.addImage(lightUpButtons[x], x + 1);
		}

		try { mtrack.waitForAll(); }
		catch (InterruptedException e) {
			logger.log(Level.SEVERE,Msg.getString("NavButtonDisplay.log.mediaTrackerError", e.toString())); //$NON-NLS-1$
		}

		// Set hot spots for mouse clicks
		hotSpots = new Rectangle[9];
		hotSpots[0] = new Rectangle(45, 45, 60, 60);
		hotSpots[1] = new Rectangle(38, 16, 74, 21);
		hotSpots[2] = new Rectangle(38, 112, 74, 21);
		hotSpots[3] = new Rectangle(113, 38, 21, 74);
		hotSpots[4] = new Rectangle(17, 38, 21, 74);
		hotSpots[5] = new Rectangle(60, 0, 29, 14);
		hotSpots[6] = new Rectangle(60, 134, 29, 14);
		hotSpots[7] = new Rectangle(135, 61, 15, 28);
		hotSpots[8] = new Rectangle(0, 61, 15, 28);
	}

	/**
	 * Update coordinates
	 * @param newCenter the new center position
	 */
	public void updateCoords(Coordinates newCenter) {
		centerCoords.setCoords(newCenter);
	}

	/**
	 * Override paintComponent method. Paints buttons and lit button
	 * @param g graphics context
	 */
	public void paintComponent(Graphics g) {

		// paint black background
		g.setColor(Color.black);
		g.fillRect(0, 0, 150, 150);

		// draw main button image
		g.drawImage(navMain, 0, 0, this);

		// draw lit button over top
		if (buttonLight >= 0) {
			g.drawImage(lightUpButtons[buttonLight], 0, 0, this);
		}
	}

	// MouseListener methods overridden

	/** Light navigation button on mouse press */
	public void mousePressed(MouseEvent event) {
		lightButton(event.getX(), event.getY());
	}

	/** Perform appropriate action on mouse release. */
	public void mouseReleased(MouseEvent event) {

		unlightButtons();

		// Use Image Map Technique to Determine Which Button was Selected
		int spot = findHotSpot(event.getX(), event.getY());

		// Results Based on Button Selected
		switch (spot) {
		case 0: // Zoom Button
			parentNavigator.updateCoords(centerCoords);
			break;
		case 1: // Inner Top Arrow
			centerCoords.setPhi(centerCoords.getPhi() - (5D * RAD_PER_DEGREE));
			if (centerCoords.getPhi() < 0D)
				centerCoords.setPhi(0D);
			break;
		case 2: // Inner Bottom Arrow
			centerCoords.setPhi(centerCoords.getPhi() + (5D * RAD_PER_DEGREE));
			if (centerCoords.getPhi() > Math.PI)
				centerCoords.setPhi(Math.PI);
			break;
		case 3: // Inner Right Arrow
			centerCoords.setTheta(centerCoords.getTheta() + (5D * RAD_PER_DEGREE));
			if (centerCoords.getTheta() > (2D * Math.PI))
				centerCoords.setTheta(centerCoords.getTheta() - (2D * Math.PI));
			break;
		case 4: // Inner Left Arrow
			centerCoords.setTheta(centerCoords.getTheta() - (5D * RAD_PER_DEGREE));
			if (centerCoords.getTheta() < 0D)
				centerCoords.setTheta(centerCoords.getTheta() + (2D * Math.PI));
			break;
		case 5: // Outer Top Arrow
			centerCoords.setPhi(centerCoords.getPhi() - (30D * RAD_PER_DEGREE));
			if (centerCoords.getPhi() < 0D)
				centerCoords.setPhi(0D);
			break;
		case 6: // Outer Bottom Arrow
			centerCoords.setPhi(centerCoords.getPhi() + (30D * RAD_PER_DEGREE));
			if (centerCoords.getPhi() > Math.PI)
				centerCoords.setPhi(Math.PI);
			break;
		case 7: // Outer Right Arrow
			centerCoords.setTheta(centerCoords.getTheta() + (30D * RAD_PER_DEGREE));
			if (centerCoords.getTheta() >= (2D * Math.PI))
				centerCoords.setTheta(centerCoords.getTheta() - (2D * Math.PI));
			break;
		case 8: // Outer Left Arrow
			centerCoords.setTheta(centerCoords.getTheta() - (30D * RAD_PER_DEGREE));
			if (centerCoords.getTheta() < 0D)
				centerCoords.setTheta(centerCoords.getTheta() + (2D * Math.PI));
			break;
		}

		// Reposition Globe If Non-Zoom Button is Selected
		if (spot > 0)
			parentNavigator.updateGlobeOnly(centerCoords);
	}

	public void mouseClicked(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}

	/**
	 * Light button if mouse is on button.
	 * @param x x-position
	 * @param y y-position
	 */
	private void lightButton(int x, int y) {
		buttonLight = findHotSpot(x, y);
		if (buttonLight >= 0) {
			repaint();
		}
	}

	/** Unlight buttons if any are lighted. */
	private void unlightButtons() {
		if (buttonLight >= 0) {
			buttonLight = -1;
			repaint();
		}
	}

	/**
	 * Returns button number if mouse is on button.<br/>
	 * Returns -1 if not on button.<br/>
	 * Uses rectangular image mapping.
	 * @param x x-position
	 * @param y y-position
	 */
	private int findHotSpot(int x, int y) {
		for (int i = 0; i < 9; i++) {
			if (hotSpots[i].contains(x, y)) {
				return i;
			}
		}
		return -1;
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
