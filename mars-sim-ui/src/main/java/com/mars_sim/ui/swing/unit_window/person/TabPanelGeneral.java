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

import javax.swing.JPanel;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The TabPanelGeneral is a tab panel for general information about a person.
 */
@SuppressWarnings("serial")
class TabPanelGeneral extends EntityTabPanel<Person> {
	
	private static final String TAB_BIRTH_DATE_AGE = "TabPanelGeneral.birthDateAndAge";
			
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelGeneral(Person unit, UIContext context) {
		super(
			GENERAL_TITLE,
			ImageLoader.getIconByName(GENERAL_ICON),		
			GENERAL_TOOLTIP,
			context, unit
		);
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare attribute panel.
		AttributePanel infoPanel = new AttributePanel();
		
		content.add(infoPanel, BorderLayout.NORTH);

		var person = getEntity();

		infoPanel.addTextField(Msg.getString("Person.gender"), person.getGender().getName(), null);
		infoPanel.addTextField(Msg.getString("Person.bloodType"), person.getBloodType(), null);
				
		// Prepare birthdate and age textfield
		var birthDate = person.getBirthDate();
		String birthTxt = Msg.getString(
			TAB_BIRTH_DATE_AGE,
			birthDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
			Integer.toString(person.getAge())); //$NON-NLS-1$
		infoPanel.addTextField(Msg.getString("Person.birthDate"), birthTxt, null);
		
		// Prepare country of origin textfield
		String country = person.getCountry();
		infoPanel.addTextField(Msg.getString("Country.singular"), //$NON-NLS-1$
				country, null);
		infoPanel.addLabelledItem(Msg.getString("Authority.singular"),
						new EntityLabel(person.getReportingAuthority(), getContext()));

		// Prepare weight textfield
		infoPanel.addTextField(Msg.getString("Person.weight"), //$NON-NLS-1$
				  							  StyleManager.DECIMAL_KG.format(person.getBaseMass()), null);
		
		// Prepare height name label
		infoPanel.addTextField(Msg.getString("Person.height"), //$NON-NLS-1$
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
	}
}
