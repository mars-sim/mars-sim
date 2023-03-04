/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The TabPanelGeneral is a tab panel for general information about a person.
 */
@SuppressWarnings("serial")
public class TabPanelGeneral extends TabPanel {

	private static final String ID_ICON = "info"; //$NON-NLS-1$
	
	private static final String TAB_BIRTH_DATE_AGE = "TabPanelGeneral.birthDateAndAge";
	
	/** The Person instance. */
	private Person person;
	
	private JLabel birthDateTF;
	
	private String birthDate;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelGeneral(Person unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelGeneral.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(ID_ICON),		
			Msg.getString("TabPanelGeneral.title"), //$NON-NLS-1$
			desktop
		);

		person = unit;
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel(8);
		
		content.add(infoPanel, BorderLayout.NORTH);

		// Prepare gender textfield
		String gender = person.getGender().getName();
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.gender"), gender,null);
		
		// Prepare birthdate and age textfield
		birthDate = person.getBirthDate();
		String birthTxt = Msg.getString(
			TAB_BIRTH_DATE_AGE,
			birthDate,
			Integer.toString(person.getAge())); //$NON-NLS-1$
		birthDateTF = infoPanel.addTextField(Msg.getString("TabPanelGeneral.birthDate"), birthTxt, null);

		// Prepare birth location textfield
		String birthLocation = person.getBirthplace();
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.birthLocation"), //$NON-NLS-1$
				birthLocation, null);		
		
		// Prepare country of origin textfield
		String country = person.getCountry();
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.country"), //$NON-NLS-1$
				country, null);

		// Prepare weight textfield
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.weight"), //$NON-NLS-1$
				  							  StyleManager.DECIMAL_KG.format(person.getBaseMass()), null);
		
		// Prepare height name label
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.height"), //$NON-NLS-1$
					 StyleManager.DECIMAL_PLACES1.format(person.getHeight()) + " m", null);

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
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.bmi"), //$NON-NLS-1$
				BMItext, null); 
		
		// Prepare loading cap label
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.loadCap"), //$NON-NLS-1$
				StyleManager.DECIMAL_KG.format(person.getCarryingCapacity()), null); 
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
