/**
 * Mars Simulation Project
 * TabPanelBots.java
 * @version 3.1.0 2017-02-14
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
//import org.mars_sim.msp.ui.swing.tool.monitor.personTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * This is a tab panel for robots.
 */
public class TabPanelBots
extends TabPanel
implements MouseListener, ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private JLabel robotNumLabel;
	private JLabel robotCapLabel;
	private RobotListModel robotListModel;
	private JList<Robot> robotList;
	private JScrollPane robotScrollPanel;
	private int robotNumCache;
	private int robotCapacityCache;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelBots(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelBots.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelBots.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Settlement settlement = (Settlement) unit;

		JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePane);

		JLabel titleLabel = new JLabel(Msg.getString("TabPanelBots.title"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		titlePane.add(titleLabel);


		// Create robot count panel
		JPanel robotCountPanel = new JPanel(new GridLayout(2, 2, 0, 0));
		robotCountPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(robotCountPanel);


		// Create robot num label
		robotNumCache = settlement.getNumCurrentRobots();
		robotNumLabel = new JLabel(Msg.getString("TabPanelBots.robot",
		        robotNumCache), JLabel.CENTER); //$NON-NLS-1$
		robotCountPanel.add(robotNumLabel);

		// Create robot capacity label
		robotCapacityCache = settlement.getRobotCapacity();
		robotCapLabel = new JLabel(Msg.getString("TabPanelBots.robotCapacity",
		        robotCapacityCache), JLabel.CENTER); //$NON-NLS-1$
		robotCountPanel.add(robotCapLabel);


		// Create spring layout robot display panel
		JPanel robotDisplayPanel = new JPanel(new SpringLayout());//FlowLayout(FlowLayout.LEFT));
		robotDisplayPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(robotDisplayPanel);

		// Create scroll panel for robot list.
		robotScrollPanel = new JScrollPane();
		robotScrollPanel.setPreferredSize(new Dimension(175, 250));
		robotDisplayPanel.add(robotScrollPanel);

		// Create robot list model
		robotListModel = new RobotListModel(settlement);

		// Create robot list
		robotList = new JList<Robot>(robotListModel);
		robotList.addMouseListener(this);
		robotScrollPanel.setViewportView(robotList);

		// Create robot monitor button
		JButton monitorButton = new JButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.addActionListener(this);
		monitorButton.setToolTipText(Msg.getString("TabPanelBots.tooltip.monitor")); //$NON-NLS-1$
		robotDisplayPanel.add(monitorButton);

		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(robotDisplayPanel,
		                                1, 2, //rows, cols
		                                30, 10,        //initX, initY
		                                10, 10);       //xPad, yPad
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		Settlement settlement = (Settlement) unit;

		// Update robot num
		if (robotNumCache != settlement.getNumCurrentRobots()) {
			robotNumCache = settlement.getNumCurrentRobots();
			robotNumLabel.setText(Msg.getString("TabPanelBots.robot",
			        robotNumCache)); //$NON-NLS-1$
		}

		// Update robot capacity
		if (robotCapacityCache != settlement.getRobotCapacity()) {
			robotCapacityCache = settlement.getRobotCapacity();
			robotCapLabel.setText(Msg.getString("TabPanelBots.robotCapacity",
			        robotCapacityCache)); //$NON-NLS-1$
		}

		// Update robot list
		robotListModel.update();
		robotScrollPanel.validate();
	}

	/**
	 * List model for settlement robot.
	 */
	private class RobotListModel extends AbstractListModel<Robot> {

	    /** default serial id. */
	    private static final long serialVersionUID = 1L;

	    private Settlement settlement;
	    private List<Robot> robotList;

	    private RobotListModel(Settlement settlement) {
	        this.settlement = settlement;

	        robotList = new ArrayList<Robot>(settlement.getRobots());
	        Collections.sort(robotList);
	    }

        @Override
        public Robot getElementAt(int index) {

            Robot result = null;

            if ((index >= 0) && (index < robotList.size())) {
                result = robotList.get(index);
            }

            return result;
        }

        @Override
        public int getSize() {
            return robotList.size();
        }

        /**
         * Update the robot list model.
         */
        public void update() {

            if (!robotList.containsAll(settlement.getRobots()) ||
                    !settlement.getRobots().containsAll(robotList)) {

                List<Robot> oldRobotList = robotList;

                List<Robot> tempRobotList = new ArrayList<Robot>(settlement.getRobots());
                Collections.sort(tempRobotList);

                robotList = tempRobotList;
                fireContentsChanged(this, 0, getSize());

                oldRobotList.clear();
            }
        }
	}

	/**
	 * Action event occurs.
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		// If the robot monitor button was pressed, create tab in monitor tool.
		//desktop.addModel(new RobotTableModel((Settlement) unit, false));
	}

	/**
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {

		// If double-click, open robot window.
		if (event.getClickCount() >= 2) {
			Robot robot = (Robot) robotList.getSelectedValue();
			if (robot != null) {
				desktop.openUnitWindow(robot, false);
			}
		}
	}

	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
}
