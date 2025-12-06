/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The TabPanelGeneral is a tab panel for general information about a person.
 */
@SuppressWarnings("serial")
public class TabPanelGeneral extends TabPanel {

	private static final String ID_ICON = "info"; //$NON-NLS-1$
	
	private static final String TAB_BIRTH_DATE_AGE = "TabPanelGeneral.birthDateAndAge";
	
	/** The Person instance. */
	private Person person;
	
	private JLabel storedMassLabel;
		
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

		// Prepare attribute panel.
		AttributePanel infoPanel = new AttributePanel();
		
		content.add(infoPanel, BorderLayout.NORTH);

		// Prepare gender textfield
		String gender = person.getGender().getName();
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.gender"), gender, null);
		
		// Prepare blood type
		String bloodType = person.getBloodType();
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.bloodType"), bloodType, null);
				
		// Prepare birthdate and age textfield
		var birthDate = person.getBirthDate();

		String birthTxt = Msg.getString(
			TAB_BIRTH_DATE_AGE,
			birthDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
			Integer.toString(person.getAge())); //$NON-NLS-1$
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.birthDate"), birthTxt, null);
		
		// Prepare country of origin textfield
		String country = person.getCountry();
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.country"), //$NON-NLS-1$
				country, null);
		infoPanel.addLabelledItem(Msg.getString("Entity.authority"), new EntityLabel(person.getReportingAuthority(), getDesktop()));

		// Prepare weight textfield
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.weight"), //$NON-NLS-1$
				  							  StyleManager.DECIMAL_KG.format(person.getBaseMass()), null);
		
		// Prepare height name label
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.height"), //$NON-NLS-1$
					 StyleManager.DECIMAL_PLACES1.format(person.getHeight()) + " cm", null);

		// Prepare BMI label
		double height = person.getHeight()/100D;
		double heightSquared = height*height;
		double bmi = person.getBaseMass()/heightSquared;
		
		// Categorize according to general weight class
		String weightClass = "";
		if (bmi <= 18.5) {weightClass = Msg.getString("TabPanelGeneral.bmi.underweight");} //$NON-NLS-1$
		if (bmi > 18.5) {weightClass = Msg.getString("TabPanelGeneral.bmi.normal");} //$NON-NLS-1$
		if (bmi > 24.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.overweight");} //$NON-NLS-1$
		if (bmi > 29.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese1");} //$NON-NLS-1$
		if (bmi > 34.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese2");} //$NON-NLS-1$
		if (bmi > 39.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese3");} //$NON-NLS-1$

		String bmiText = Msg.getString("TabPanelGeneral.bmiValue", //$NON-NLS-1$
				Math.round(bmi*100.0)/100.0, weightClass);
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.bmi"), //$NON-NLS-1$
				bmiText, null); 
		
		// Prepare loading cap label
		infoPanel.addTextField(Msg.getString("TabPanelGeneral.loadCap"), //$NON-NLS-1$
				StyleManager.DECIMAL_KG.format(person.getCarryingCapacity()), null); 
		
		// Prepare total mass label
		storedMassLabel = infoPanel.addTextField(Msg.getString("TabPanelGeneral.storedMass"), //$NON-NLS-1$
				StyleManager.DECIMAL_KG.format(person.getStoredMass()), 
				Msg.getString("TabPanelGeneral.storedMass.tooltip"));  //$NON-NLS-2$
	}
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		
		// Prepare total mass label
		storedMassLabel.setText(StyleManager.DECIMAL_KG.format(person.getStoredMass()));

		super.update();
	}
}
