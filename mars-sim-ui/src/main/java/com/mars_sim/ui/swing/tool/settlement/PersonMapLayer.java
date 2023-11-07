/**
 * Mars Simulation Project
 * PersonMapLayer.java
 * @date 2023-11-06
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.Iterator;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;

/**
 * A settlement map layer for displaying people.
 */
public class PersonMapLayer implements SettlementMapLayer {

	// Static members
	private final Color maleColor = LabelMapLayer.maleColor;
	private final Color maleOutline = LabelMapLayer.maleOutline;
	private final Color maleSelected = LabelMapLayer.maleSelected;
	private final Color maleSelectedOutline = LabelMapLayer.maleSelectedOutline;

	private final Color femaleColor = LabelMapLayer.femaleColor;
	private final Color femaleOutline = LabelMapLayer.femaleOutline;
	private final Color femaleSelected = LabelMapLayer.femaleSelected;
	private final Color femaleSelectedOutline = LabelMapLayer.femaleSelectedOutline;
	
	// Data members
	private SettlementMapPanel mapPanel;
	

	/**
	 * Constructor.
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
	 * Draws people at a settlement.
	 * 
	 * @param g2d the graphics context.
	 * @param settlement the settlement to draw people at.
	 */
	private void drawPeople(Graphics2D g2d, Settlement settlement, double scale) {

		Collection<Person> people = CollectionUtils.getPeopleInSettlementVicinity(settlement);
		// Note: Cannot use settlement.getPeopleInVicinity() since it won't include visitors 
		// people.addAll(settlement.getIndoorPeople());
		
		Person selectedPerson = mapPanel.getSelectedPerson();

		// Draw all people except selected person.
		Iterator<Person> i = people.iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (!person.equals(selectedPerson)) {
				boolean male = person.getGender().equals(GenderType.MALE);
				drawPerson(g2d, person, (male ? maleColor : femaleColor), 
							   (male ? maleOutline : femaleOutline), scale);
			}
		}

		// Draw selected person.
		if (people.contains(selectedPerson)) {
			boolean male = selectedPerson.getGender().equals(GenderType.MALE);
			drawPerson(g2d, selectedPerson, (male ? maleSelected : femaleSelected),
					   (male ? maleSelectedOutline : femaleSelectedOutline), scale);
		}
	}

	/**
	 * Draws a person at a settlement.
	 * 
	 * @param g2d the graphics context.
	 * @param person the person to draw.
	 */
	private void drawPerson(Graphics2D g2d, Person person, Color iconColor, Color outlineColor, double scale) {

		int size = (int)(Math.round(scale / 3.0));
		size = Math.max(size, 4);
		
		double radius = size / 2.0;
		
		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();

		double translationX = -1.0 * person.getPosition().getX() * scale - radius;
		double translationY = -1.0 * person.getPosition().getY() * scale - radius;

		// Apply graphic transforms for label.
		AffineTransform newTransform = new AffineTransform(saveTransform);
		newTransform.translate(translationX, translationY);
		newTransform.rotate(mapPanel.getRotation() * -1D, radius, radius);
		g2d.setTransform(newTransform);

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
