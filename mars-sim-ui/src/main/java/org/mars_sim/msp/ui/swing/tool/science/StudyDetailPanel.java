/**
 * Mars Simulation Project
 * StudyDetailPanel.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.science;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A panel showing details of a selected scientific study.
 */
public class StudyDetailPanel
extends JPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private JLabel scienceFieldLabel;
	private JLabel levelLabel;
	private JLabel phaseLabel;
	private JLabel topicLabel;
	
	private ResearcherPanel primaryResearcherPane;
	private ResearcherPanel[] collabResearcherPanes;
	private ScientificStudy study;

	private static UnitManager unitManager = Simulation.instance().getUnitManager();
	
	/**
	 * Constructor
	 */
	StudyDetailPanel(ScienceWindow scienceWindow) {
		// Use JPanel constructor.
		super();

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(425, -1));

		JLabel titleLabel = new JLabel(Msg.getString("StudyDetailPanel.details"), JLabel.CENTER); //$NON-NLS-1$
		add(titleLabel, BorderLayout.NORTH);

		Box mainPane = Box.createVerticalBox();
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);

		JPanel infoPane = new JPanel(new GridLayout(2, 1, 0, 0));
		infoPane.setBorder(new MarsPanelBorder());
		infoPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(infoPane);

		JPanel labelPane = new JPanel(new GridLayout(2, 2, 0, 0));
		
		scienceFieldLabel = new JLabel(Msg.getString("StudyDetailPanel.science",""), JLabel.LEFT); //$NON-NLS-1$
		labelPane.add(scienceFieldLabel);

		levelLabel = new JLabel(Msg.getString("StudyDetailPanel.level",""), JLabel.LEFT); //$NON-NLS-1$
		labelPane.add(levelLabel);

		phaseLabel = new JLabel(Msg.getString("StudyDetailPanel.phase",""), JLabel.LEFT); //$NON-NLS-1$
		labelPane.add(phaseLabel);

		labelPane.add(new JLabel(""));
		
		infoPane.add(labelPane);
		
		topicLabel = new JLabel(Msg.getString("StudyDetailPanel.topic", "[Work in Progress]"), JLabel.LEFT); //$NON-NLS-1$
		infoPane.add(topicLabel);
		
		primaryResearcherPane = new ResearcherPanel(scienceWindow);
		primaryResearcherPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(primaryResearcherPane);

		collabResearcherPanes = new ResearcherPanel[3];
		for (int x = 0; x < 3; x++) {
			collabResearcherPanes[x] = new ResearcherPanel(scienceWindow);
			collabResearcherPanes[x].setAlignmentX(Component.LEFT_ALIGNMENT);
			mainPane.add(collabResearcherPanes[x]);
		}

		// Add a vertical glue.
		mainPane.add(Box.createVerticalGlue());
	}

	/**
	 * Updates the panel.
	 */
	void update() {
		if (study != null) {
			// Update the status label.
			phaseLabel.setText(Msg.getString("StudyDetailPanel.phase", getPhaseString(study))); //$NON-NLS-1$

			// Update any changes to the displayed collaborative researcher panels.
			Iterator<Integer> i = study.getCollaborativeResearchers().keySet().iterator();
			int count = 0;
			while (i.hasNext()) {
				Person researcher = unitManager.getPersonByID(i.next());
				if (!researcher.equals(collabResearcherPanes[count].getStudyResearcher()))
					collabResearcherPanes[count].setStudyResearcher(study, researcher);
				count++;
			}
			for (int x = count; x < collabResearcherPanes.length; x++) {
				if (collabResearcherPanes[x].getStudyResearcher() != null)
					collabResearcherPanes[x].setStudyResearcher(null, null);
			}

			// Update all researcher panels.
			primaryResearcherPane.update();
			for (ResearcherPanel collabResearcherPane : collabResearcherPanes) collabResearcherPane.update();
		}
	}

	/**
	 * Display information about a scientific study.
	 * @param study the scientific study.
	 */
	void displayScientificStudy(ScientificStudy study) {
		this.study = study;

		if (study != null) {
			scienceFieldLabel.setText(Msg.getString("StudyDetailPanel.science", study.getScience().getName()) + " "); //$NON-NLS-1$
			levelLabel.setText(Msg.getString("StudyDetailPanel.level", Integer.toString(study.getDifficultyLevel())) + " "); //$NON-NLS-1$
			phaseLabel.setText(Msg.getString("StudyDetailPanel.phase", getPhaseString(study)) + " "); //$NON-NLS-1$
			topicLabel.setText(Msg.getString("StudyDetailPanel.topic", study.getTopic(study.getScience()))); //$NON-NLS-1$
			
			primaryResearcherPane.setStudyResearcher(study, study.getPrimaryResearcher());
			Iterator<Integer> i = study.getCollaborativeResearchers().keySet().iterator();
			int count = 0;
			while (i.hasNext()) {
				collabResearcherPanes[count].setStudyResearcher(study, unitManager.getPersonByID(i.next()));
				count++;
			}
			for (int x = count; x < collabResearcherPanes.length; x++)
				collabResearcherPanes[x].setStudyResearcher(null, null);
		}
		else {
			clearLabels();
			clearResearcherPanels();
		}
	}

	/**
	 * Clear all labels.
	 */
	private void clearLabels() {
		scienceFieldLabel.setText(Msg.getString("StudyDetailPanel.science","")); //$NON-NLS-1$
		levelLabel.setText(Msg.getString("StudyDetailPanel.level","")); //$NON-NLS-1$
		phaseLabel.setText(Msg.getString("StudyDetailPanel.phase","")); //$NON-NLS-1$
		topicLabel.setText(Msg.getString("StudyDetailPanel.topic","")); //$NON-NLS-1$
	}

	/**
	 * Clear all researcher panels.
	 */
	private void clearResearcherPanels() {
		primaryResearcherPane.setStudyResearcher(null, null);
		for (ResearcherPanel collabResearcherPane : collabResearcherPanes)
			collabResearcherPane.setStudyResearcher(null, null);
	}

	/**
	 * Get the phase string for a scientific study.
	 * @param study the scientific study.
	 * @return the phase string.
	 */
	private String getPhaseString(ScientificStudy study) {
		String result = ""; //$NON-NLS-1$

		if (study != null) {
			if (!study.isCompleted()) result = study.getPhase();
			else result = study.getCompletionState();
		}

		return result;
	}
}