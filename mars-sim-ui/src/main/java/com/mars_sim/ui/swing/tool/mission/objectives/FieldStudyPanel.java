/**
 * Mars Simulation Project
 * FieldStudyPanel.java
 * @date 2025-06-22
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission.objectives;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.mars_sim.core.mission.objectives.FieldStudyObjectives;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyEvent;
import com.mars_sim.core.science.ScientificStudyListener;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.tool.mission.ObjectivesPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * A panel for displaying study field mission information.
 */
public class FieldStudyPanel extends JPanel implements ObjectivesPanel, ScientificStudyListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private JProgressBar studyResearchBar;

	private ScientificStudy study;

	/**
	 * Constructor.
	 * 
	 * @param desktop the main desktop pane.
	 */
	public FieldStudyPanel(FieldStudyObjectives objectives) {
		super();

		// Set layout.
		setLayout(new BorderLayout());
		setName(objectives.getName());

		// Create content panel.
		var attrPanel = new AttributePanel();
		add(attrPanel, BorderLayout.NORTH);

		study = objectives.getStudy();
		attrPanel.addRow(Msg.getString("FieldStudyPanel.study"), study.getName());
		attrPanel.addRow(Msg.getString("FieldStudyPanel.science"), objectives.getScience().getName());
		attrPanel.addRow(Msg.getString("FieldStudyPanel.siteTime"), Double.toString(objectives.getFieldSiteTime()));
		attrPanel.addRow(Msg.getString("FieldStudyPanel.leadResearcher"), study.getPrimaryResearcher().getName());

		// Create study research progress bar.
		studyResearchBar = new JProgressBar(0, 100);
		studyResearchBar.setStringPainted(true);
		attrPanel.addLabelledItem(Msg.getString("FieldStudyPanel.researchCompletion"), studyResearchBar);

		study.addScientificStudyListener(this);
	}

	@Override
	public void scientificStudyUpdate(ScientificStudyEvent event) {
		studyResearchBar.setValue((int) (study.getPhaseProgress() * 100D));
	}

	/**
	 * Unregister the study listener
	 */
	@Override
	public void unregister() {
		study.removeScientificStudyListener(this);
	}
}
