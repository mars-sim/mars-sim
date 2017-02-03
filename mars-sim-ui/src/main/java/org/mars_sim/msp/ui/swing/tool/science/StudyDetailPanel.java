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
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

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
	private JLabel statusLabel;
	private ResearcherPanel primaryResearcherPane;
	private ResearcherPanel[] collabResearcherPanes;
	private ScientificStudy study;

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

		JPanel infoPane = new JPanel(new GridLayout(3, 1, 0, 0));
		infoPane.setBorder(new MarsPanelBorder());
		infoPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(infoPane);

		scienceFieldLabel = new JLabel(Msg.getString("StudyDetailPanel.science",""), JLabel.LEFT); //$NON-NLS-1$
		infoPane.add(scienceFieldLabel);

		levelLabel = new JLabel(Msg.getString("StudyDetailPanel.level",""), JLabel.LEFT); //$NON-NLS-1$
		infoPane.add(levelLabel);

		statusLabel = new JLabel(Msg.getString("StudyDetailPanel.status",""), JLabel.LEFT); //$NON-NLS-1$
		infoPane.add(statusLabel);

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
			statusLabel.setText(Msg.getString("StudyDetailPanel.status", getStatusString(study))); //$NON-NLS-1$

			// Update any changes to the displayed collaborative researcher panels.
			Iterator<Person> i = study.getCollaborativeResearchers().keySet().iterator();
			int count = 0;
			while (i.hasNext()) {
				Person researcher = i.next();
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
			statusLabel.setText(Msg.getString("StudyDetailPanel.status", getStatusString(study)) + " "); //$NON-NLS-1$

			primaryResearcherPane.setStudyResearcher(study, study.getPrimaryResearcher());
			Iterator<Person> i = study.getCollaborativeResearchers().keySet().iterator();
			int count = 0;
			while (i.hasNext()) {
				collabResearcherPanes[count].setStudyResearcher(study, i.next());
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
		statusLabel.setText(Msg.getString("StudyDetailPanel.status","")); //$NON-NLS-1$
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
	 * Get the status string for a scientific study.
	 * @param study the scientific study.
	 * @return the status string.
	 */
	private String getStatusString(ScientificStudy study) {
		String result = ""; //$NON-NLS-1$

		if (study != null) {
			if (!study.isCompleted()) result = study.getPhase();
			else result = study.getCompletionState();
		}

		return result;
	}
}