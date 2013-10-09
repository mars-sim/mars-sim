/**
 * Mars Simulation Project
 * GeneralTabPanel.java
 * @version 3.06 2013-10-03
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
		JPanel infoPanel = new JPanel(new GridLayout(7, 2, 0, 0));
		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);
		
		// Prepare gender name label
		JLabel genderNameLabel = new JLabel("Gender", JLabel.LEFT);
		infoPanel.add(genderNameLabel);

		// Prepare gender label
		String gender = person.getGender().substring(0, 1).toUpperCase() + person.getGender().substring(1);
		JLabel genderLabel = new JLabel(gender, JLabel.RIGHT);
		infoPanel.add(genderLabel);

		// Prepare birthdate and age name label
		JLabel birthNameLabel = new JLabel("Birth Date", JLabel.LEFT);
		infoPanel.add(birthNameLabel);
		
		// Prepare birthdate and age label
		String birthdate = person.getBirthDate()+" ("+person.getAge()+" y)";
		JLabel birthDateLabel = new JLabel(birthdate, JLabel.RIGHT);
		infoPanel.add(birthDateLabel);

		// Prepare birth location name label
		JLabel birthLocationNameLabel = new JLabel("Birth Location", JLabel.LEFT);
		infoPanel.add(birthLocationNameLabel);
		
		// Prepare birth location label
		String birthLocation = person.getBirthplace();
		JLabel birthLocationLabel = new JLabel(birthLocation, JLabel.RIGHT);
		infoPanel.add(birthLocationLabel);
		
		// Prepare weight name label
		JLabel weightNameLabel = new JLabel("Weight", JLabel.LEFT);
		infoPanel.add(weightNameLabel);
		
		// Prepare weight label
		double baseMass = person.getBaseMass();
		JLabel weightLabel = new JLabel(baseMass+" kg", JLabel.RIGHT);
		infoPanel.add(weightLabel);

		// Prepare height name label
		JLabel heightNameLabel = new JLabel("Height", JLabel.LEFT);
		infoPanel.add(heightNameLabel);
		
		// Prepare height label
		int baseHeight = person.getHeight();
		JLabel heightLabel = new JLabel(baseHeight+" cm", JLabel.RIGHT);
		infoPanel.add(heightLabel);

		// Prepare BMI name label
		JLabel BMINameLabel = new JLabel("BMI", JLabel.LEFT);
		infoPanel.add(BMINameLabel);
		
		// Prepare BMI label
		double heightInCmSquared = (person.getHeight()/100D)*(person.getHeight()/100D);
		double BMI = (person.getBaseMass()/heightInCmSquared);
			// categorize according to general weight class
			String weightClass= "Underweight";
			if (BMI > 18.5)  {weightClass ="Normal";}
			if (BMI > 25)  {weightClass ="Overweight";}
			if (BMI > 30)  {weightClass ="Obese";}
		JLabel BMILabel = new JLabel(String.valueOf((int)BMI)+" ("+weightClass+")", JLabel.RIGHT);
		infoPanel.add(BMILabel);
			
		
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