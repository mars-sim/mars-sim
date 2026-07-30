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
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.EntityLabel;


/**
 * The TabPanelDeath is a tab panel with info about a person's death.
 */
class TabPanelDeath extends EntityTabPanel<Person> {

	private static final String RIP_ICON = "rip";

	private JLabel doctorRetrievingBodyLabel;	
	private JLabel examinerLabel;
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

		JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
		content.add(mainPanel, BorderLayout.NORTH);
		
		// Prepare death label panel
		var deathLabelPanel = new AttributePanel();
		mainPanel.add(deathLabelPanel, BorderLayout.NORTH);

		deathLabelPanel.addRow(Msg.getString("TabPanelDeath.task"), death.getTask());
		deathLabelPanel.addRow(Msg.getString("TabPanelDeath.mission"), death.getMission());
		deathLabelPanel.addRow(Msg.getString("TabPanelDeath.mission.phase"), death.getMissionPhase());
		deathLabelPanel.addRow(Msg.getString("TabPanelDeath.cause"), death.getIllness().getName());
		deathLabelPanel.addRow(Msg.getString("TabPanelDeath.time"), death.getTimeOfDeath().getTruncatedDateTimeStamp());
 		doctorRetrievingBodyLabel = deathLabelPanel.addRow(Msg.getString("TabPanelDeath.retrievingBody"), death.getDoctorRetrievingBody());
		examinerLabel = deathLabelPanel.addRow(Msg.getString("TabPanelDeath.examiner"), death.getDoctorSigningCertificate());
		deathLabelPanel.addRow(Msg.getString("TabPanelDeath.malfunctionIfAny"), death.getMalfunction());
		deathLabelPanel.addRow(Msg.getString("TabPanelDeath.lastWord"), death.getLastWord());
		deathLabelPanel.addRow(Msg.getString("TabPanelDeath.placeOfDeath"), death.getPlaceOfDeath());
	
		var deathPlace = death.getContainerUnit();
		if (deathPlace != null) {
			var deathEntity = new EntityLabel(deathPlace, getContext());
			deathLabelPanel.addLabelledItem(Msg.getString("TabPanelDeath.containerUnit"), deathEntity);
		}
		
		if (death.getCoordinates() != null) {
			deathLabelPanel.addRow(Msg.getString("TabPanelDeath.coordinates"),
					death.getCoordinates().getFormattedString());
		}
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void refreshUI() {
		
		if (death.getDoctorRetrievingBody() != null) {
			String text = death.getDoctorRetrievingBody();
			doctorRetrievingBodyLabel.setText(text);
		}	
		
		if (death.getExamDone()) {
			String text = death.getDoctorSigningCertificate() + " done @ " 
				+ death.getTimePostMortemExam().getTruncatedDateTimeStamp();
			examinerLabel.setText(text);
		}	
	}
}

