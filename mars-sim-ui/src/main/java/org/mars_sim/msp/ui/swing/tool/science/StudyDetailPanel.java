/**
 * Mars Simulation Project
 * StudyDetailPanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.science;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

/**
 * A panel showing details of a selected scientific study.
 */
@SuppressWarnings("serial")
public class StudyDetailPanel
extends JPanel {
	
	// Font used in tab panel title
	protected static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 16);

	// Data members
	private JLabel scienceFieldLabel;
	private JLabel levelLabel;
	private JLabel phaseLabel;
	private JLabel nameLabel;
//	private JLabel topicLabel;
	
	private JLabel scienceHeader;
	private JLabel levelHeader;
	private JLabel phaseHeader;
	private JLabel nameHeader;
	private JLabel topicHeader;
	
	private JPanel topicPanel;
	
	private ResearcherPanel primaryResearcherPane;
	private List<ResearcherPanel> collabResearcherPanes = new ArrayList<>();

	private JScrollPane scrollPane;

	private ScientificStudy study;
	private Box mainPane;
	private ScienceWindow scienceWindow;
	
	/**
	 * Constructor
	 */
	StudyDetailPanel(ScienceWindow scienceWindow) {
		// Use JPanel constructor.
		super();
		this.scienceWindow = scienceWindow;

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(425, -1));

		JLabel titleLabel = new JLabel(Msg.getString("StudyDetailPanel.details"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(TITLE_FONT);
		add(titleLabel, BorderLayout.NORTH);

		mainPane = Box.createVerticalBox();
		mainPane.setBorder(new MarsPanelBorder());

		// Create scroll pane.
		scrollPane = new JScrollPane();
		scrollPane.setBorder(new MarsPanelBorder());
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setViewportView(mainPane);

		JPanel infoPane = new JPanel(new BorderLayout());//FlowLayout(FlowLayout.LEFT,5,5));//GridLayout(2, 1, 0, 0));
		infoPane.setBorder(new MarsPanelBorder());
		infoPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(infoPane);

		JPanel topSpringPane = new JPanel(new SpringLayout());//new GridLayout(2, 2, 0, 0));
		infoPane.add(topSpringPane, BorderLayout.NORTH);

		nameHeader = new JLabel("Name :", JLabel.RIGHT); //$NON-NLS-1$
		nameLabel = new JLabel("N/A", JLabel.LEFT);

		scienceHeader = new JLabel(Msg.getString("StudyDetailPanel.science"), JLabel.RIGHT); //$NON-NLS-1$
		scienceFieldLabel = new JLabel("N/A", JLabel.LEFT);
		
		levelHeader = new JLabel(Msg.getString("StudyDetailPanel.level"), JLabel.RIGHT); //$NON-NLS-1$
		levelLabel = new JLabel("N/A", JLabel.LEFT);

		phaseHeader = new JLabel(Msg.getString("StudyDetailPanel.phase"), JLabel.RIGHT); //$NON-NLS-1$
		phaseLabel = new JLabel("N/A", JLabel.LEFT); 

		topicHeader = new JLabel("  " + Msg.getString("StudyDetailPanel.topic") + "    "); //$NON-NLS-1$

		topSpringPane.add(nameHeader);
		topSpringPane.add(nameLabel);
		
		topSpringPane.add(scienceHeader);
		topSpringPane.add(scienceFieldLabel);

		topSpringPane.add(levelHeader);
		topSpringPane.add(levelLabel);

		topSpringPane.add(phaseHeader);
		topSpringPane.add(phaseLabel);
		
		JLabel noneLabel = new JLabel("{None:i;c(blue);background(grey)}"); // StyleId.styledlabelTag, 

		topicPanel = new JPanel(new BorderLayout());
		topicPanel.add(topicHeader, BorderLayout.WEST);
		topicPanel.add(noneLabel);
		
		infoPane.add(topicPanel, BorderLayout.CENTER);
		
		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(topSpringPane,
		                                4, 2, //rows, cols
		                                10, 4,        //initX, initY
		                                30, 3);       //xPad, yPad
		
		primaryResearcherPane = new ResearcherPanel(scienceWindow);
		primaryResearcherPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(primaryResearcherPane);

		// Add a vertical glue.
		mainPane.add(Box.createVerticalGlue());
	}

	/**
	 * Updates the panel.
	 */
	void update() {
		if (study != null) {
			// Update the status label.
			phaseLabel.setText(getPhaseString(study));

			loadCollaborators(false);
			
			// Update all researcher panels.
			primaryResearcherPane.update();
			for (ResearcherPanel collabResearcherPane : collabResearcherPanes) collabResearcherPane.update();
		}
	}

	/**
	 * Display information about a scientific study.
	 * @param study the scientific study.
	 */
	boolean displayScientificStudy(ScientificStudy study) {
		boolean newSelection = false;
		if ((this.study == null) || !this.study.equals(study)) {
			this.study = study;
			newSelection = true;

			nameLabel.setText(study.getName());
			scienceFieldLabel.setText(study.getScience().getName());
			levelLabel.setText(Integer.toString(study.getDifficultyLevel()));
			phaseLabel.setText(getPhaseString(study));
			
			// Clear off the old topic labels
			clearLabels();
			// Add back the topic label header
			topicPanel.add(topicHeader, BorderLayout.WEST);			
			
			List<String> topics = study.getTopic();
			if (topics != null && !topics.isEmpty()) {
				for (String t: topics) {
					JLabel label = new JLabel(t);//"{None:i;c(blue);background(grey)}");//"{None:b;c(blue)}");
					topicPanel.add(label);
				}			
			}
			primaryResearcherPane.setStudyResearcher(study, study.getPrimaryResearcher());
			loadCollaborators(true);
		}
		else {
			clearLabels();
			clearResearcherPanels();
		}
		
		return newSelection;
	}

	/**
	 * Load the collaborators of the current study
	 */
	private void loadCollaborators(boolean forceReload) {
		// Update any changes to the displayed collaborative researcher panels.
		Set<Person> researchers = study.getCollaborativeResearchers();
		
		// Add panel as collaborator come along
		if (forceReload || researchers.size() > collabResearcherPanes.size()) {
			List<Person> active = researchers.stream()
					.sorted(Comparator.comparing(Person::getName))
					.collect(Collectors.toList());
			
			// Add researchers
			int updated = 0;
			for (Person researcher : active) {
				if (updated >= collabResearcherPanes.size()) {
					ResearcherPanel newPanel = new ResearcherPanel(scienceWindow);
					newPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
					mainPane.add(newPanel);	
					mainPane.validate();
					collabResearcherPanes.add(newPanel);
				}
				collabResearcherPanes.get(updated).setStudyResearcher(study, researcher);
				updated++;
			}
			
			// Blank any remaining panels
			while(updated < collabResearcherPanes.size()) {
				collabResearcherPanes.get(updated).setStudyResearcher(null, null);
				updated++;
			}
		}
	}

	/**
	 * Clear all labels.
	 */
	private void clearLabels() {
//		scienceHeader.setText(Msg.getString("StudyDetailPanel.science")); //$NON-NLS-1$
//		levelHeader.setText(Msg.getString("StudyDetailPanel.level")); //$NON-NLS-1$
//		phaseHeader.setText(Msg.getString("StudyDetailPanel.phase")); //$NON-NLS-1$
//		topicHeader.setText(Msg.getString("StudyDetailPanel.topic")); //$NON-NLS-1$
		topicPanel.removeAll();
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
