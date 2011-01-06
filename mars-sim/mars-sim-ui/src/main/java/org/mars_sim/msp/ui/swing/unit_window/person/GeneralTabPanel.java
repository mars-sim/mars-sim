/**
 * Mars Simulation Project
 * GeneralTabPanel.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import javax.swing.*;
import java.awt.*;



/**
 * The GeneralTabPanel is a tab panel for general information about a person.
 */
public class GeneralTabPanel extends TabPanel {

	/**
	 * Constructor
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public GeneralTabPanel(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super("General", null, "General Info", unit, desktop);
        
		Person person = (Person) unit;
		
		// Create general label panel.
		JPanel generalLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(generalLabelPanel);
        
		// Prepare general label
		JLabel generalLabel = new JLabel("General Info", JLabel.CENTER);
		generalLabelPanel.add(generalLabel);
		
		// Prepare info panel.
		JPanel infoPanel = new JPanel(new GridLayout(4, 2, 0, 0));
		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);
		
		// Prepare gender name label
		JLabel genderNameLabel = new JLabel("Gender", JLabel.LEFT);
		infoPanel.add(genderNameLabel);

		// Prepare gender label
		String gender = person.getGender().substring(0, 1).toUpperCase() + person.getGender().substring(1);
		JLabel genderLabel = new JLabel(gender, JLabel.RIGHT);
		infoPanel.add(genderLabel);

		// Prepare birthdate name label
		JLabel birthNameLabel = new JLabel("Birth Date", JLabel.LEFT);
		infoPanel.add(birthNameLabel);
		
		// Prepare birthdate label
		String birthdate = person.getBirthDate();
		JLabel birthDateLabel = new JLabel(birthdate, JLabel.RIGHT);
		infoPanel.add(birthDateLabel);

		// Prepare age name label
		JLabel ageNameLabel = new JLabel("Age", JLabel.LEFT);
		infoPanel.add(ageNameLabel);
		
		// Prepare age label
		String age = Integer.toString(person.getAge());
		JLabel ageLabel = new JLabel(age, JLabel.RIGHT);
		infoPanel.add(ageLabel);
		
		// Prepare personality name label
		JLabel personalityNameLabel = new JLabel("Personality (MBTI)", JLabel.LEFT);
		infoPanel.add(personalityNameLabel);
		
		// Prepare personality label
		String personality = person.getMind().getPersonalityType().getTypeString();
		JLabel personalityLabel = new JLabel(personality, JLabel.RIGHT);
		infoPanel.add(personalityLabel);
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		// Person person = (Person) unit;
		// Fill in as we have more to update on this panel.
	}
}