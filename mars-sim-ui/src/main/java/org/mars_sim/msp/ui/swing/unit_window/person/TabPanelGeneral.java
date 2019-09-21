/**
1
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @version 3.1.0 2017-01-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelGeneral is a tab panel for general information about a person.
 */
@SuppressWarnings("serial")
public class TabPanelGeneral extends TabPanel {

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelGeneral(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelGeneral.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelGeneral.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Person person = (Person) unit;

		// Create general panel.
		JPanel generalLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(generalLabelPanel);

		// Prepare general label
		JLabel generalLabel = new JLabel(Msg.getString("TabPanelGeneral.label"), JLabel.CENTER); //$NON-NLS-1$
		generalLabel.setFont(new Font("Serif", Font.BOLD, 16));
		generalLabelPanel.add(generalLabel);

		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new SpringLayout());//GridLayout(7, 2, 0, 0));
//		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// Prepare gender name label
		JLabel genderNameLabel = new JLabel(Msg.getString("TabPanelGeneral.gender"), JLabel.RIGHT); //$NON-NLS-1$
		genderNameLabel.setSize(5, 2);
		infoPanel.add(genderNameLabel);

		// Prepare gender label
		String gender = person.getGender().getName();
		JTextField genderTF = new JTextField(Conversion.capitalize(gender));
		genderTF.setEditable(false);
		genderTF.setColumns(12);
		//JLabel genderLabel = new JLabel(gender, JLabel.RIGHT);
		infoPanel.add(genderTF);

		// Prepare birthdate and age name label
		JLabel birthNameLabel = new JLabel(Msg.getString("TabPanelGeneral.birthDate"), JLabel.RIGHT); //$NON-NLS-1$
		birthNameLabel.setSize(5, 2);
		infoPanel.add(birthNameLabel);

		// Prepare birthdate and age label
		String birthdate = Msg.getString(
			"TabPanelGeneral.birthDateAndAge",
			person.getBirthDate(),
			Integer.toString(person.updateAge())
		); //$NON-NLS-1$
		//JLabel birthDateLabel = new JLabel(birthdate, JLabel.RIGHT);
		JTextField birthDateTF = new JTextField(birthdate);
		birthDateTF.setEditable(false);
		birthDateTF.setColumns(12);
		infoPanel.add(birthDateTF);

		// Prepare birth location name label
		JLabel birthLocationNameLabel = new JLabel(Msg.getString("TabPanelGeneral.birthLocation"), JLabel.RIGHT); //$NON-NLS-1$
		birthLocationNameLabel.setSize(5, 2);
		infoPanel.add(birthLocationNameLabel);

		// Prepare birth location label
		String birthLocation = person.getBirthplace();
		//JLabel birthLocationLabel = new JLabel(birthLocation, JLabel.RIGHT);
		JTextField birthLocationTF = new JTextField(Conversion.capitalize(birthLocation));
		birthLocationTF.setEditable(false);
		birthLocationTF.setColumns(12);
		infoPanel.add(birthLocationTF);

		// Prepare country name label
		JLabel countryNameLabel = new JLabel(Msg.getString("TabPanelGeneral.country"), JLabel.RIGHT); //$NON-NLS-1$
		countryNameLabel.setSize(5, 2);
		infoPanel.add(countryNameLabel);

		// Prepare birth location label
		String country = person.getCountry();
		JTextField countryTF = new JTextField(Conversion.capitalize(country));
		countryTF.setEditable(false);
		countryTF.setColumns(15);
		infoPanel.add(countryTF);

		// Prepare weight name label
		JLabel weightNameLabel = new JLabel(Msg.getString("TabPanelGeneral.weight"), JLabel.RIGHT); //$NON-NLS-1$
		weightNameLabel.setSize(5, 2);
		infoPanel.add(weightNameLabel);

		// Prepare weight label
		double baseMass = Math.round(person.getBaseMass()*10.0)/10.0;
		//JLabel weightLabel = new JLabel(Msg.getString("TabPanelGeneral.kilograms",baseMass), JLabel.RIGHT); //$NON-NLS-1$
		JTextField weightTF = new JTextField(Msg.getString("TabPanelGeneral.kilograms", baseMass));
		weightTF.setEditable(false);
		weightTF.setColumns(12);
		infoPanel.add(weightTF);

		// Prepare height name label
		JLabel heightNameLabel = new JLabel(Msg.getString("TabPanelGeneral.height"), JLabel.RIGHT); //$NON-NLS-1$
		heightNameLabel.setSize(5, 2);
		infoPanel.add(heightNameLabel);

		// Prepare height label
		double baseHeight = Math.round(person.getHeight()*10.0)/10.0;
		//JLabel heightLabel = new JLabel(Msg.getString("TabPanelGeneral.centimeters", baseHeight), JLabel.RIGHT); //$NON-NLS-1$
		JTextField heightTF = new JTextField(Msg.getString("TabPanelGeneral.centimeters", baseHeight));
		heightTF.setEditable(false);
		heightTF.setColumns(12);
		infoPanel.add(heightTF);

		// Prepare BMI name label
		JLabel BMINameLabel = new JLabel(Msg.getString("TabPanelGeneral.bmi"), JLabel.RIGHT); //$NON-NLS-1$
		BMINameLabel.setSize(5, 2);
		infoPanel.add(BMINameLabel);

		// Prepare BMI label
		double height = person.getHeight()/100D;
		double heightSquared = height*height;
		double BMI = person.getBaseMass()/heightSquared;
		// categorize according to general weight class
		String weightClass = Msg.getString("TabPanelGeneral.bmi.underweight"); //$NON-NLS-1$
		if (BMI > 18.5) {weightClass = Msg.getString("TabPanelGeneral.bmi.normal");} //$NON-NLS-1$
		if (BMI > 24.9) {weightClass = Msg.getString("TabPanelGeneral.bmi.overweight");} //$NON-NLS-1$
		if (BMI > 29.9) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese1");} //$NON-NLS-1$
		if (BMI > 34.9) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese2");} //$NON-NLS-1$
		if (BMI > 39.9) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese3");} //$NON-NLS-1$

		//JLabel BMILabel = new JLabel(Msg.getString("TabPanelGeneral.bmiValue", //$NON-NLS-1$
		//		Integer.toString((int)BMI),	weightClass), JLabel.RIGHT);
		JTextField BMITF = new JTextField(Msg.getString("TabPanelGeneral.bmiValue", //$NON-NLS-1$
				Integer.toString((int)BMI),	weightClass));
		BMITF.setEditable(false);
		BMITF.setColumns(12);
		infoPanel.add(BMITF);

		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(infoPanel,
		                                7, 2, //rows, cols
		                                50, 10,        //initX, initY
		                                10, 5);       //xPad, yPad
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