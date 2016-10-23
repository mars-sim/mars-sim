/**
 * Mars Simulation Project
 * StudyDetailPanel.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.science;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A panel that displays study researcher information.
 */
class ResearcherPanel
extends JPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private ScienceWindow scienceWindow;
	private ScientificStudy study;
	private Person researcher;
	private JLabel titleLabel;
	private JButton nameButton;
	private JLabel scienceLabel;
	private JLabel activityLabel;
	private JProgressBar activityBar;
	private JPanel activityBarPane;

	/**
	 * Constructor.
	 */
	public ResearcherPanel(ScienceWindow scienceWindow) {

		// Use JPanel constructor
		super();

		this.scienceWindow = scienceWindow;

		// Set card layout.
		setLayout(new CardLayout());

		// Create blank panel.
		JPanel blankPane = new JPanel();
		add(blankPane, Msg.getString("ResearcherPanel.blank")); //$NON-NLS-1$

		// Create researcher panel.
		JPanel researcherPane = new JPanel(new BorderLayout());
		researcherPane.setBorder(new MarsPanelBorder());
		add(researcherPane, Msg.getString("ResearcherPanel.researcher")); //$NON-NLS-1$

		// Create top panel.
		JPanel topPane = new JPanel(new GridLayout(2, 1, 0, 0));
		researcherPane.add(topPane, BorderLayout.NORTH);

		// Create title panel.
		JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		topPane.add(titlePane);

		// Create title label.
		titleLabel = new JLabel(Msg.getString("ResearcherPanel.primaryResearcher")); //$NON-NLS-1$
		titlePane.add(titleLabel);

		// Create name button.
		nameButton = new JButton(""); //$NON-NLS-1$
		nameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Open window for researcher.
				if (researcher != null) openResearcherWindow(researcher);
			}
		});
		titlePane.add(nameButton);

		// Create science label.
		scienceLabel = new JLabel(Msg.getString("ResearcherPanel.scientificField","")); //$NON-NLS-1$
		topPane.add(scienceLabel);

		// Create bottom panel.
		JPanel bottomPane = new JPanel(new BorderLayout());
		researcherPane.add(bottomPane, BorderLayout.SOUTH);

		// Create activity label.
		activityLabel = new JLabel(Msg.getString("ResearcherPanel.activity.none") + " "); //$NON-NLS-1$
		bottomPane.add(activityLabel, BorderLayout.WEST);

		// Create activity bar panel.
		activityBarPane = new JPanel(new CardLayout());
		bottomPane.add(activityBarPane, BorderLayout.CENTER);

		// Create blank activity bar panel.
		JPanel blankActivityBarPane = new JPanel();
		activityBarPane.add(blankActivityBarPane, Msg.getString("ResearcherPanel.blankActivityBar")); //$NON-NLS-1$

		// Create activity progress bar.
		activityBar = new JProgressBar(0, 100);
		activityBar.setStringPainted(true);
		activityBarPane.add(activityBar, Msg.getString("ResearcherPanel.showActivityBar")); //$NON-NLS-1$
	}

	/**
	 * Gets the associated researcher for this panel.
	 * @return researcher or null if none.
	 */
	Person getStudyResearcher() {
		return researcher;
	}

	/**
	 * Sets the scientific study and researcher to display.
	 * If either parameter is null, display blank pane.
	 * @param study the scientific study.
	 * @param researcher the researcher.
	 */
	void setStudyResearcher(ScientificStudy study, Person researcher) {
		this.study = study;
		this.researcher = researcher;

		CardLayout cardLayout = (CardLayout) getLayout();
		if ((study == null) || (researcher == null)) {
			cardLayout.show(this, Msg.getString("ResearcherPanel.blank")); //$NON-NLS-1$
		}
		else {
			cardLayout.show(this, Msg.getString("ResearcherPanel.researcher")); //$NON-NLS-1$

			if (researcher.equals(study.getPrimaryResearcher())) {
				titleLabel.setText(Msg.getString("ResearcherPanel.primaryResearcher")); //$NON-NLS-1$
				scienceLabel.setText(Msg.getString("ResearcherPanel.scientificField", study.getScience().getName())); //$NON-NLS-1$
			}
			else {
				titleLabel.setText(Msg.getString("ResearcherPanel.collaborativeResearcher")); //$NON-NLS-1$
				ScienceType collabScience = study.getCollaborativeResearchers().get(researcher);
				scienceLabel.setText(Msg.getString("ResearcherPanel.scientificField", collabScience.getName())); //$NON-NLS-1$
			}

			nameButton.setText(researcher.getName());

			update();
		}
	}

	/**
	 * Update the information.
	 */
	void update() {
		activityLabel.setText(getStudyActivityText() + "    ");
		setActivityProgressBar();
	}

	/**
	 * Opens an info window for a researcher.
	 * @param researcher the researcher.
	 */
	private void openResearcherWindow(Person researcher) {
		scienceWindow.openResearcherWindow(researcher);
	}

	/**
	 * Gets the study activity text.
	 * @return the activity text.
	 */
	private String getStudyActivityText() {
		String result = Msg.getString("ResearcherPanel.activity.none") + " "; //$NON-NLS-1$

		if ((study != null) && (researcher != null)) {
			boolean isPrimaryResearcher = 
					(researcher.equals(study.getPrimaryResearcher()));
			if (!study.isCompleted()) {
				String phase = study.getPhase();
				if (ScientificStudy.PROPOSAL_PHASE.equals(phase)) {
					if (isPrimaryResearcher) result = Msg.getString("ResearcherPanel.activity.writingProposal"); //$NON-NLS-1$
				}
				else if (ScientificStudy.INVITATION_PHASE.equals(phase)) {
					if (isPrimaryResearcher) result = Msg.getString("ResearcherPanel.activity.inivitingCollaborators"); //$NON-NLS-1$
				}
				else if (ScientificStudy.RESEARCH_PHASE.equals(phase)) {
					result = Msg.getString("ResearcherPanel.activity.performingResearch"); //$NON-NLS-1$
				}
				else if (ScientificStudy.PAPER_PHASE.equals(phase)) {
					result = Msg.getString("ResearcherPanel.activity.compilingResults"); //$NON-NLS-1$
				}
				else if (ScientificStudy.PEER_REVIEW_PHASE.equals(phase)) {
					if (isPrimaryResearcher) result = Msg.getString("ResearcherPanel.activity.awaitingReview"); //$NON-NLS-1$
				}
			}
			else {
				double achievement = 0D;
				if (isPrimaryResearcher) achievement = study.getPrimaryResearcherEarnedScientificAchievement();
				else achievement = study.getCollaborativeResearcherEarnedScientificAchievement(researcher);
				DecimalFormat formatter = new DecimalFormat(Msg.getString("ResearcherPanel.decimalFormat")); //$NON-NLS-1$
				String achievementString = formatter.format(achievement);
				result = Msg.getString("ResearcherPanel.scientificAchievement", achievementString); //$NON-NLS-1$
			}
		}

		return result;
	}

	/**
	 * Set the activity progress bar.
	 */
	private void setActivityProgressBar() {
		boolean showProgress = false;
		double workCompleted = 0D;
		double workRequired = 0D;

		if ((study != null) && (researcher != null)) {
			if (!study.isCompleted()) {
				boolean isPrimaryResearcher = (researcher.equals(study.getPrimaryResearcher()));
				String phase = study.getPhase();
				if (ScientificStudy.PROPOSAL_PHASE.equals(phase)) {
					if (isPrimaryResearcher) {
						showProgress = true;
						workCompleted = study.getProposalWorkTimeCompleted();
						workRequired = study.getTotalProposalWorkTimeRequired();
					}
				}
				else if (ScientificStudy.RESEARCH_PHASE.equals(phase)) {
					showProgress = true;
					if (isPrimaryResearcher) {
						workCompleted = study.getPrimaryResearchWorkTimeCompleted();
						workRequired = study.getTotalPrimaryResearchWorkTimeRequired();
					}
					else {
						workCompleted = study.getCollaborativeResearchWorkTimeCompleted(researcher);
						workRequired = study.getTotalCollaborativeResearchWorkTimeRequired();
					}
				}
				else if (ScientificStudy.PAPER_PHASE.equals(phase)) {
					showProgress = true;
					if (isPrimaryResearcher) {
						workCompleted = study.getPrimaryPaperWorkTimeCompleted();
						workRequired = study.getTotalPrimaryPaperWorkTimeRequired();
					}
					else {
						workCompleted = study.getCollaborativePaperWorkTimeCompleted(researcher);
						workRequired = study.getTotalCollaborativePaperWorkTimeRequired();
					}
				}
				else if (ScientificStudy.PEER_REVIEW_PHASE.equals(phase)) {
					if (isPrimaryResearcher) {
						showProgress = true;
						workCompleted = study.getPeerReviewTimeCompleted();
						workRequired = study.getTotalPeerReviewTimeRequired();
					}
				}
			}
		}

		if (showProgress) {
			int progressValue = 0;
			if (workRequired > 0D) progressValue = (int) (workCompleted / workRequired * 100D);
			activityBar.setValue(progressValue);
			((CardLayout) activityBarPane.getLayout()).show(activityBarPane, Msg.getString("ResearcherPanel.showActivityBar")); //$NON-NLS-1$
		} else {
			((CardLayout) activityBarPane.getLayout()).show(activityBarPane, Msg.getString("ResearcherPanel.blankActivityBar")); //$NON-NLS-1$
		}
	}
}