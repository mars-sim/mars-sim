/**
 * Mars Simulation Project
 * MeteorologyStudyFieldMissionCustomInfoPanel.java
 * @version 3.1.0 2019-02-20
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MeteorologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyEvent;
import org.mars_sim.msp.core.science.ScientificStudyListener;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.progressbar.WebProgressBar;

/**
 * A panel for displaying meteorology study field mission information.
 */
public class MeteorologyStudyFieldMissionCustomInfoPanel extends MissionCustomInfoPanel
		implements ScientificStudyListener {

	// Data members.
	private MainDesktopPane desktop;
	private ScientificStudy study;
	private MeteorologyFieldStudy meteorologyMission;
	private WebLabel studyNameLabel;
	private WebLabel researcherNameLabel;
	private WebProgressBar studyResearchBar;

	/**
	 * Constructor.
	 * 
	 * @param desktop the main desktop pane.
	 */
	public MeteorologyStudyFieldMissionCustomInfoPanel(MainDesktopPane desktop) {
		// Use MissionCustomInfoPanel constructor.
		super();

		// Initialize data members.
		this.desktop = desktop;

		// Set layout.
		setLayout(new BorderLayout());

		// Create content panel.
		WebPanel contentPanel = new WebPanel(new GridLayout(3, 1));
		add(contentPanel, BorderLayout.NORTH);

		// Create study panel.
		WebPanel studyPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		contentPanel.add(studyPanel);

		// Create science tool button.
		WebButton scienceToolButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.science"))); //$NON-NLS-1$
		scienceToolButton.setMargin(new Insets(1, 1, 1, 1));
		scienceToolButton
				.setToolTipText(Msg.getString("MeteorologyStudyFieldMissionCustomInfoPanel.tooltip.openInScienceTool")); //$NON-NLS-1$
		scienceToolButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				displayStudyInScienceTool();
			}
		});
		studyPanel.add(scienceToolButton);

		// Create study title label.
		WebLabel studyTitleLabel = new WebLabel(
				Msg.getString("MeteorologyStudyFieldMissionCustomInfoPanel.meteorologyFieldStudy")); //$NON-NLS-1$
		studyPanel.add(studyTitleLabel);

		// Create study name label.
		studyNameLabel = new WebLabel(""); //$NON-NLS-1$
		studyPanel.add(studyNameLabel);

		// Create researcher panel.
		WebPanel researcherPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		contentPanel.add(researcherPanel);

		// Create researcher title label.
		WebLabel researcherTitleLabel = new WebLabel(
				Msg.getString("MeteorologyStudyFieldMissionCustomInfoPanel.leadResearcher")); //$NON-NLS-1$
		researcherPanel.add(researcherTitleLabel);

		// Create researcher name label.
		researcherNameLabel = new WebLabel(""); //$NON-NLS-1$
		researcherPanel.add(researcherNameLabel);

		// Create study research panel.
		WebPanel studyResearchPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		contentPanel.add(studyResearchPanel);

		// Create study research title label.
		WebLabel studyResearchTitleLabel = new WebLabel(
				Msg.getString("MeteorologyStudyFieldMissionCustomInfoPanel.researchCompletion")); //$NON-NLS-1$
		studyResearchPanel.add(studyResearchTitleLabel);

		// Create study research progress bar.
		studyResearchBar = new WebProgressBar(0, 100);
		studyResearchBar.setStringPainted(true);
		studyResearchPanel.add(studyResearchBar);
	}

	@Override
	public void updateMission(Mission mission) {
		if (mission instanceof MeteorologyFieldStudy) {
			meteorologyMission = (MeteorologyFieldStudy) mission;

			// Remove as scientific study listener.
			if (study != null) {
				study.removeScientificStudyListener(this);
			}

			// Add as scientific study listener to new study.
			study = meteorologyMission.getScientificStudy();
			if (study != null) {
				study.addScientificStudyListener(this);

				// Update study name.
				studyNameLabel.setText(study.toString());

				// Update lead researcher for mission.
				researcherNameLabel.setText(meteorologyMission.getLeadResearcher().getName());

				// Update study research bar.
				updateStudyResearchBar(study, meteorologyMission.getLeadResearcher());
			}
		}
	}

	@Override
	public void updateMissionEvent(MissionEvent e) {
		// Do nothing
	}

	@Override
	public void scientificStudyUpdate(ScientificStudyEvent event) {
		ScientificStudy study = event.getStudy();
		Person leadResearcher = meteorologyMission.getLeadResearcher();

		if (ScientificStudyEvent.PRIMARY_RESEARCH_WORK_EVENT.equals(event.getType())
				|| ScientificStudyEvent.COLLABORATION_RESEARCH_WORK_EVENT.equals(event.getType())) {
			if (leadResearcher.equals(event.getResearcher())) {
				updateStudyResearchBar(study, leadResearcher);
			}
		}
	}

	/**
	 * Checks if a researcher is the primary researcher on a scientific study.
	 * 
	 * @param researcher the researcher.
	 * @param study      the scientific study.
	 * @return true if primary researcher.
	 */
	private boolean isStudyPrimaryResearcher(Person researcher, ScientificStudy study) {
		boolean result = false;

		if (researcher.equals(study.getPrimaryResearcher()))
			result = true;

		return result;
	}

	/**
	 * Checks if a researcher is a collaborative researcher on a scientific study.
	 * 
	 * @param researcher the researcher.
	 * @param study      the scientific study.
	 * @return true if collaborative researcher.
	 */
	private boolean isStudyCollaborativeResearcher(Person researcher, ScientificStudy study) {
		boolean result = false;

		if (study.getCollaborativeResearchers().containsKey(researcher.getIdentifier()))
			result = true;

		return result;
	}

	/**
	 * Updates the research completion progress bar.
	 * 
	 * @param study          the
	 * @param leadResearcher
	 */
	private void updateStudyResearchBar(ScientificStudy study, Person leadResearcher) {
		if (study != null) {
			double requiredResearchWork = 0D;
			double completedResearchWork = 0D;

			if (isStudyPrimaryResearcher(leadResearcher, study)) {
				requiredResearchWork = study.getTotalPrimaryResearchWorkTimeRequired();
				completedResearchWork = study.getPrimaryResearchWorkTimeCompleted();
			} else if (isStudyCollaborativeResearcher(leadResearcher, study)) {
				requiredResearchWork = study.getTotalCollaborativeResearchWorkTimeRequired();
				completedResearchWork = study.getCollaborativeResearchWorkTimeCompleted(leadResearcher);
			} else {
				return;
			}

			int percentResearchCompleted = (int) (completedResearchWork / requiredResearchWork * 100D);
			studyResearchBar.setValue(percentResearchCompleted);
		}
	}

	/**
	 * Display the scientific study in the science tool window.
	 */
	private void displayStudyInScienceTool() {
		if (study != null) {
			ScienceWindow scienceToolWindow = (ScienceWindow) desktop.getToolWindow(ScienceWindow.NAME);
			scienceToolWindow.setScientificStudy(study);
			desktop.openToolWindow(ScienceWindow.NAME);
		}
	}
}