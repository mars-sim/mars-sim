package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A panel for displaying exploration custom mission information.
 */
public class ExplorationCustomInfoPanel extends MissionCustomInfoPanel {

    // Data members
    private Exploration mission;
    private Map<String, ExplorationSitePanel> sitePanes;
    private Box mainPane;
    
    /**
     * Constructor
     */
    ExplorationCustomInfoPanel() {
        // Use JPanel constructor
        super();
        
        setLayout(new BorderLayout());
        
        // Create the main scroll panel.
        JScrollPane mainScrollPane = new JScrollPane();
        mainScrollPane.setPreferredSize(new Dimension(-1, -1));
        add(mainScrollPane, BorderLayout.CENTER);
        
        // Create main panel.
        mainPane = Box.createVerticalBox();
        mainScrollPane.setViewportView(mainPane);
        
        sitePanes = new HashMap<String, ExplorationSitePanel>(5);
    }
    
    @Override
    public void updateMission(Mission mission) {
        if (mission instanceof Exploration) {
            if (!mission.equals(this.mission)) {
                this.mission = (Exploration) mission;
                
                // Clear site panels.
                sitePanes.clear();
                mainPane.removeAll();
                
                // Create new site panels.
                Map<String, Double> explorationSites = this.mission.getExplorationSiteCompletion();
                TreeSet<String> treeSet = new TreeSet<String>(explorationSites.keySet());
                Iterator<String> i = treeSet.iterator();
                while (i.hasNext()) {
                    String siteName = i.next();
                    double completion = explorationSites.get(siteName);
                    ExplorationSitePanel panel = new ExplorationSitePanel(siteName, completion);
                    sitePanes.put(siteName, panel);
                    mainPane.add(panel);
                }
                
                mainPane.add(Box.createVerticalGlue());
                repaint();
            }
        }
    }

    @Override
    public void updateMissionEvent(MissionEvent e) {
        if (Exploration.SITE_EXPLORATION_EVENT.equals(e.getType())) {
            Exploration mission = (Exploration) e.getSource();
            String siteName = (String) e.getTarget();
            double completion = mission.getExplorationSiteCompletion().get(siteName);
            if (sitePanes.containsKey(siteName)) {
                sitePanes.get(siteName).updateCompletion(completion);
            }
        }
    }
    
    /**
     * Inner class panel for displaying exploration site info.
     */
    private class ExplorationSitePanel extends JPanel {
        
        // Data members
        private double completion;
        private JProgressBar completionBar;
        
        /**
         * Constructor
         * @param siteName the site name.
         * @param completion the completion level.
         */
        ExplorationSitePanel(String siteName, double completion) {
            // Use JPanel constructor.
            super();
            
            this.completion = completion;
            
            setLayout(new GridLayout(1, 2));
            setBorder(new MarsPanelBorder());
            
            JLabel nameLabel = new JLabel(siteName, SwingConstants.LEFT);
            add(nameLabel);
            
            JPanel barPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            add(barPanel);
            
            completionBar = new JProgressBar(0, 100);
            completionBar.setStringPainted(true);
            completionBar.setValue((int) (completion * 100D));
            barPanel.add(completionBar);
        }
        
        /**
         * Updates the completion.
         * @param completion the site completion level.
         */
        void updateCompletion(double completion) {
            if (this.completion != completion) {
                this.completion = completion;
                completionBar.setValue((int) (completion * 100D));
            }
        }
    }
}