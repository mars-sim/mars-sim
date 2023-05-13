/*
 * Mars Simulation Project
 * SettlementWindow.java
 * @date 2021-12-06
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.plaf.LayerUI;

import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ConfigurableWindow;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.SpotlightLayerUI;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;

/**
 * The SettlementWindow is a tool window that displays the Settlement Map Tool.
 */
@SuppressWarnings("serial")
public class SettlementWindow extends ToolWindow implements ConfigurableWindow {

	// default logger.
	// private static final Logger logger = Logger.getLogger(SettlementWindow.class.getName());

	private static final int HORIZONTAL = 800;
	private static final int VERTICAL = 800;
	
	public static final String NAME = Msg.getString("SettlementWindow.title"); //$NON-NLS-1$
	public static final String ICON = "settlement_map";

	private static final String POPULATION = "  Population : ";
	private static final String WHITESPACES_2 = "  ";
	private static final String CLOSE_PARENT = ")  ";
	private static final String WITHIN_BLDG = "  Building : (";
	private static final String SETTLEMENT_MAP = "  Map : (";
	private static final String PIXEL_MAP = "  Window : (";

	private JLabel buildingXYLabel;
	private JLabel mapXYLabel;
	private JLabel pixelXYLabel;
	private JLabel popLabel;
	private JPanel subPanel;

	/** The status bar. */
	private JStatusBar statusBar;
	/** Map panel. */
	private SettlementMapPanel mapPanel;

	private Font sansSerif12Plain = new Font("SansSerif", Font.PLAIN, 12);
	private Font sansSerif13Bold = new Font("SansSerif", Font.BOLD, 13);
	
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
        statusBar = new JStatusBar(0, 0, 16);
        statusBar.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        popLabel = new JLabel();
        popLabel.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
        popLabel.setFont(sansSerif13Bold);
        popLabel.setForeground(Color.DARK_GRAY);
        
	    buildingXYLabel = new JLabel();
	    buildingXYLabel.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
	    buildingXYLabel.setFont(sansSerif13Bold);
	    buildingXYLabel.setForeground(Color.GREEN.darker().darker().darker());
	    
	    mapXYLabel = new JLabel();
	    mapXYLabel.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
	    mapXYLabel.setFont(sansSerif13Bold);
	    mapXYLabel.setForeground(Color.ORANGE.darker());
	    
	    pixelXYLabel = new JLabel();
	    pixelXYLabel.setFont(sansSerif13Bold);
	    pixelXYLabel.setForeground(Color.GRAY);

        statusBar.addLeftComponent(popLabel, false);
        statusBar.addCenterComponent(buildingXYLabel, false);
        statusBar.addRightComponent(pixelXYLabel, false);
        statusBar.addRightComponent(mapXYLabel, false);
        
        // Create subPanel for housing the settlement map
		subPanel = new JPanel(new BorderLayout());
		mainPanel.add(subPanel, BorderLayout.CENTER);
		subPanel.setBackground(Color.BLACK);

		mapPanel = new SettlementMapPanel(desktop, this, desktop.getMainWindow().getConfig().getInternalWindowProps(NAME));
		mapPanel.createUI();

		// Added SpotlightLayerUI
		LayerUI<JPanel> layerUI = new SpotlightLayerUI(mapPanel);
		JLayer<JPanel> jlayer = new JLayer<JPanel>(mapPanel, layerUI);
		subPanel.add(jlayer, BorderLayout.CENTER);

		setSize(new Dimension(HORIZONTAL, VERTICAL));
		setPreferredSize(new Dimension(HORIZONTAL, VERTICAL));
		setMinimumSize(new Dimension(HORIZONTAL / 2, VERTICAL / 2));
		setClosable(true);
		setResizable(false);
		setMaximizable(true);

		setVisible(true);
	}

	private String format0(double x, double y) {
//		return String.format("%6.2f,%6.2f", x, y);
		return Math.round(x*100.00)/100.00 + ", " + Math.round(y*100.00)/100.00;
	}

	private String format1(double x, double y) {
//		return String.format("%6.2f,%6.2f", x, y);
		return (int)x + ", " + (int)y;
	}

	/**
	 * Sets the label of the coordinates within a building
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
	 * Sets the x/y pixel label of the settlement window
	 *
	 * @param point
	 * @param blank
	 */
	void setPixelXYCoord(double x, double y, boolean blank) {
		if (blank) {
			pixelXYLabel.setText("");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(PIXEL_MAP).append(format1(x, y)).append(CLOSE_PARENT);
			pixelXYLabel.setText(sb.toString());
		}
	}

	/**
	 * Sets the label of the settlement map coordinates
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
	 * Sets the population label
	 *
	 * @param pop
	 */
	public void setPop(int pop) {
        popLabel.setText(POPULATION + pop + WHITESPACES_2);
	}

	/**
	 * Update this tool based on a change of time
	 * @param pulse Clock pulse
	 */
	@Override
	public void update(ClockPulse pulse) {
		mapPanel.update(pulse);
	}


	@Override
	public void destroy() {
		buildingXYLabel = null;
		pixelXYLabel = null;
		mapXYLabel = null;
		popLabel = null;
		statusBar = null;
		mapPanel.destroy();
		mapPanel = null;
		desktop = null;

	}

	/**
	 * Center the map panel on a position in a Settlement.
	 * @param settlement To display
	 * @parma position Location position within the set
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
	 * Dispay a Vehicle in the appropirate Settlement map. The map will be switched to 
	 * the appropriate Settlement and focused on the Vehicle. The Vehicle labels will be enabled.
	 * @param vv Vehicle to display
	 */
    public void displayVehicle(Vehicle vv) {
		if (vv.isInSettlement()) {
			refocusMap(vv.getSettlement(), vv.getPosition());
			mapPanel.setShowVehicleLabels(true);
		}
    }

	/**
	 * Display a worker in the settlement map. This caters for the Worker
	 * 1. In a Building
	 * 2. In a Vehicle
	 * 3. Outsid edoing a local EVA
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
	 * Dispay a Robot in the appropirate Settlement map. The map will be switched to 
	 * the appropriate Settlement and focused on the Robot. The Robot labels will be enabled.
	 * @param t Robot to display
	 */
    public void displayRobot(Robot r) {
		if (displayWorker(r)) {
			mapPanel.setShowRobotLabels(true);

			if (mapPanel.getSelectedRobot() != null && mapPanel.getSelectedRobot() != r)
				mapPanel.selectRobot(r);
		}
    }

	/**
	 * Dispay a Person in the appropirate Settlement map. The map will be switched to 
	 * the appropriate Settlement and focused on the Person. The Person labels will be enabled.
	 * @param p Person to display
	 */
    public void displayPerson(Person p) {
		if (displayWorker(p)) {
			mapPanel.setShowPersonLabels(true);

			if (mapPanel.getSelectedPerson() != null && mapPanel.getSelectedPerson() != p)
				mapPanel.selectPerson(p);
		}
    }

	/**
	 * Get the current user configured properties
	 */
	@Override
	public Properties getUIProps() {
		return mapPanel.getUIProps();
	}

}
