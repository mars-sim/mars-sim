/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelGeneral is a tab panel for general information about a person.
 */
@SuppressWarnings("serial")
public class TabPanelGeneral extends TabPanel {

	private static final String ID_ICON = Msg.getString("icon.id"); //$NON-NLS-1$
	
	private static final String TAB_BIRTH_DATE_AGE = "TabPanelGeneral.birthDateAndAge";
	
	/** The Person instance. */
	private Person person;
	
	private JTextField birthDateTF;
	
	private String birthDate;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelGeneral(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelGeneral.title"), //$NON-NLS-1$
			ImageLoader.getNewIcon(ID_ICON),		
			Msg.getString("TabPanelGeneral.title"), //$NON-NLS-1$
			unit, desktop
		);

		person = (Person) unit;
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new SpringLayout());
		content.add(infoPanel, BorderLayout.NORTH);

		// Prepare gender textfield
		String gender = person.getGender().getName();
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.gender"), gender, 5, null);
		
		// Prepare birthdate and age textfield
		birthDate = person.getBirthDate();
		String birthTxt = Msg.getString(
			TAB_BIRTH_DATE_AGE,
			birthDate,
			Integer.toString(person.getAge())); //$NON-NLS-1$
		birthDateTF = addTextField(infoPanel, Msg.getString("TabPanelGeneral.birthDate"), birthTxt, 16, null);

		// Prepare birth location textfield
		String birthLocation = person.getBirthplace();
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.birthLocation"), //$NON-NLS-1$
				birthLocation, 17, null);		
		
		// Prepare country of origin textfield
		String country = person.getCountry();
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.country"), //$NON-NLS-1$
				country, 12, null);

		// Prepare weight textfield
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.weight"), //$NON-NLS-1$
				  							  DECIMAL_KG.format(person.getBaseMass()), 6, null);
		
		// Prepare height name label
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.height"), //$NON-NLS-1$
					 DECIMAL_PLACES1.format(person.getHeight()) + " m", 6, null);

		// Prepare BMI label
		double height = person.getHeight()/100D;
		double heightSquared = height*height;
		double BMI = person.getBaseMass()/heightSquared;
		
		// Categorize according to general weight class
		String weightClass = "";
		if (BMI <= 18.5) {weightClass = Msg.getString("TabPanelGeneral.bmi.underweight");} //$NON-NLS-1$
		if (BMI > 18.5) {weightClass = Msg.getString("TabPanelGeneral.bmi.normal");} //$NON-NLS-1$
		if (BMI > 24.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.overweight");} //$NON-NLS-1$
		if (BMI > 29.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese1");} //$NON-NLS-1$
		if (BMI > 34.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese2");} //$NON-NLS-1$
		if (BMI > 39.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese3");} //$NON-NLS-1$

		String BMItext = Msg.getString("TabPanelGeneral.bmiValue", //$NON-NLS-1$
				Math.round(BMI*100.0)/100.0, weightClass);
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.bmi"), //$NON-NLS-1$
				BMItext, 10, null); 
		
		// Prepare loading cap label
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.loadCap"), //$NON-NLS-1$
				DECIMAL_KG.format(person.getCarryingCapacity()), 6, null); 
		
		// Use spring panel layout.
		SpringUtilities.makeCompactGrid(infoPanel,
		                                8, 2,	//rows, cols
		                                95, 2,	//initX, initY
		                                XPAD_DEFAULT, YPAD_DEFAULT);       //xPad, yPad
	}
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		
		String newBD = person.getBirthDate();
		
		if (!newBD.equalsIgnoreCase(birthDate)) {
			// Update the age
			String birthdate = Msg.getString(
				TAB_BIRTH_DATE_AGE,
				newBD,
				Integer.toString(person.getAge())); 
					
			birthDateTF.setText(birthdate); 
		}
	}
}
