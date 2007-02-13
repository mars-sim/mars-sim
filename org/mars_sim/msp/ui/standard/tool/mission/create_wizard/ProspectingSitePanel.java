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
import org.mars_sim.msp.simulation.Direction;
import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.tool.map.CenteredCircleLayer;
import org.mars_sim.msp.ui.standard.tool.map.MapPanel;
import org.mars_sim.msp.ui.standard.tool.map.MapUtils;
import org.mars_sim.msp.ui.standard.tool.map.NavpointEditLayer;
import org.mars_sim.msp.ui.standard.tool.map.SurfMarsMap;
import org.mars_sim.msp.ui.standard.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitLabelMapLayer;

class ProspectingSitePanel extends WizardPanel {

	private final static String NAME = "Prospecting Site";
	
	private MapPanel mapPane;
	private CenteredCircleLayer circleLayer;
	private NavpointEditLayer navLayer;
	private boolean navSelected;
	private IntPoint navOffset;
	private JLabel locationLabel;
	
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
		mapPane.addMapLayer(circleLayer = new CenteredCircleLayer(Color.GREEN));
		mapPane.addMapLayer(navLayer = new NavpointEditLayer(mapPane));
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
		Coordinates center = getWizard().getMissionData().getStartingSettlement().getCoordinates();
		IntPoint navpointPixel = navLayer.getDisplayPosition();
		Coordinates navpoint = center.convertRectToSpherical(navpointPixel.getiX() - 150, navpointPixel.getiY() - 150);
		getWizard().getMissionData().setIceCollectionSite(navpoint);
		getWizard().getMissionData().createMission();
	}

	void clearInfo() {
		getWizard().setButtonEnabled(CreateMissionWizard.FINAL_BUTTON, false);
	}

	void updatePanel() {
		try {
			Coordinates center = getWizard().getMissionData().getStartingSettlement().getCoordinates();
			double range = getWizard().getMissionData().getRover().getRange() / 2D;
			int pixelRange = convertRadiusToMapPixels(range);
			circleLayer.setRadius(pixelRange);
			navLayer.setRadiusLimit(pixelRange);
			IntPoint initialNavpointPos = new IntPoint(150, 150 - (pixelRange / 2));
			navLayer.setDisplayPosition(initialNavpointPos);
			Coordinates initialNavpoint = center.convertRectToSpherical(0, (-1 * (pixelRange / 2)));
			locationLabel.setText("Location: " + initialNavpoint.getFormattedString());
			mapPane.showMap(center);
		}
		catch (Exception e) {}
		
		getWizard().setButtonEnabled(CreateMissionWizard.FINAL_BUTTON, true);
	}
	
	private int convertRadiusToMapPixels(double radius) {
		Coordinates center = new Coordinates((Math.PI / 2D), 0D);
		Coordinates startPosition = center.getNewLocation(new Direction(0D), radius);
		IntPoint startPoint = MapUtils.getRectPosition(startPosition, center, SurfMarsMap.TYPE);
		return 150 - startPoint.getiY();
	}
	
	private class NavpointMouseListener extends MouseAdapter {
	
		public void mousePressed(MouseEvent event) {
			if (navLayer.isOverNavIcon(event.getX(), event.getY())) {
				navSelected = true;
				navLayer.selectIcon(true);
				navOffset = determineOffset(event.getX(), event.getY());
				mapPane.repaint();
			}
		}
		
		private IntPoint determineOffset(int x, int y) {
			int xOffset = navLayer.getDisplayPosition().getiX() - x;
			int yOffset = navLayer.getDisplayPosition().getiY() - y;
			return new IntPoint(xOffset, yOffset);
		}
	
		public void mouseReleased(MouseEvent event) {
			navSelected = false;
			navLayer.selectIcon(false);
			mapPane.repaint();
		}
	}
	
	private class NavpointMouseMotionListener extends MouseMotionAdapter {
		
		public void mouseDragged(MouseEvent event) {
			if (navSelected) {
				int displayX = event.getPoint().x + navOffset.getiX();
				int displayY = event.getPoint().y + navOffset.getiY();
				navLayer.setDisplayPosition(new IntPoint(displayX, displayY));
				
				// Note: display position in nav layer might not have changed.
				IntPoint displayPos = navLayer.getDisplayPosition();
				Coordinates center = getWizard().getMissionData().getStartingSettlement().getCoordinates();
				Coordinates navpoint = center.convertRectToSpherical(displayPos.getiX() - 150, displayPos.getiY() - 150);
				locationLabel.setText("Location: " + navpoint.getFormattedString());
				
				mapPane.repaint();
			}
		}
	}
}