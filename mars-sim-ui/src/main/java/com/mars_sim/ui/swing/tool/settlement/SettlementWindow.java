/*
 * Mars Simulation Project
 * SettlementWindow.java
 * @date 2025-08-07
 * @author Lars Naesbye Christensen
 */

package com.mars_sim.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.plaf.LayerUI;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ConfigurableWindow;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.JStatusBar;
import com.mars_sim.ui.swing.tool.SpotlightLayerUI;
import com.mars_sim.ui.swing.tool_window.ToolWindow;

/**
 * The SettlementWindow is a tool window that displays the Settlement Map Tool.
 */
@SuppressWarnings("serial")
public class SettlementWindow extends ToolWindow implements ConfigurableWindow {


	private static final int HORIZONTAL = 800;
	private static final int VERTICAL = 800;
	private static final int HEIGHT_STATUS_BAR = 18;
	
	public static final String NAME = "settlement_map";
	public static final String ICON = "settlement_map";
    public static final String TITLE = Msg.getString("SettlementWindow.title");

	private static final String POPULATION = " Pop: ";
	private static final String WHITESPACES_2 = " ";
	private static final String CLOSE_PARENT = ") ";
	private static final String BLDG_CENTER = "Building Center: ";
	private static final String BLDG_SPOT = " Building Spot: ";
	private static final String SETTLEMENT_MAP = " Map: ";
	private static final String PIXEL_MAP = " Window: (";

	
	private JLabel buildingSpotLabel;
	private JLabel buildingXYLabel;
	private JLabel mapXYLabel;
	private JLabel windowXYLabel;
	private JLabel popLabel;
	private JPanel subPanel;

	/** The status bar. */
	private JStatusBar statusBar;
	/** Map panel. */
	private SettlementMapPanel mapPanel;

	/**
	 * Constructor.
	 *
	 * @param desktop the main desktop panel.
	 */
	public SettlementWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, TITLE, desktop);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);//.HIDE_ON_CLOSE);

		setBackground(Color.BLACK);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);

		statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        // Create subPanel for housing the settlement map
		subPanel = new JPanel(new BorderLayout());
		mainPanel.add(subPanel, BorderLayout.CENTER);
		subPanel.setBackground(Color.BLACK);

		mapPanel = new SettlementMapPanel(desktop, this, desktop.getMainWindow().getConfig().getInternalWindowProps(NAME));
		mapPanel.createUI();
		desktop.setSettlementMapPanel(mapPanel);

		// Added SpotlightLayerUI
		LayerUI<JPanel> layerUI = new SpotlightLayerUI(mapPanel);
		JLayer<JPanel> jlayer = new JLayer<>(mapPanel, layerUI);
		subPanel.add(jlayer, BorderLayout.CENTER);

		setSize(new Dimension(HORIZONTAL, VERTICAL));
		setPreferredSize(new Dimension(HORIZONTAL, VERTICAL));
		setMinimumSize(new Dimension(HORIZONTAL / 2, VERTICAL / 2));
		setClosable(true);
		setResizable(true);
		setMaximizable(true);

		setVisible(true);
	}

	private JStatusBar createStatusBar() {
		Font font = StyleManager.getSmallFont();
		// Creates the status bar for showing the x/y coordinates and population
		JStatusBar statusBar = new JStatusBar(3, 3, HEIGHT_STATUS_BAR);
        statusBar.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));

        popLabel = new JLabel();
//        popLabel.setPreferredSize(new Dimension(50, HEIGHT_STATUS_BAR));
        popLabel.setVerticalAlignment(SwingConstants.CENTER);
        popLabel.setHorizontalAlignment(SwingConstants.LEFT);
        popLabel.setFont(font);
          
	    buildingXYLabel = new JLabel();
//	    buildingXYLabel.setPreferredSize(new Dimension(130, HEIGHT_STATUS_BAR));
	    buildingXYLabel.setVerticalAlignment(SwingConstants.CENTER);
	    buildingXYLabel.setHorizontalAlignment(SwingConstants.LEFT);
	    buildingXYLabel.setFont(font);
  		
	    buildingSpotLabel = new JLabel();
//	    buildingSpotLabel.setPreferredSize(new Dimension(130, HEIGHT_STATUS_BAR));
	    buildingSpotLabel.setVerticalAlignment(SwingConstants.CENTER);
	    buildingSpotLabel.setHorizontalAlignment(SwingConstants.LEFT);
	    buildingSpotLabel.setFont(font);
	    
	    windowXYLabel = new JLabel();
//	    windowXYLabel.setPreferredSize(new Dimension(120, HEIGHT_STATUS_BAR));
	    windowXYLabel.setVerticalAlignment(SwingConstants.CENTER);
	    windowXYLabel.setHorizontalAlignment(SwingConstants.LEFT);
	    windowXYLabel.setFont(font);
    
	    mapXYLabel = new JLabel();
//	    mapXYLabel.setPreferredSize(new Dimension(110, HEIGHT_STATUS_BAR));
	    mapXYLabel.setVerticalAlignment(SwingConstants.CENTER);
	    mapXYLabel.setHorizontalAlignment(SwingConstants.LEFT);
	    mapXYLabel.setFont(font);
    
	    JPanel popPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
	    popPanel.add(popLabel);
	    statusBar.addLeftComponent(popPanel, false);
	    
	    JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 4));
//	    centerPanel.setPreferredSize(new Dimension(300, HEIGHT_STATUS_BAR));
	    centerPanel.setAlignmentY(CENTER_ALIGNMENT);
	    centerPanel.setAlignmentX(CENTER_ALIGNMENT);
	    
	    centerPanel.add(mapXYLabel);
	    centerPanel.add(buildingXYLabel);
	    centerPanel.add(buildingSpotLabel);
	    
	    statusBar.addCenterComponent(centerPanel, false);

	    JPanel winPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
	    winPanel.add(windowXYLabel);
	    statusBar.addRightComponent(winPanel, false);

	    return statusBar;
	}
	
	private String format1(double x, double y) {
		return (int)x + ", " + (int)y;
	}

	/**
	 * Sets the label of the center position of a building.
	 *
	 * @param pos
	 * @param blank
	 */
	void setBuildingXYCoord(LocalPosition pos, boolean blank) {
		if (blank) {
			buildingXYLabel.setText("");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(BLDG_CENTER).append(pos.getShortFormat());
			buildingXYLabel.setText(sb.toString());
		}
	}

	/**
	 * Sets the pointer position within a building.
	 *
	 * @param pos
	 * @param blank
	 */
	void setBuildingPointerXYCoord(LocalPosition pos, boolean blank) {
		if (blank) {
			buildingSpotLabel.setText("");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(BLDG_SPOT).append(pos.getShortFormat());
			buildingSpotLabel.setText(sb.toString());
		}
	}
	
	
	/**
	 * Sets the x/y pixel label of the settlement window.
	 *
	 * @param point
	 */
	void setPixelXYCoord(double x, double y) {
		StringBuilder sb = new StringBuilder();
		sb.append(PIXEL_MAP).append(format1(x, y)).append(CLOSE_PARENT);
		windowXYLabel.setText(sb.toString());
	}

	/**
	 * Sets the label of the settlement map coordinates.
	 *
	 * @param point
	 */
	void setMapXYCoord(LocalPosition point) {
		mapXYLabel.setText(SETTLEMENT_MAP + point.getShortFormat());
	}

	/**
	 * Sets the population label.
	 *
	 * @param pop
	 */
	public void setPop(int pop) {
        popLabel.setText(POPULATION + pop + WHITESPACES_2);
	}

	/**
	 * Updates this tool based on a change of time.
	 * 
	 * @param pulse Clock pulse
	 */
	@Override
	public void update(ClockPulse pulse) {
		mapPanel.update(pulse);
		setPop(mapPanel.getSettlement().getNumCitizens());
	}

	/**
	 * Centers the map panel on a position in a Settlement.
	 * 
	 * @param settlement To display
	 * @param position Location position within the set
	 */
	private void refocusMap(Settlement settlement, LocalPosition position) {
		// Surely this should be simpler ?
		mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(settlement);

		double xLoc = position.getX();
		double yLoc = position.getY();
		double scale = mapPanel.getScale();
		mapPanel.reCenter();
		mapPanel.moveCenter(xLoc * scale, yLoc * scale);
	}

	/**
	 * Displays a position in a Settlement. The map will be flipped if needed
	 * 
	 * @param s Settlement to display
	 * @param posn Local position for focus
	 */
    public void displayPosition(Settlement s, LocalPosition posn) {
		refocusMap(s, posn);
    }

	/**
	 * Displays a worker in the settlement map. This caters for the Worker.
	 * 1. In a Building
	 * 2. In a Vehicle
	 * 3. Outside doing a local EVA
	 * 
	 * @param w Worker to display
	 */
	private boolean displayWorker(Worker w) {
		Settlement home = null;
		LocalPosition p = null;
		if (w.isInSettlement()) {
			home = w.getSettlement();
			p = w.getBuildingLocation().getPosition();
		}
		else if (w.isInVehicle()) {
			Vehicle v = w.getVehicle();
			home = v.getSettlement();
			p = v.getPosition();
		}
		else if (w.isOutside()) {
			home = w.getSettlement();
			p = w.getPosition();
		}
		else {
			return false;
		}

		refocusMap(home, p);
		return true;
	}

	/**
	 * Displays a Robot in the appropriate Settlement map. The map will be switched to 
	 * the appropriate Settlement and focused on the Robot. The Robot labels will be enabled.
	 * 
	 * @param r Robot to display
	 */
    public void displayRobot(Robot r) {
		if (displayWorker(r)) {
			mapPanel.selectRobot(r);
		}
    }

	/**
	 * Displays a Person in the appropriate Settlement map. The map will be switched to 
	 * the appropriate Settlement and focused on the Person. The Person labels will be enabled.
	 * 
	 * @param p Person to display
	 */
    public void displayPerson(Person p) {
		if (displayWorker(p)) {
			mapPanel.selectPerson(p);
		}
    }

	/**
	 * Gets the current user configured properties.
	 */
	@Override
	public Properties getUIProps() {
		return mapPanel.getUIProps();
	}

	/**
	 * Chooses the settlement.
	 * 
	 * @param settlement
	 */
	public void chooseSettlement(Settlement settlement) {
		mapPanel.setSettlement(settlement);
	}

	@Override
	public void destroy() {
		buildingSpotLabel = null;
		buildingXYLabel = null;
		windowXYLabel = null;
		mapXYLabel = null;
		popLabel = null;
		subPanel = null;
		
		statusBar = null;
		
		mapPanel.destroy();
		mapPanel = null;
		
		desktop = null;
	}
}
