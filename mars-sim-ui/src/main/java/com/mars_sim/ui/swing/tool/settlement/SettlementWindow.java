/*
 * Mars Simulation Project
 * SettlementWindow.java
 * @date 2023-05-14
 * @author Lars Naesbye Christensen
 */

package com.mars_sim.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.plaf.LayerUI;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.mapdata.location.LocalPosition;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ConfigurableWindow;
import com.mars_sim.ui.swing.MainDesktopPane;
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
	private static final int HEIGHT_STATUS_BAR = 16;
	
	public static final String NAME = Msg.getString("SettlementWindow.title"); //$NON-NLS-1$
	public static final String ICON = "settlement_map";

	private static final String POPULATION = " Pop: ";
	private static final String WHITESPACES_2 = " ";
	private static final String CLOSE_PARENT = ") ";
	private static final String WITHIN_BLDG = " Building: (";
	private static final String SETTLEMENT_MAP = " Map: (";
	private static final String PIXEL_MAP = " Window: (";

	private JLabel buildingXYLabel;
	private JLabel mapXYLabel;
	private JLabel windowXYLabel;
	private JLabel popLabel;
	private JPanel subPanel;

	/** The status bar. */
	private JStatusBar statusBar;
	/** Map panel. */
	private SettlementMapPanel mapPanel;

	private Font font0 = new Font(Font.MONOSPACED, Font.PLAIN, 11);
	
	/**
	 * Constructor.
	 *
	 * @param desktop the main desktop panel.
	 */
	public SettlementWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, desktop);

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		setBackground(Color.BLACK);

		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);

		// Creates the status bar for showing the x/y coordinates and population
        statusBar = new JStatusBar(1, 1, HEIGHT_STATUS_BAR);
        statusBar.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        popLabel = new JLabel();
        popLabel.setFont(font0);
  
        JPanel gridPanel = new JPanel(new GridLayout(1, 4));
          
	    buildingXYLabel = new JLabel();
	    buildingXYLabel.setFont(font0);
  		
	    windowXYLabel = new JLabel();
	    windowXYLabel.setFont(font0);
    
	    mapXYLabel = new JLabel();
	    mapXYLabel.setFont(font0);
    
	    gridPanel.add(popLabel);
	    gridPanel.add(windowXYLabel);
	    gridPanel.add(mapXYLabel);
	    gridPanel.add(buildingXYLabel);
	    statusBar.addFullBarComponent(gridPanel, false);
	    
        // Create subPanel for housing the settlement map
		subPanel = new JPanel(new BorderLayout());
		mainPanel.add(subPanel, BorderLayout.CENTER);
		subPanel.setBackground(Color.BLACK);

		mapPanel = new SettlementMapPanel(desktop, this, desktop.getMainWindow().getConfig().getInternalWindowProps(NAME));
		mapPanel.createUI();

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

	private String format0(double x, double y) {
		return Math.round(x*100.00)/100.00 + ", " + Math.round(y*100.00)/100.00;
	}

	private String format1(double x, double y) {
		return (int)x + ", " + (int)y;
	}

	/**
	 * Sets the label of the coordinates within a building.
	 *
	 * @param x
	 * @param y
	 * @param blank
	 */
	void setBuildingXYCoord(double x, double y, boolean blank) {
		if (blank) {
			buildingXYLabel.setText("");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(WITHIN_BLDG).append(format0(x, y)).append(CLOSE_PARENT);
			buildingXYLabel.setText(sb.toString());
		}
	}

	/**
	 * Sets the x/y pixel label of the settlement window.
	 *
	 * @param point
	 * @param blank
	 */
	void setPixelXYCoord(double x, double y, boolean blank) {
		if (blank) {
			windowXYLabel.setText("");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(PIXEL_MAP).append(format1(x, y)).append(CLOSE_PARENT);
			windowXYLabel.setText(sb.toString());
		}
	}

	/**
	 * Sets the label of the settlement map coordinates.
	 *
	 * @param point
	 * @param blank
	 */
	void setMapXYCoord(Point.Double point, boolean blank) {
		if (blank) {
			mapXYLabel.setText("");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(SETTLEMENT_MAP).append(format0(point.getX(), point.getY())).append(CLOSE_PARENT);
			mapXYLabel.setText(sb.toString());
		}
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
	 * Displays a Vehicle in the appropriate Settlement map. The map will be switched to 
	 * the appropriate Settlement and focused on the Vehicle. The Vehicle labels will be enabled.
	 * 
	 * @param vv Vehicle to display
	 */
    public void displayVehicle(Vehicle vv) {
		if (vv.isInSettlement()) {
			refocusMap(vv.getSettlement(), vv.getPosition());
			mapPanel.setShowVehicleLabels(true);
		}
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


	@Override
	public void destroy() {
		buildingXYLabel = null;
		windowXYLabel = null;
		mapXYLabel = null;
		popLabel = null;
		subPanel = null;
		
		statusBar = null;
		mapPanel.destroy();
		
		mapPanel = null;
		desktop = null;

		font0 = null;
	}
}
