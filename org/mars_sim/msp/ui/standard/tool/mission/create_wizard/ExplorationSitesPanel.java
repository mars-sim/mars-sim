package org.mars_sim.msp.ui.standard.tool.mission.create_wizard;

import java.awt.BorderLayout;
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
import org.mars_sim.msp.simulation.Direction;
import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.simulation.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.tool.map.CenteredCircleLayer;
import org.mars_sim.msp.ui.standard.tool.map.MapPanel;
import org.mars_sim.msp.ui.standard.tool.map.MapUtils;
import org.mars_sim.msp.ui.standard.tool.map.NavpointEditLayer;
import org.mars_sim.msp.ui.standard.tool.map.SurfMarsMap;
import org.mars_sim.msp.ui.standard.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitLabelMapLayer;

class ExplorationSitesPanel extends WizardPanel {

	private final static String NAME = "Exploration Sites";

	private MapPanel mapPane;
	// private CenteredCircleLayer circleLayer;
	private NavpointEditLayer navLayer;
	// private boolean navSelected;
	// private IntPoint navOffset;
	private JPanel siteListPane;
	private JButton addButton;
	private double range;
	private double missionTimeLimit;
	private double timePerSite;
	
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
		// mapPane.addMapLayer(circleLayer = new CenteredCircleLayer(Color.GREEN));
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
        
        addButton = new JButton("Add Site");
        addButton.addActionListener(
        		new ActionListener() {
    				public void actionPerformed(ActionEvent e) {
    					SitePanel sitePane = new SitePanel(siteListPane.getComponentCount(), getNewSiteLocation());
    					siteListPane.add(sitePane);
    					navLayer.addNavpointPosition(MapUtils.getRectPosition(sitePane.getSite(), getCenterCoords(), SurfMarsMap.TYPE));
    					mapPane.repaint();
    					addButton.setEnabled(canAddMoreSites());
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
		getWizard().getMissionData().setExplorationSites(getSites());
		getWizard().getMissionData().createMission();
	}

	void clearInfo() {
		siteListPane.removeAll();
		navLayer.clearNavpointPositions();
		getWizard().setButtonEnabled(CreateMissionWizard.FINAL_BUTTON, false);
	}

	void updatePanel() {
		range = getRange();
		missionTimeLimit = getMissionTimeLimit();
		timePerSite = getTimePerSite();
		
		// int pixelRange = convertRadiusToMapPixels(range / 2D);
		// navLayer.setRadiusLimit(pixelRange);
		Coordinates startingSite = getCenterCoords().getNewLocation(new Direction(0D), range / 4D);
		SitePanel startingSitePane = new SitePanel(0, startingSite);
		siteListPane.add(startingSitePane);
		navLayer.addNavpointPosition(MapUtils.getRectPosition(startingSitePane.getSite(), getCenterCoords(), SurfMarsMap.TYPE));
		mapPane.showMap(getCenterCoords());
		addButton.setEnabled(canAddMoreSites());
		getWizard().setButtonEnabled(CreateMissionWizard.FINAL_BUTTON, true);
	}
	
	private boolean canAddMoreSites() {
		return (missionTimeLimit > (getTotalMissionTime() + getTimePerSite()));
	}
	
	private Coordinates getNewSiteLocation() {
		Coordinates result = null;
		
		Coordinates[] sites = getSites();
		Coordinates lastSite = sites[sites.length - 1];
		result = determineNewSiteLocation(lastSite, getCenterCoords(), getRemainingRange(true));
		
		return result;
	}
	
	private double getRemainingRange(boolean newSite) {
		double travelTime = missionTimeLimit - getTotalSiteTime();
		if (newSite) travelTime -= timePerSite;
		Rover rover = getWizard().getMissionData().getRover();
		double timeRange = (travelTime / 1000D) * rover.getEstimatedTravelDistancePerSol();
		double realRange = range;
		if (timeRange < range) realRange = timeRange;
		return realRange - getDistance();
	}
	
	private double getRange() {
		try {
			return getWizard().getMissionData().getRover().getRange();
		}
		catch (Exception e) {
			return 0D;
		}
	}
	
	private double getMissionTimeLimit() {
		Rover rover = getWizard().getMissionData().getRover();
		int memberNum = getWizard().getMissionData().getMembers().size();
		try {
			return CollectResourcesMission.getTotalTripTimeLimit(rover, memberNum, true);
		}
		catch (Exception e) {
			return 0D;
		}
	}
	
	private double getTimePerSite() {
    	double timePerPerson = (Exploration.SITE_GOAL / Exploration.COLLECTION_RATE) * 
    			CollectResourcesMission.EVA_COLLECTION_OVERHEAD;
    	return timePerPerson / getWizard().getMissionData().getMembers().size();
	}
	
	private double getTotalSiteTime() {
		return timePerSite * siteListPane.getComponentCount();
	}
	
	private double getTravelTime() {
		Rover rover = getWizard().getMissionData().getRover();
		return getDistance() / (rover.getEstimatedTravelDistancePerSol() / 1000D);
	}
	
	private double getTotalMissionTime() {
		return getTravelTime() + getTotalSiteTime();
	}
	
	private double getDistance() {
		double result = 0D;
		Coordinates[] sites = getSites();
		
		result += getCenterCoords().getDistance(sites[0]);
		
		for (int x = 1; x < sites.length; x++) 
			result += sites[x - 1].getDistance(sites[x]);
		
		result += sites[sites.length - 1].getDistance(getCenterCoords());
		
		return result;
	}
	
	private Coordinates[] getSites() {
		Coordinates[] result = new Coordinates[siteListPane.getComponentCount()];
		for (int x = 0; x < siteListPane.getComponentCount(); x++) 
			result[x] = ((SitePanel) siteListPane.getComponent(x)).getSite();
		
		return result;
	}
	
	private void updateSiteNumbers() {
		navLayer.clearNavpointPositions();
		for (int x = 0; x < siteListPane.getComponentCount(); x++) {
			SitePanel sitePane = (SitePanel) siteListPane.getComponent(x);
			sitePane.setSiteNum(x);
			navLayer.addNavpointPosition(MapUtils.getRectPosition(sitePane.getSite(), getCenterCoords(), SurfMarsMap.TYPE));
		}
		mapPane.repaint();
	}
	
	private Coordinates getCenterCoords() {
		return getWizard().getMissionData().getStartingSettlement().getCoordinates();
	}
	
	private int convertRadiusToMapPixels(double radius) {
		Coordinates center = new Coordinates((Math.PI / 2D), 0D);
		Coordinates startPosition = center.getNewLocation(new Direction(0D), radius);
		IntPoint startPoint = MapUtils.getRectPosition(startPosition, center, SurfMarsMap.TYPE);
		return 150 - startPoint.getiY();
	}
	
	private Coordinates determineNewSiteLocation(Coordinates prevNav, Coordinates nextNav, double range) {
		double fociDistance = prevNav.getDistance(nextNav);
		double distanceFromCenterOfAxis = Math.sqrt(Math.pow((range / 2D), 2D) - Math.pow((fociDistance / 2D), 2D));
		double initialDistanceFromAxis = distanceFromCenterOfAxis / 2D;
		double initialDistanceFromFoci = Math.sqrt(Math.pow((fociDistance / 2D), 2D) + Math.pow(initialDistanceFromAxis, 2D));
		Direction initialDirectionFromFoci = new Direction(Math.asin(initialDistanceFromAxis / initialDistanceFromFoci)); 
		Direction fociDirection = prevNav.getDirectionToPoint(nextNav);
		Direction directionToNewSite = new Direction(fociDirection.getDirection() - initialDirectionFromFoci.getDirection());
		return prevNav.getNewLocation(directionToNewSite, initialDistanceFromFoci);
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