/**
 * Mars Simulation Project
 * GeneralTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The GeneralTabPanel is a tab panel for general information about a person.
 */
public class GeneralTabPanel
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public GeneralTabPanel(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("GeneralTabPanel.title"), //$NON-NLS-1$
			null,
			Msg.getString("GeneralTabPanel.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Person person = (Person) unit;

		// Create general label panel.
		JPanel generalLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(generalLabelPanel);

		// Prepare general label
		JLabel generalLabel = new JLabel(Msg.getString("GeneralTabPanel.label"), JLabel.CENTER); //$NON-NLS-1$
		generalLabelPanel.add(generalLabel);

		// Prepare info panel.
		JPanel infoPanel = new JPanel(new GridLayout(7, 2, 0, 0));
		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// Prepare gender name label
		JLabel genderNameLabel = new JLabel(Msg.getString("GeneralTabPanel.gender"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(genderNameLabel);

		// Prepare gender label
		String gender = person.getGender().getName();
		JLabel genderLabel = new JLabel(gender, JLabel.RIGHT);
		infoPanel.add(genderLabel);

		// Prepare birthdate and age name label
		JLabel birthNameLabel = new JLabel(Msg.getString("GeneralTabPanel.birthDate"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(birthNameLabel);

		// Prepare birthdate and age label
		String birthdate = Msg.getString(
			"GeneralTabPanel.birthDateAndAge",
			person.getBirthDate(),
			Integer.toString(person.getAge())
		); //$NON-NLS-1$
		JLabel birthDateLabel = new JLabel(birthdate, JLabel.RIGHT);
		infoPanel.add(birthDateLabel);

		// Prepare birth location name label
		JLabel birthLocationNameLabel = new JLabel(Msg.getString("GeneralTabPanel.birthLocation"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(birthLocationNameLabel);

		// Prepare birth location label
		String birthLocation = person.getBirthplace();
		JLabel birthLocationLabel = new JLabel(birthLocation, JLabel.RIGHT);
		infoPanel.add(birthLocationLabel);

		// Prepare weight name label
		JLabel weightNameLabel = new JLabel(Msg.getString("GeneralTabPanel.weight"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(weightNameLabel);

		// Prepare weight label
		double baseMass = person.getBaseMass();
		JLabel weightLabel = new JLabel(Msg.getString("GeneralTabPanel.kilograms",baseMass), JLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(weightLabel);

		// Prepare height name label
		JLabel heightNameLabel = new JLabel(Msg.getString("GeneralTabPanel.height"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(heightNameLabel);

		// Prepare height label
		int baseHeight = person.getHeight();
		JLabel heightLabel = new JLabel(Msg.getString("GeneralTabPanel.centimeters",baseHeight), JLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(heightLabel);

		// Prepare BMI name label
		JLabel BMINameLabel = new JLabel(Msg.getString("GeneralTabPanel.bmi"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(BMINameLabel);

		// Prepare BMI label
		double heightInCmSquared = (person.getHeight()/100D)*(person.getHeight()/100D);
		double BMI = (person.getBaseMass()/heightInCmSquared);
		// categorize according to general weight class
		String weightClass = Msg.getString("GeneralTabPanel.bmi.underweight"); //$NON-NLS-1$
		if (BMI > 18.5) {weightClass = Msg.getString("GeneralTabPanel.bmi.normal");} //$NON-NLS-1$
		if (BMI > 25) {weightClass = Msg.getString("GeneralTabPanel.bmi.overweight");} //$NON-NLS-1$
		if (BMI > 30) {weightClass = Msg.getString("GeneralTabPanel.bmi.obese");} //$NON-NLS-1$
		JLabel BMILabel = new JLabel(
			Msg.getString(
				"GeneralTabPanel.bmiValue", //$NON-NLS-1$
				Integer.toString((int)BMI),
				weightClass
			),JLabel.RIGHT
		);
		infoPanel.add(BMILabel);


		// Prepare personality name label
		JLabel personalityNameLabel = new JLabel(Msg.getString("GeneralTabPanel.personalityMBTI"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(personalityNameLabel);

		// Prepare personality label
		String personality = person.getMind().getPersonalityType().getTypeString();
		JLabel personalityLabel = new JLabel(personality, JLabel.RIGHT);
		infoPanel.add(personalityLabel);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		// Person person = (Person) unit;
		// Fill in as we have more to update on this panel.
	}
}