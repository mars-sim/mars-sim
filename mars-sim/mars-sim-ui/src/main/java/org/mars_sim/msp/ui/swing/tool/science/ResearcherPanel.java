/**
 * Mars Simulation Project
 * StudyDetailPanel.java
 * @version 2.87 2009-11-03
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

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.Science;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A panel that displays study researcher information.
 */
class ResearcherPanel extends JPanel {

    // Static members
    private static final String BLANK_PANE = "blank";
    private static final String RESEARCHER_PANE = "researcher";
    private static final String PRIMARY_RESEARCHER = "Primary Researcher  ";
    private static final String COLLAB_RESEARCHER = "Collaborative Researcher  ";
    private static final String BLANK_ACTIVITY_BAR = "blank activity bar";
    private static final String SHOW_ACTIVITY_BAR = "show activity bar";
    
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
     * Constructor
     */
    ResearcherPanel(ScienceWindow scienceWindow) {
       
        // Use JPanel constructor
        super();
        
        this.scienceWindow = scienceWindow;
        
        // Set card layout.
        setLayout(new CardLayout());
        
        // Create blank panel.
        JPanel blankPane = new JPanel();
        add(blankPane, BLANK_PANE);
        
        // Create researcher panel.
        JPanel researcherPane = new JPanel(new BorderLayout());
        researcherPane.setBorder(new MarsPanelBorder());
        add(researcherPane, RESEARCHER_PANE);
        
        // Create top panel.
        JPanel topPane = new JPanel(new GridLayout(2, 1, 0, 0));
        researcherPane.add(topPane, BorderLayout.NORTH);
        
        // Create title panel.
        JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topPane.add(titlePane);
        
        // Create title label.
        titleLabel = new JLabel(PRIMARY_RESEARCHER);
        titlePane.add(titleLabel);
        
        // Create name button.
        nameButton = new JButton("");
        nameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // Open window for researcher.
                if (researcher != null) openResearcherWindow(researcher);
            }
        });
        titlePane.add(nameButton);
        
        // Create science label.
        scienceLabel = new JLabel("Scientific Field: ");
        topPane.add(scienceLabel);
        
        // Create bottom panel.
        JPanel bottomPane = new JPanel(new BorderLayout());
        researcherPane.add(bottomPane, BorderLayout.SOUTH);
        
        // Create activity label.
        activityLabel = new JLabel("Study Activity: None     ");
        bottomPane.add(activityLabel, BorderLayout.WEST);
        
        // Create activity bar panel.
        activityBarPane = new JPanel(new CardLayout());
        bottomPane.add(activityBarPane, BorderLayout.CENTER);
        
        // Create blank activity bar panel.
        JPanel blankActivityBarPane = new JPanel();
        activityBarPane.add(blankActivityBarPane, BLANK_ACTIVITY_BAR);
        
        // Create activity progress bar.
        activityBar = new JProgressBar(0, 100);
        activityBar.setStringPainted(true);
        activityBarPane.add(activityBar, SHOW_ACTIVITY_BAR);
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
            cardLayout.show(this, BLANK_PANE);
        }
        else {
            cardLayout.show(this, RESEARCHER_PANE);
            
            if (researcher.equals(study.getPrimaryResearcher())) {
                titleLabel.setText(PRIMARY_RESEARCHER);
                scienceLabel.setText("Scientific Field: " + study.getScience());
            }
            else {
                titleLabel.setText(COLLAB_RESEARCHER);
                Science collabScience = study.getCollaborativeResearchers().get(researcher);
                scienceLabel.setText("Scientific Field: " + collabScience.getName());
            }
            
            nameButton.setText(researcher.getName());
            
            update();
        }
    }
    
    /**
     * Update the information.
     */
    void update() {
        activityLabel.setText(getStudyActivityText());
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
        String result = "Study Activity: None";
        
        if ((study != null) && (researcher != null)) {
            boolean isPrimaryResearcher = 
                (researcher.equals(study.getPrimaryResearcher()));
            if (!study.isCompleted()) {
                String phase = study.getPhase();
                if (ScientificStudy.PROPOSAL_PHASE.equals(phase)) {
                    if (isPrimaryResearcher) result = "Study Activity: Writing study proposal  ";
                }
                else if (ScientificStudy.INVITATION_PHASE.equals(phase)) {
                    if (isPrimaryResearcher) result = "Study Activity: Inviting collaborators";
                }
                else if (ScientificStudy.RESEARCH_PHASE.equals(phase)) {
                    result = "Study Activity: Performing study research  ";
                }
                else if (ScientificStudy.PAPER_PHASE.equals(phase)) {
                    result = "Study Activity: Compiling study results  ";
                }
                else if (ScientificStudy.PEER_REVIEW_PHASE.equals(phase)) {
                    if (isPrimaryResearcher) result = "Study Activity: Awaiting peer review  ";
                }
            }
            else {
                double achievement = 0D;
                if (isPrimaryResearcher) achievement = study.getPrimaryResearcherEarnedScientificAchievement();
                else achievement = study.getCollaborativeResearcherEarnedScientificAchievement(researcher);
                DecimalFormat formatter = new DecimalFormat("0.0");
                String achievementString = formatter.format(achievement);
                result = "Scientific Achievement: " + achievementString;
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
                boolean isPrimaryResearcher = 
                    (researcher.equals(study.getPrimaryResearcher()));
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
            ((CardLayout) activityBarPane.getLayout()).show(activityBarPane, SHOW_ACTIVITY_BAR);
        }
        else {
            ((CardLayout) activityBarPane.getLayout()).show(activityBarPane, BLANK_ACTIVITY_BAR);
        }
    }
}