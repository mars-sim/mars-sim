/**
 * Mars Simulation Project
 * PersonMapLayer.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A settlement map layer for displaying people.
 */
public class PersonMapLayer implements SettlementMapLayer {

	// Static members
	private static final Color MALE_COLOR = LabelMapLayer.MALE_COLOR;
	private static final Color MALE_OUTLINE_COLOR = LabelMapLayer.MALE_OUTLINE_COLOR;
	private static final Color MALE_SELECTED_COLOR = LabelMapLayer.MALE_SELECTED_COLOR;
	private static final Color MALE_SELECTED_OUTLINE_COLOR = LabelMapLayer.MALE_SELECTED_OUTLINE_COLOR;

	private static final Color FEMALE_COLOR = LabelMapLayer.FEMALE_COLOR;
	private static final Color FEMALE_OUTLINE_COLOR = LabelMapLayer.FEMALE_OUTLINE_COLOR;
	private static final Color FEMALE_SELECTED_COLOR = LabelMapLayer.FEMALE_SELECTED_COLOR;
	private static final Color FEMALE_SELECTED_OUTLINE_COLOR = LabelMapLayer.FEMALE_SELECTED_OUTLINE_COLOR;
	
	// Data members
	private SettlementMapPanel mapPanel;
	

	/**
	 * Constructor
	 * 
	 * @param mapPanel the settlement map panel.
	 */
	public PersonMapLayer(SettlementMapPanel mapPanel) {
		// Initialize data members.
		this.mapPanel = mapPanel;
	}
	
	@Override
	public void displayLayer(
		Graphics2D g2d, Settlement settlement, Building building,
		double xPos, double yPos, int mapWidth, int mapHeight,
		double rotation, double scale
	) {

		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();

		// Get the map center point.
		double mapCenterX = mapWidth / 2D;
		double mapCenterY = mapHeight / 2D;

		// Translate map from settlement center point.
		g2d.translate(mapCenterX + (xPos * scale), mapCenterY + (yPos * scale));

		// Rotate map from North.
		g2d.rotate(rotation, 0D - (xPos * scale), 0D - (yPos * scale));

		// Draw all people.
		drawPeople(g2d, settlement, scale);

		// Restore original graphic transforms.
		g2d.setTransform(saveTransform);
	}


	/**
	 * Draw people at a settlement.
	 * @param g2d the graphics context.
	 * @param settlement the settlement to draw people at.
	 */
	private void drawPeople(Graphics2D g2d, Settlement settlement, double scale) {

		List<Person> people = CollectionUtils.getPeopleToDisplay(settlement);
		Person selectedPerson = mapPanel.getSelectedPerson();

		// Draw all people except selected person.
		Iterator<Person> i = people.iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (!person.equals(selectedPerson)) {
				if (person.isMale())
					drawPerson(g2d, person, MALE_COLOR, MALE_OUTLINE_COLOR, scale);
				else 
					drawPerson(g2d, person, FEMALE_COLOR, FEMALE_OUTLINE_COLOR, scale);
			}
		}

		// Draw selected person.
		if (people.contains(selectedPerson)) {
			if (selectedPerson.isMale())
				drawPerson(g2d, selectedPerson, MALE_SELECTED_COLOR, MALE_SELECTED_OUTLINE_COLOR, scale);
			else 
				drawPerson(g2d, selectedPerson, FEMALE_SELECTED_COLOR, FEMALE_SELECTED_OUTLINE_COLOR, scale);
		}
	}

	/**
	 * Draw a person at a settlement.
	 * @param g2d the graphics context.
	 * @param person the person to draw.
	 */
	private void drawPerson(Graphics2D g2d, Person person, Color iconColor, Color outlineColor, double scale) {

		int size = (int)(Math.round(scale / 3.0));
		size = Math.max(size, 4);
		
//		if (sizeCache != size) {
//			sizeCache = size;
//			System.out.println("size : " + size);	
//		}
		
//		int size1 = (int)(Math.round(size * 1.1));
		
		double radius = size / 2.0;
		
		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();

		double translationX = -1.0 * person.getXLocation() * scale - radius;
		double translationY = -1.0 * person.getYLocation() * scale - radius;

		// Apply graphic transforms for label.
		AffineTransform newTransform = new AffineTransform(saveTransform);
		newTransform.translate(translationX, translationY);
		newTransform.rotate(mapPanel.getRotation() * -1D, radius, radius);
		g2d.setTransform(newTransform);

//		// Set color outline color.
//		g2d.setColor(outlineColor);
//		
//		// Draw outline circle.
//		g2d.fillOval(0,  0, size1, size1);
		
		// Set circle color.
		g2d.setColor(iconColor);
		
		// Draw circle
		g2d.fillOval(0, 0, size, size);

		// Restore original graphic transforms.
		g2d.setTransform(saveTransform);

	}

	@Override
	public void destroy() {
		mapPanel = null;
	}
}
