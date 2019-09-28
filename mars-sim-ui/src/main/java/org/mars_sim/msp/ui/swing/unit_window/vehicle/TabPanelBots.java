/**
 * Mars Simulation Project
 * TabPanelBots.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.monitor.RobotTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/** 
 * The TabPanelBots is a tab panel for a vehicle's bots crew information.
 */
@SuppressWarnings("serial")
public class TabPanelBots
extends TabPanel
implements MouseListener, ActionListener {

	private WebLabel crewNumLabel;
	private WebLabel crewCapLabel;
	private DefaultListModel<Robot> crewListModel;
	//private DefaultListModel<Unit> crewListModel;
	private JList<Robot> crewList;
	//private JList<Unit> crewList;
	private Collection<Robot> crewCache;
	//private Collection<Unit> crewCache;

	private int crewNumCache;
	private int crewCapacityCache;

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Crewable instance. */
	private Crewable crewable;
	
	/**
	 * Constructor.
	 * @param vehicle the vehicle.
	 * @param desktop the main desktop.
	 */
	public TabPanelBots(Vehicle vehicle, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelBots.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelBots.tooltip"), //$NON-NLS-1$
			vehicle, desktop
		);

		crewable = (Crewable) vehicle;

	}

	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;

		// Prepare title label.
		WebPanel titlePanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelBots.title"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		titlePanel.add(titleLabel);
		topContentPanel.add(titlePanel);
		
		// Create crew count panel
		WebPanel crewCountPanel = new WebPanel(new GridLayout(2, 1, 0, 0));
//		crewCountPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(crewCountPanel);

		// Create crew num label
		crewNumCache = crewable.getRobotCrewNum();
		crewNumLabel = new WebLabel(Msg.getString("TabPanelBots.crew", crewNumCache), WebLabel.CENTER); //$NON-NLS-1$
		crewCountPanel.add(crewNumLabel);

		// Create crew capacity label
		crewCapacityCache = crewable.getRobotCrewCapacity();
		crewCapLabel = new WebLabel(Msg.getString("TabPanelBots.crewCapacity", crewCapacityCache), WebLabel.CENTER); //$NON-NLS-1$
		crewCountPanel.add(crewCapLabel);

		// Create crew display panel
		WebPanel crewDisplayPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT));
//		crewDisplayPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(crewDisplayPanel);

		// Create scroll panel for crew list.
		WebScrollPane crewScrollPanel = new WebScrollPane();
		crewScrollPanel.setPreferredSize(new Dimension(175, 100));
		crewDisplayPanel.add(crewScrollPanel);

		// Create crew list model
		crewListModel = new DefaultListModel<Robot>();
		//crewListModel = new DefaultListModel<Unit>();
		crewCache = crewable.getRobotCrew();
		//crewCache = crewable.getUnitCrew();
		Iterator<Robot> i = crewCache.iterator();
		//Iterator<Unit> i = crewCache.iterator();
		while (i.hasNext()) crewListModel.addElement(i.next());

		// Create crew list
		crewList = new JList<Robot>(crewListModel);
		//crewList = new JList<Unit>(crewListModel);
		crewList.addMouseListener(this);
		crewScrollPanel.setViewportView(crewList);

		// Create crew monitor button
		WebButton monitorButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.addActionListener(this);
		monitorButton.setToolTipText(Msg.getString("TabPanelBots.tooltip.monitor")); //$NON-NLS-1$
		crewDisplayPanel.add(monitorButton);
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		if (!uiDone)
			initializeUI();

		// Update crew num
		if (crewNumCache !=  crewable.getRobotCrewNum()) {
			crewNumCache = crewable.getRobotCrewNum();
			crewNumLabel.setText(Msg.getString("TabPanelBots.crew", crewNumCache)); //$NON-NLS-1$
		}

		// Update crew capacity
		if (crewCapacityCache != crewable.getRobotCrewCapacity()) {
			crewCapacityCache =  crewable.getRobotCrewCapacity();
			crewCapLabel.setText(Msg.getString("TabPanelBots.crewCapacity", crewCapacityCache)); //$NON-NLS-1$
		}

		// Update crew list
		//if (!Arrays.equals(crewCache.toArray(), crewable.getUnitCrew().toArray())) {
		if (!Arrays.equals(crewCache.toArray(), crewable.getRobotCrew().toArray())) {
			//crewCache = crewable.getUnitCrew();
			crewCache = crewable.getRobotCrew();
			crewListModel.clear();
			Iterator<Robot> i = crewCache.iterator();
			//Iterator<Unit> i = crewCache.iterator();
			while (i.hasNext()) crewListModel.addElement(i.next());
		}

	}

	/** 
	 * Action event occurs.
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		// If the crew monitor button was pressed, create tab in monitor tool.
		Vehicle vehicle = (Vehicle) unit;
		Crewable crewable = (Crewable) vehicle;
		desktop.addModel(new RobotTableModel(crewable));
	}

	/** 
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {
		// If double-click, open person window.
		if (event.getClickCount() >= 2) {
			Robot robot= (Robot) crewList.getSelectedValue();
			if (robot != null) desktop.openUnitWindow(robot, false);
		}
	}

	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	
	public void destroy() {
		crewNumLabel = null; 
		crewCapLabel = null; 
		crewListModel = null; 
		crewList = null; 
		crewCache = null; 
	}
}
