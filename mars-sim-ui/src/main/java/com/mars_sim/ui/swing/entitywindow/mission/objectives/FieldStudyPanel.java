/**
 * Mars Simulation Project
 * FieldStudyPanel.java
 * @date 2025-06-22
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.mission.objectives;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.mission.objectives.FieldStudyObjectives;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * A panel for displaying study field mission information.
 */
public class FieldStudyPanel extends JPanel implements ObjectivesPanel, EntityListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private JProgressBar studyResearchBar;

	private ScientificStudy study;

	/**
	 * Constructor.
	 * 
	 * @param context the UI context.
	 */
	public FieldStudyPanel(FieldStudyObjectives objectives, UIContext context) {
		super();

		// Set layout.
		setLayout(new BorderLayout());
		setName(objectives.getName());

		// Create content panel.
		var attrPanel = new AttributePanel();
		add(attrPanel, BorderLayout.NORTH);

	
		study = objectives.getStudy();
		attrPanel.addLabelledItem(Msg.getString("FieldStudyPanel.study"), 
															new EntityLabel(study, context));
		attrPanel.addRow(Msg.getString("FieldStudyPanel.science"), objectives.getScience().getName());
		attrPanel.addRow(Msg.getString("FieldStudyPanel.siteTime"), Double.toString(objectives.getFieldSiteTime()));
		attrPanel.addLabelledItem(Msg.getString("FieldStudyPanel.leadResearcher"),
															new EntityLabel(study.getPrimaryResearcher(), context));

		// Create study research progress bar.
		studyResearchBar = new JProgressBar(0, 100);
		studyResearchBar.setStringPainted(true);
		attrPanel.addLabelledItem(Msg.getString("FieldStudyPanel.researchCompletion"), studyResearchBar);

		study.addEntityListener(this);
	}

	@Override
	public void entityUpdate(EntityEvent event) {
		studyResearchBar.setValue((int) (study.getPhaseProgress() * 100D));
	}

	/**
	 * Unregister the study listener
	 */
	@Override
	public void unregister() {
		study.removeEntityListener(this);
	}
}
