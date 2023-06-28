/*
 * Mars Simulation Project
 * SiteTabPanel.java
 * @date 2023-06-26
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;

/**
 * The tab panel for showing site details.
 */
@SuppressWarnings("serial")
public class SiteTabPanel extends JPanel implements MissionListener, UnitListener {

	// Custom mission panel IDs.
//	private static final String EMPTY = Msg.getString("SiteTabPanel.empty"); //$NON-NLS-1$

	private static final int MAX_LENGTH = 48;
	private static final int HEIGHT_1 = 150;
	
	// Private members
	private JLabel vehicleStatusLabel;

	private JButton centerMapButton;

	private CardLayout customPanelLayout;

	private JPanel missionCustomPane;

	private Mission missionCache;
	private Vehicle currentVehicle;
	
	private MissionWindow missionWindow;
	private MainDesktopPane desktop;

	private Map<String, MissionCustomInfoPanel> customInfoPanels;


	/**
	 * Constructor.
	 *
	 * @param desktop the main desktop panel.
	 */
	public SiteTabPanel(MainDesktopPane desktop, MissionWindow missionWindow) {
		// User JPanel constructor.
		super();
		// Initialize data members.
		this.desktop = desktop;
		this.missionWindow = missionWindow;
		
		// Set the layout.
		setLayout(new BorderLayout());
        setMaximumSize(new Dimension(MissionWindow.WIDTH - MissionWindow.LEFT_PANEL_WIDTH, MissionWindow.HEIGHT));
        
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

		// Create the main panel.
		JPanel mainBox = new JPanel(new BorderLayout(1, 1));
		scrollPane.setViewportView(mainBox);

	}

	
	private JPanel initCustomMissionPane() {

		// Create the mission custom panel.
		customPanelLayout = new CardLayout(10, 10);
		missionCustomPane = new JPanel(customPanelLayout);
		missionCustomPane.setAlignmentX(Component.RIGHT_ALIGNMENT);

		Border blackline = StyleManager.createLabelBorder("Mission Specific");
		missionCustomPane.setBorder(blackline);
		
		// Create custom empty panel.
		JPanel emptyCustomPanel = new JPanel();
	
		return missionCustomPane;
	}


	public void setCurrentMission(Mission mission) {
		if (missionCache != null) {
			if (!missionCache.equals(mission)) {
				missionCache = mission;
			}
		}
		else {
			missionCache = mission;
		}
	}
	
	public Mission getCurrentMission() {
		return missionCache;
	}

	/**
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document},
	 * and a {@link PropertyChangeListener} on the text component to detect
	 * if the {@code Document} itself is replaced.
	 *
	 * @param text any text component, such as a {@link JTextField}
	 *        or {@link JTextArea}
	 * @param changeListener a listener to receieve {@link ChangeEvent}s
	 *        when the text is changed; the source object for the events
	 *        will be the text component
	 * @throws NullPointerException if either parameter is null
	 */
	public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
	    Objects.requireNonNull(text);
	    Objects.requireNonNull(changeListener);
	    DocumentListener dl = new DocumentListener() {
	        private int lastChange = 0, lastNotifiedChange = 0;

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	            changedUpdate(e);
	        }

	        @Override
	        public void removeUpdate(DocumentEvent e) {
	            changedUpdate(e);
	        }

	        @Override
	        public void changedUpdate(DocumentEvent e) {
	            lastChange++;
	            SwingUtilities.invokeLater(() -> {
	                if (lastNotifiedChange != lastChange) {
	                    lastNotifiedChange = lastChange;
	                    changeListener.stateChanged(new ChangeEvent(text));
	                }
	            });
	        }
	    };
	    text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
	        Document d1 = (Document)e.getOldValue();
	        Document d2 = (Document)e.getNewValue();
	        if (d1 != null) d1.removeDocumentListener(dl);
	        if (d2 != null) d2.addDocumentListener(dl);
	        dl.changedUpdate(null);
	    });
	    Document d = text.getDocument();
	    if (d != null) d.addDocumentListener(dl);
	}

	/**
	 * Sets to the given mission.
	 *
	 * @param newMission
	 */
	public void setMission(Mission newMission) {
		// Remove this as previous mission listener.
		if (missionCache != null)
			missionCache.removeMissionListener(this);

		if (newMission == null) {	
			clearInfo();
			return;
		}
				
		missionCache = newMission;
		
		// Add this as listener for new mission.
		newMission.addMissionListener(this);
		
		setCurrentMission(newMission);
		// Update info on Main tab
		updateMainTab(newMission);
		// Update custom mission panel.
		updateCustomPanel(newMission);
	}


	/**
	 * Updates the mission content on the Main tab.
	 *
	 * @param mission
	 */
	public void updateMainTab(Mission mission) {

		if (mission == null || missionCache == null) {	
			clearInfo();
			return;
		}


		// Add mission listener.
		mission.addMissionListener(this);
		missionCache = mission;
	}


	/**
	 * Clears the mission content on the Main tab.
	 */
	public void clearInfo() {

		if (missionCache != null) {
			missionCache.removeMissionListener(this);
		}
		missionCache = null;
		
		if (currentVehicle != null)
			currentVehicle.removeUnitListener(this);
		currentVehicle = null;
		
//		customPanelLayout.show(missionCustomPane, EMPTY);
	}

	/**
	 * Updates the custom mission panel with a mission.
	 *
	 * @param mission the mission.
	 */
	private void updateCustomPanel(Mission mission) {
		boolean hasMissionPanel = false;
		if (mission != null) {		
			String missionClassName = mission.getClass().getName();
			if (customInfoPanels.containsKey(missionClassName)) {
				hasMissionPanel = true;
				MissionCustomInfoPanel panel = customInfoPanels.get(missionClassName);
				customPanelLayout.show(missionCustomPane, missionClassName);
				panel.updateMission(mission);
			}
		}

//		if (!hasMissionPanel)
//			customPanelLayout.show(missionCustomPane, EMPTY);
	}

	/**
	 * Mission event update.
	 */
	public void missionUpdate(MissionEvent e) {
		if (e.getSource().equals(missionCache)) {
			SwingUtilities.invokeLater(new MissionEventUpdater(e, this));
		}
	}

	/**
	 * Updates the custom mission panels with a mission event.
	 *
	 * @param e the mission event.
	 */
	private void updateCustomPanelMissionEvent(MissionEvent e) {
		Mission mission = (Mission) e.getSource();
		if (mission != null) {
			String missionClassName = mission.getClass().getName();
			if (customInfoPanels.containsKey(missionClassName)) {
				customInfoPanels.get(missionClassName).updateMissionEvent(e);
			}
		}
	}

	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		if ((((Unit)event.getSource()).getUnitType() == UnitType.VEHICLE)
			&& event.getSource().equals(currentVehicle)) {
//				SwingUtilities.invokeLater(new VehicleInfoUpdater(event));
		}
	}

	public MissionWindow getMissionWindow() {
		return missionWindow;
	}

	public void destroy() {
		vehicleStatusLabel = null;
		centerMapButton = null;
		customPanelLayout = null;
		missionCustomPane = null;
		missionCache = null;
		currentVehicle = null;
		missionWindow = null;
		desktop = null;
	}
	
	/**
	 * Gets the main desktop.
	 *
	 * @return desktop.
	 */
	private MainDesktopPane getDesktop() {
		return desktop;
	}

	private class MissionEventUpdater implements Runnable {

		private MissionEvent event;
		private SiteTabPanel panel;

		private MissionEventUpdater(MissionEvent event, SiteTabPanel panel) {
			this.event = event;
			this.panel = panel;
		}

		public void run() {
			Mission mission = (Mission) event.getSource();
			MissionEventType type = event.getType();


			// Update custom mission panel.
			updateCustomPanelMissionEvent(event);
		}
	}

}
