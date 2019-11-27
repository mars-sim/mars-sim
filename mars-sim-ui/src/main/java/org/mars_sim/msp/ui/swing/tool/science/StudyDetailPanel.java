/**
 * Mars Simulation Project
 * StudyDetailPanel.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.science;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

import com.alee.extended.label.WebStyledLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;

/**
 * A panel showing details of a selected scientific study.
 */
@SuppressWarnings("serial")
public class StudyDetailPanel
extends JPanel {

	// Data members
	private JLabel scienceFieldLabel;
	private JLabel levelLabel;
	private JLabel phaseLabel;
//	private JLabel topicLabel;
	
	private JLabel scienceHeader;
	private JLabel levelHeader;
	private JLabel phaseHeader;
	private JLabel topicHeader;
	
	private WebPanel topicPanel;
	
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

		JPanel infoPane = new JPanel(new BorderLayout());//FlowLayout(FlowLayout.LEFT,5,5));//GridLayout(2, 1, 0, 0));
		infoPane.setBorder(new MarsPanelBorder());
		infoPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(infoPane);

		JPanel topSpringPane = new JPanel(new SpringLayout());//new GridLayout(2, 2, 0, 0));
		infoPane.add(topSpringPane, BorderLayout.NORTH);
		
		scienceHeader = new JLabel(Msg.getString("StudyDetailPanel.science"), JLabel.RIGHT); //$NON-NLS-1$
		scienceFieldLabel = new JLabel("N/A", JLabel.LEFT);
		
		levelHeader = new JLabel(Msg.getString("StudyDetailPanel.level"), JLabel.RIGHT); //$NON-NLS-1$
		levelLabel = new JLabel("N/A", JLabel.LEFT);

		phaseHeader = new JLabel(Msg.getString("StudyDetailPanel.phase"), JLabel.RIGHT); //$NON-NLS-1$
		phaseLabel = new JLabel("N/A", JLabel.LEFT); 

		topicHeader = new JLabel("  " + Msg.getString("StudyDetailPanel.topic") + "    "); //$NON-NLS-1$
		
		topSpringPane.add(scienceHeader);
		topSpringPane.add(scienceFieldLabel);

		topSpringPane.add(levelHeader);
		topSpringPane.add(levelLabel);

		topSpringPane.add(phaseHeader);
		topSpringPane.add(phaseLabel);
		
		WebStyledLabel noneLabel = new WebStyledLabel("{None:i;c(blue);background(grey)}"); // StyleId.styledlabelTag, 
		noneLabel.setStyleId(StyleId.styledlabelShadow); // styledlabelTag

		topicPanel = new WebPanel(new BorderLayout());
		topicPanel.add(topicHeader, BorderLayout.WEST);
		topicPanel.add(noneLabel);
		
		infoPane.add(topicPanel, BorderLayout.CENTER);
		
		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(topSpringPane,
		                                3, 2, //rows, cols
		                                5, 4,        //initX, initY
		                                30, 3);       //xPad, yPad
		
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
			phaseLabel.setText(getPhaseString(study));

			// Update any changes to the displayed collaborative researcher panels.
			Iterator<Integer> i = study.getCollaborativeResearchers().keySet().iterator();
			int count = 0;
			while (i.hasNext()) {
				Person researcher = unitManager.getPersonByID(i.next());
				if (count < collabResearcherPanes.length && !researcher.equals(collabResearcherPanes[count].getStudyResearcher())) {
					collabResearcherPanes[count].setStudyResearcher(study, researcher);
					count++;
				}
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
			scienceFieldLabel.setText(study.getScience().getName());
			levelLabel.setText(Integer.toString(study.getDifficultyLevel()));
			phaseLabel.setText(getPhaseString(study));
			
			// Clear off the old topic labels
			clearLabels();
			// Add back the topic label header
			topicPanel.add(topicHeader, BorderLayout.WEST);			
			
			List<String> topics = study.getTopic(study.getScience());
//			List<WebStyledLabel> topicLabels = new ArrayList<>();
			if (topics != null && !topics.isEmpty()) {
				for (String t: topics) {
	//				label.addStyleRange ( new StyleRange (Font.BOLD ) );
					WebStyledLabel label = new WebStyledLabel();//"{None:i;c(blue);background(grey)}");//"{None:b;c(blue)}");
					label.setStyleId(StyleId.styledlabelShadow);
					label.setText(t);
//					label.resetStyleId();
//					label.setBackground(Color.DARK_GRAY);
//					label.setForeground(Color.WHITE);				
					topicPanel.add(label);
				}			
			}
			primaryResearcherPane.setStudyResearcher(study, study.getPrimaryResearcher());
			Iterator<Integer> i = study.getCollaborativeResearchers().keySet().iterator();
			int count = 0;
			while (i.hasNext()) {
				if (count < collabResearcherPanes.length) {
					collabResearcherPanes[count].setStudyResearcher(study, unitManager.getPersonByID(i.next()));
					count++;
				}
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