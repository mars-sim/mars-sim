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

import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.plaf.LayerUI;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.SpotlightLayerUI;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;

/**
 * The SettlementWindow is a tool window that displays the Settlement Map Tool.
 */
@SuppressWarnings("serial")
public class SettlementWindow extends ToolWindow {

	// default logger.
	// private static final Logger logger = Logger.getLogger(SettlementWindow.class.getName());

	/** Tool name. */
	public static final String NAME = Msg.getString("SettlementWindow.title"); //$NON-NLS-1$
	public static final String ICON = "settlement_map";

	private static final String POPULATION = "  Population : ";
	private static final String WHITESPACES_2 = "  ";
	private static final String CLOSE_PARENT = ")  ";
	private static final String WITHIN_BLDG = "  Building : (";
	private static final String SETTLEMENT_MAP = "  Map : (";
	private static final String PIXEL_MAP = "  Window : (";

	private static final int HORIZONTAL = 800;// 630;
	private static final int VERTICAL = 800;// 590;

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
        statusBar = new JStatusBar(1, 1, 20);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        popLabel = new JLabel();
        popLabel.setFont(sansSerif13Bold);
        popLabel.setForeground(Color.DARK_GRAY);
	    buildingXYLabel = new JLabel();
	    buildingXYLabel.setFont(sansSerif12Plain);
	    buildingXYLabel.setForeground(Color.GREEN.darker().darker().darker());
	    mapXYLabel = new JLabel();
	    mapXYLabel.setFont(sansSerif12Plain);
	    mapXYLabel.setForeground(Color.ORANGE.darker());
	    pixelXYLabel = new JLabel();
	    pixelXYLabel.setFont(sansSerif12Plain);
	    pixelXYLabel.setForeground(Color.GRAY);

	    JPanel rightPanel = new JPanel();
		rightPanel.add(buildingXYLabel);
	    rightPanel.add(mapXYLabel);

        statusBar.addLeftComponent(pixelXYLabel, true);
        statusBar.addCenterComponent(popLabel, true);
        statusBar.addRightComponent(rightPanel, true);

        // Create subPanel for housing the settlement map
		subPanel = new JPanel(new BorderLayout());
		mainPanel.add(subPanel, BorderLayout.CENTER);
		subPanel.setBackground(Color.BLACK);

		mapPanel = new SettlementMapPanel(desktop, this);
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

	/**
	 * Gets the settlement map panel.
	 *
	 * @return the settlement map panel.
	 */
	public SettlementMapPanel getMapPanel() {
		return mapPanel;
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

}
