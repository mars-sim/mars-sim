/*
 * Mars Simulation Project
 * TabPanelDeath.java
 * @date 2022-07-09
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.health.DeathInfo;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;


/**
 * The TabPanelDeath is a tab panel with info about a person's death.
 */
@SuppressWarnings("serial")
public class TabPanelDeath extends EntityTabPanel<Person> {

	private static final String RIP_ICON = "rip";

	private JLabel doctorRetrievingBodyTF;	
	private JLabel examinerTF;
	private DeathInfo death;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display
	 * @param context the overall UI context.
	 */
	public TabPanelDeath(Person unit, UIContext context) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(RIP_ICON),
			Msg.getString("TabPanelDeath.title"), //$NON-NLS-1$
			context, unit
		);
	}

	@Override
	protected void buildUI(JPanel content) {
		var person = getEntity();
		PhysicalCondition condition = person.getPhysicalCondition();
		death = condition.getDeathDetails();

		// Prepare death label panel
		var deathLabelPanel = new AttributePanel();
		content.add(deathLabelPanel, BorderLayout.CENTER);

		deathLabelPanel.addTextField(Msg.getString("TabPanelDeath.cause"), death.getIllness().getName(), null);
		deathLabelPanel.addTextField(Msg.getString("TabPanelDeath.time"), death.getTimeOfDeath().getTruncatedDateTimeStamp(), null);
 		doctorRetrievingBodyTF = deathLabelPanel.addTextField(Msg.getString("TabPanelDeath.retrievingBody"), death.getDoctorRetrievingBody(), null);
		examinerTF = deathLabelPanel.addTextField(Msg.getString("TabPanelDeath.examiner"), death.getDoctorSigningCertificate(), null);
		deathLabelPanel.addTextField(Msg.getString("TabPanelDeath.malfunctionIfAny"), death.getMalfunction(), null);
		deathLabelPanel.addTextField(Msg.getString("TabPanelDeath.lastWord"), death.getLastWord(), null);
	
		var deathPlace = death.getDeathVicinity();
		if (deathPlace != null) {
			var deathEntity = new EntityLabel(deathPlace, getContext());
			deathLabelPanel.addLabelledItem(Msg.getString("TabPanelDeath.placeOfDeath"), deathEntity);
		}
		else {
			deathLabelPanel.addTextField(Msg.getString("TabPanelDeath.placeOfDeath"),
					death.getLocationOfDeath().getFormattedString(), null);
		}
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void refreshUI() {
		
		if (death.getDoctorRetrievingBody() != null) {
			String text = death.getDoctorRetrievingBody();
			doctorRetrievingBodyTF.setText(text);
		}	
		
		if (death.getExamDone()) {
			String text = death.getDoctorSigningCertificate() + " done @ " 
				+ death.getTimePostMortemExam().getTruncatedDateTimeStamp();
			examinerTF.setText(text);
		}	
	}
}

