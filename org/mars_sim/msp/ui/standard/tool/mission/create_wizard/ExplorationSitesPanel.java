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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

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
import org.mars_sim.msp.ui.standard.tool.map.EllipseLayer;
import org.mars_sim.msp.ui.standard.tool.map.MapPanel;
import org.mars_sim.msp.ui.standard.tool.map.MapUtils;
import org.mars_sim.msp.ui.standard.tool.map.NavpointEditLayer;
import org.mars_sim.msp.ui.standard.tool.map.SurfMarsMap;
import org.mars_sim.msp.ui.standard.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitLabelMapLayer;

class ExplorationSitesPanel extends WizardPanel {

	private final static String NAME = "Exploration Sites";
	
	private final static double RANGE_MODIFIER = .95D;

	private MapPanel mapPane;
	private EllipseLayer ellipseLayer;
	private NavpointEditLayer navLayer;
	private int navSelected;
	private IntPoint navOffset;
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
		mapPane.addMapLayer(ellipseLayer = new EllipseLayer(Color.GREEN));
		mapPane.addMapLayer(navLayer = new NavpointEditLayer(mapPane));
		mapPane.setBorder(new MarsPanelBorder());
		mapPane.addMouseListener(new NavpointMouseListener());
		mapPane.addMouseMotionListener(new NavpointMouseMotionListener());
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
			return getWizard().getMissionData().getRover().getRange() * RANGE_MODIFIER;
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
	
	private Coordinates determineNewSiteLocation(Coordinates prevNav, Coordinates nextNav, double range) {
		double fociDistance = prevNav.getDistance(nextNav);
		double distanceFromCenterOfAxis = Math.sqrt(Math.pow(((range + fociDistance) / 2D), 2D) - Math.pow((fociDistance / 2D), 2D));
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
	
	private class NavpointMouseListener extends MouseAdapter {
		
		public void mousePressed(MouseEvent event) {
			navSelected = navLayer.overNavIcon(event.getX(), event.getY());
			if (navSelected > -1) {
				navLayer.selectNavpoint(navSelected);
				navOffset = determineOffset(event.getX(), event.getY());
				
				IntPoint prevNavpoint = MapUtils.getRectPosition(getPreviousNavpoint(), getCenterCoords(), SurfMarsMap.TYPE);
				IntPoint nextNavpoint = MapUtils.getRectPosition(getNextNavpoint(), getCenterCoords(), SurfMarsMap.TYPE);
				int radiusPixels = convertDistanceToMapPixels(getRadius());
				ellipseLayer.setEllipseDetails(prevNavpoint, nextNavpoint, radiusPixels);
				ellipseLayer.setDisplayEllipse(true);
				mapPane.repaint();
			}
		}
		
		private IntPoint determineOffset(int x, int y) {
			int xOffset = navLayer.getNavpointPosition(navSelected).getiX() - x;
			int yOffset = navLayer.getNavpointPosition(navSelected).getiY() - y;
			return new IntPoint(xOffset, yOffset);
		}
		
		private double getRadius() {
			Coordinates currentNavpoint = getCurrentNavpoint();
			Coordinates prevNavpoint = getPreviousNavpoint();
			Coordinates nextNavpoint = getNextNavpoint();
			double currentDistance = prevNavpoint.getDistance(currentNavpoint) + currentNavpoint.getDistance(nextNavpoint);
			double straightDistance = prevNavpoint.getDistance(nextNavpoint);
			return currentDistance - straightDistance + getRemainingRange(false);
		}
		
		private int convertDistanceToMapPixels(double distance) {
			return MapUtils.getPixelDistance(distance, SurfMarsMap.TYPE);
		}
	
		public void mouseReleased(MouseEvent event) {
			navSelected = -1;
			navLayer.clearSelectedNavpoint();
			ellipseLayer.setDisplayEllipse(false);
			mapPane.repaint();
		}
	}
	
	private class NavpointMouseMotionListener extends MouseMotionAdapter {
		
		public void mouseDragged(MouseEvent event) {
			if (navSelected > -1) {
				int displayX = event.getPoint().x + navOffset.getiX();
				int displayY = event.getPoint().y + navOffset.getiY();
				IntPoint displayPos = new IntPoint(displayX, displayY);
				Coordinates center = getWizard().getMissionData().getStartingSettlement().getCoordinates();
				Coordinates navpoint = center.convertRectToSpherical(displayPos.getiX() - 150, displayPos.getiY() - 150);
				
				if (withinBounds(displayPos, navpoint)) {
					navLayer.setNavpointPosition(navSelected, new IntPoint(displayX, displayY));
					SitePanel selectedSitePane = (SitePanel) siteListPane.getComponent(navSelected);
					selectedSitePane.setLocation(navpoint);
					addButton.setEnabled(canAddMoreSites());
					mapPane.repaint();
				}
			}
		}
		
		private boolean withinBounds(IntPoint position, Coordinates location) {
			boolean result = true;
			if (!navLayer.withinDisplayEdges(position)) result = false;
			if (getRemainingRange(false) < getDistanceDiff(location)) result = false;
			return result;
		}
		
		private double getDistanceDiff(Coordinates newSite) {
			Coordinates prevNavpoint = getPreviousNavpoint();
			Coordinates nextNavpoint = getNextNavpoint();
			
			Coordinates currentSite = getCurrentNavpoint();
			double currentSiteDistance = prevNavpoint.getDistance(currentSite) + currentSite.getDistance(nextNavpoint);
			
			double newSiteDistance = prevNavpoint.getDistance(newSite) + newSite.getDistance(nextNavpoint);
			
			return newSiteDistance - currentSiteDistance;
		}
	}
	
	private Coordinates getPreviousNavpoint() {
		Coordinates prevNavpoint = null;
		if (navSelected > 0) prevNavpoint = ((SitePanel) siteListPane.getComponent(navSelected - 1)).getSite();
		else prevNavpoint = getCenterCoords();
		return prevNavpoint;
	}
	
	private Coordinates getNextNavpoint() {
		Coordinates nextNavpoint = null;
		if (navSelected < (siteListPane.getComponentCount() - 1)) 
			nextNavpoint = ((SitePanel) siteListPane.getComponent(navSelected + 1)).getSite();
		else nextNavpoint = getCenterCoords();
		return nextNavpoint;
	}
	
	private Coordinates getCurrentNavpoint() {
		return ((SitePanel) siteListPane.getComponent(navSelected)).getSite();
	}
}