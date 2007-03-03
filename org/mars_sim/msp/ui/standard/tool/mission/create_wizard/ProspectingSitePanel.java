package org.mars_sim.msp.ui.standard.tool.mission.create_wizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.tool.map.EllipseLayer;
import org.mars_sim.msp.ui.standard.tool.map.MapPanel;
import org.mars_sim.msp.ui.standard.tool.map.MapUtils;
import org.mars_sim.msp.ui.standard.tool.map.NavpointEditLayer;
import org.mars_sim.msp.ui.standard.tool.map.SurfMarsMap;
import org.mars_sim.msp.ui.standard.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitLabelMapLayer;

class ProspectingSitePanel extends WizardPanel {

	private final static String NAME = "Prospecting Site";
	
	private final static double RANGE_MODIFIER = .95D;
	
	private MapPanel mapPane;
	private EllipseLayer ellipseLayer;
	private NavpointEditLayer navLayer;
	private boolean navSelected;
	private IntPoint navOffset;
	private JLabel locationLabel;
	private int pixelRange;
	
	ProspectingSitePanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new MarsPanelBorder());
		
		JLabel titleLabel = new JLabel("Choose ice collection site.");
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(titleLabel);
		
		mapPane = new MapPanel();
		mapPane.addMapLayer(new UnitIconMapLayer(mapPane));
		mapPane.addMapLayer(new UnitLabelMapLayer());
		mapPane.addMapLayer(ellipseLayer = new EllipseLayer(Color.GREEN));
		mapPane.addMapLayer(navLayer = new NavpointEditLayer(mapPane, false));
		mapPane.addMouseListener(new NavpointMouseListener());
		mapPane.addMouseMotionListener(new NavpointMouseMotionListener());
		mapPane.setMaximumSize(mapPane.getPreferredSize());
		mapPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(mapPane);
		
		locationLabel = new JLabel("Location: ", JLabel.CENTER);
		locationLabel.setFont(locationLabel.getFont().deriveFont(Font.BOLD));
		locationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(locationLabel);
		
		add(Box.createVerticalStrut(10));
		
		JLabel instructionLabel = new JLabel("Drag navpoint flag to desired ice collection site.");
		instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.BOLD));
		instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(instructionLabel);
		
		add(Box.createVerticalGlue());
	}
	
	String getPanelName() {
		return NAME;
	}

	void commitChanges() {
		IntPoint navpointPixel = navLayer.getNavpointPosition(0);
		Coordinates navpoint = getCenterCoords().convertRectToSpherical(navpointPixel.getiX() - 150, 
				navpointPixel.getiY() - 150);
		getWizard().getMissionData().setIceCollectionSite(navpoint);
		getWizard().getMissionData().createMission();
	}

	void clearInfo() {
		getWizard().setButtonEnabled(CreateMissionWizard.FINAL_BUTTON, false);
		navLayer.clearNavpointPositions();
	}

	void updatePanel() {
		try {
			double range = (getWizard().getMissionData().getRover().getRange() * RANGE_MODIFIER) / 2D;
			pixelRange = convertRadiusToMapPixels(range);
			ellipseLayer.setEllipseDetails(new IntPoint(150, 150), new IntPoint(150, 150), (pixelRange * 2));
			IntPoint initialNavpointPos = new IntPoint(150, 150 - (pixelRange / 2));
			navLayer.addNavpointPosition(initialNavpointPos);
			Coordinates initialNavpoint = getCenterCoords().convertRectToSpherical(0, (-1 * (pixelRange / 2)));
			locationLabel.setText("Location: " + initialNavpoint.getFormattedString());
			mapPane.showMap(getCenterCoords());
		}
		catch (Exception e) {}
		
		getWizard().setButtonEnabled(CreateMissionWizard.FINAL_BUTTON, true);
	}
	
	private Coordinates getCenterCoords() {
		return getWizard().getMissionData().getStartingSettlement().getCoordinates();
	}
	
	private int convertRadiusToMapPixels(double radius) {
		return MapUtils.getPixelDistance(radius, SurfMarsMap.TYPE);
	}
	
	private class NavpointMouseListener extends MouseAdapter {
	
		public void mousePressed(MouseEvent event) {
			if (navLayer.overNavIcon(event.getX(), event.getY()) == 0) {
				navSelected = true;
				navLayer.selectNavpoint(0);
				navOffset = determineOffset(event.getX(), event.getY());
				ellipseLayer.setDisplayEllipse(true);
				mapPane.repaint();
			}
		}
		
		private IntPoint determineOffset(int x, int y) {
			int xOffset = navLayer.getNavpointPosition(0).getiX() - x;
			int yOffset = navLayer.getNavpointPosition(0).getiY() - y;
			return new IntPoint(xOffset, yOffset);
		}
	
		public void mouseReleased(MouseEvent event) {
			navSelected = false;
			navLayer.clearSelectedNavpoint();
			ellipseLayer.setDisplayEllipse(false);
			mapPane.repaint();
		}
	}
	
	private class NavpointMouseMotionListener extends MouseMotionAdapter {
		
		public void mouseDragged(MouseEvent event) {
			if (navSelected) {
				int displayX = event.getPoint().x + navOffset.getiX();
				int displayY = event.getPoint().y + navOffset.getiY();
				IntPoint displayPos = new IntPoint(displayX, displayY);
				if (withinBounds(displayPos)) {
					navLayer.setNavpointPosition(0, displayPos);
					Coordinates center = getWizard().getMissionData().getStartingSettlement().getCoordinates();
					Coordinates navpoint = center.convertRectToSpherical(displayPos.getiX() - 150, displayPos.getiY() - 150);
					locationLabel.setText("Location: " + navpoint.getFormattedString());
				
					mapPane.repaint();
				}
			}
		}
		
		private boolean withinBounds(IntPoint position) {
			boolean result = true;
			
			if (!navLayer.withinDisplayEdges(position)) result = false;
			
			int radius = (int) Math.round(Math.sqrt(Math.pow(150D - position.getX(), 2D) + Math.pow(150D - position.getY(), 2D)));
			if (radius > pixelRange) result = false;
			
			return result;
		}
	}
}