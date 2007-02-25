package org.mars_sim.msp.ui.standard.tool.mission.create_wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.tool.map.CenteredCircleLayer;
import org.mars_sim.msp.ui.standard.tool.map.MapPanel;
import org.mars_sim.msp.ui.standard.tool.map.NavpointEditLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitLabelMapLayer;

class ExplorationSitesPanel extends WizardPanel {

	private final static String NAME = "Exploration Sites";

	private MapPanel mapPane;
	private CenteredCircleLayer circleLayer;
	private NavpointEditLayer navLayer;
	private boolean navSelected;
	private IntPoint navOffset;
	private JPanel siteListPane;
	
	ExplorationSitesPanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new MarsPanelBorder());
		
		JLabel titleLabel = new JLabel("Choose the exploration sites.");
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(titleLabel);
		
		add(Box.createVerticalStrut(10));
		
		JPanel centerPane = new JPanel(new BorderLayout(0, 0));
		centerPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		centerPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 350));
		add(centerPane);
		
		JPanel mapMainPane = new JPanel(new BorderLayout(0, 0));
		centerPane.add(mapMainPane, BorderLayout.WEST);
		
		mapPane = new MapPanel();
		mapPane.addMapLayer(new UnitIconMapLayer(mapPane));
		mapPane.addMapLayer(new UnitLabelMapLayer());
		mapPane.addMapLayer(circleLayer = new CenteredCircleLayer(Color.GREEN));
		mapPane.addMapLayer(navLayer = new NavpointEditLayer(mapPane));
		mapPane.setBorder(new MarsPanelBorder());
		// mapPane.addMouseListener(new NavpointMouseListener());
		// mapPane.addMouseMotionListener(new NavpointMouseMotionListener());
		mapMainPane.add(mapPane, BorderLayout.NORTH);
		
		JLabel instructionLabel = new JLabel("Drag navpoint flags to the desired exploration sites.", JLabel.CENTER);
		instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.BOLD));
		mapMainPane.add(instructionLabel, BorderLayout.SOUTH);
		
		JPanel sitePane = new JPanel(new BorderLayout(0, 0));
		sitePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		centerPane.add(sitePane, BorderLayout.CENTER);
		
        // Create scroll panel for site list.
        JScrollPane siteScrollPane = new JScrollPane();
        sitePane.add(siteScrollPane, BorderLayout.CENTER);
        
        JPanel siteListMainPane = new JPanel(new BorderLayout(0, 0));
        siteScrollPane.setViewportView(siteListMainPane);
        
        siteListPane = new JPanel();
        siteListPane.setLayout(new BoxLayout(siteListPane, BoxLayout.Y_AXIS));
        siteListMainPane.add(siteListPane, BorderLayout.NORTH);
        
        JPanel addButtonPane = new JPanel(new FlowLayout());
        sitePane.add(addButtonPane, BorderLayout.SOUTH);
        
        JButton addButton = new JButton("Add Site");
        addButton.addActionListener(
        		new ActionListener() {
    				public void actionPerformed(ActionEvent e) {
    					siteListPane.add(new SitePanel(siteListPane.getComponentCount(), getNewSiteLocation()));
    					validate();
    				}
    			});
        addButtonPane.add(addButton);
		
		add(Box.createVerticalGlue());
	}
	
	String getPanelName() {
		return NAME;
	}

	void commitChanges() {
		// Coordinates center = getWizard().getMissionData().getStartingSettlement().getCoordinates();
		// IntPoint navpointPixel = navLayer.getDisplayPosition();
		// Coordinates navpoint = center.convertRectToSpherical(navpointPixel.getiX() - 150, navpointPixel.getiY() - 150);
		int siteNum = siteListPane.getComponentCount();
		Coordinates[] sites = new Coordinates[siteNum];
		for (int x = 0; x < siteNum; x++) 
			sites[x] = ((SitePanel) siteListPane.getComponent(x)).getSite();
		getWizard().getMissionData().setExplorationSites(sites);
		getWizard().getMissionData().createMission();
	}

	void clearInfo() {
		siteListPane.removeAll();
	}

	void updatePanel() {
		Coordinates center = getWizard().getMissionData().getStartingSettlement().getCoordinates();
		siteListPane.add(new SitePanel(0, new Coordinates(0D, 0D)));
		mapPane.showMap(center);
		getWizard().setButtonEnabled(CreateMissionWizard.FINAL_BUTTON, true);
	}
	
	private Coordinates getNewSiteLocation() {
		return new Coordinates(Math.PI / 2D, Math.PI / 2D);
	}
	
	private void updateSiteNumbers() {
		for (int x = 0; x < siteListPane.getComponentCount(); x++) {
			SitePanel sitePane = (SitePanel) siteListPane.getComponent(x);
			sitePane.setSiteNum(x);
		}
	}
	
	private class SitePanel extends JPanel {
		
		private Coordinates site;
		private int siteNum;
		private JLabel siteNumLabel;
		private JLabel siteLocationLabel;
		
		SitePanel(int siteNum, Coordinates site) {
			// Use JPanel constructor.
			super();
		
			this.siteNum = siteNum;
			this.site = site;
			
			setLayout(new GridLayout(1, 3));
			setBorder(new MarsPanelBorder());
			
			siteNumLabel = new JLabel(" Site " + (siteNum + 1));
			add(siteNumLabel);
			
			siteLocationLabel = new JLabel(site.getFormattedString());
			add(siteLocationLabel);
			
			if (siteNum > 0) {
				JButton removeButton = new JButton("Remove");
				removeButton.addActionListener(
						new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								setVisible(false);
								siteListPane.remove(getSiteNum());
								updateSiteNumbers();
								siteListPane.validate();
							}
						});
				add(removeButton);
			}
			else add(new JPanel());
		}
		
		void setSiteNum(int siteNum) {
			this.siteNum = siteNum;
			siteNumLabel.setText(" Site " + (siteNum + 1));
		}
		
		int getSiteNum() {
			return siteNum;
		}
		
		void setLocation(Coordinates site) {
			this.site = site;
			siteLocationLabel.setText(site.getFormattedString());
		}
		
		Coordinates getSite() {
			return site;
		}
	}
}