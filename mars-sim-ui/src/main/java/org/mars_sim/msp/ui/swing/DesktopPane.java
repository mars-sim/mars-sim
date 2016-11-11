/**
 * Mars Simulation Project
 * DesktopPane.java
 * @version 3.1.0 2016-10-22
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.CheckMenuItem;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.ui.astroarts.OrbitViewer;
import org.mars_sim.msp.ui.javafx.BrowserJFX;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.sound.OGGSoundClip;
import org.mars_sim.msp.ui.swing.sound.SoundConstants;
import org.mars_sim.msp.ui.swing.tool.FXInSwing;
import org.mars_sim.msp.ui.swing.tool.JFXPannableView;
import org.mars_sim.msp.ui.swing.tool.MarqueeTicker;
//import org.mars_sim.msp.ui.swing.tool.MarsViewer;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.EventTableModel;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.UnitTableModel;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.TransportWizard;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindowFactory;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindowListener;

/**
 * Each DesktopPane class is a swing desktop for housing a tool window
 */
public class DesktopPane
extends JDesktopPane
implements ComponentListener, UnitListener, UnitManagerListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(DesktopPane.class.getName());

	/** True if this MainDesktopPane hasn't been displayed yet. */
	private boolean firstDisplay;
	private boolean isTransportingBuilding = false, isConstructingSite = false;
	
	private ImageIcon backgroundImageIcon;
	/** Label that contains the tiled background. */
	private JLabel backgroundLabel;

	private Building building;
	
	private MainScene mainScene;
	
	/**
	 * Constructor 1 for setting up javaFX desktop
	 * @param mainScene the main scene
	 */
	public DesktopPane(MainScene mainScene) {
	   	//logger.info("MainDesktopPane's constructor is on " + Thread.currentThread().getName() + " Thread");
		this.mainScene = mainScene;

		initialize();
	}
	
	public void initialize() {
		// Set background color to black
		setBackground(Color.black);

		// set desktop manager
		setDesktopManager(new MainDesktopManager());

		// Set component listener
		addComponentListener(this);
	
		// Create background label and set it to the back layer
		backgroundImageIcon = new ImageIcon();
		backgroundLabel = new JLabel(backgroundImageIcon);
		add(backgroundLabel, Integer.MIN_VALUE);
		backgroundLabel.setLocation(0, 0);
		moveToBack(backgroundLabel);

		// Initialize firstDisplay to true
		firstDisplay = true;
		
		setPreferredSize(new Dimension(1280, 1024));

		prepareListeners();
	}

	/**
	 * Constructor 2 for setting up swing desktop
	 */
	public DesktopPane() {
		
		initialize();
	}
	
	/** Returns the MainScene instance
	 *  @return MainScene instance
	 */
	public MainScene getMainScene() {
		return mainScene;
	}


	/**
	 * Create background tile when MainDesktopPane is first
	 * displayed. Recenter logoLabel on MainWindow and set
	 * backgroundLabel to the size of MainDesktopPane.
	 * @param e the component event
	 */
	@Override
	public void componentResized(ComponentEvent e) {

		// If displayed for the first time, create background image tile.
		// The size of the background tile cannot be determined during construction
		// since it requires the MainDesktopPane be displayed first.
		if (firstDisplay) {
			ImageIcon baseImageIcon = ImageLoader.getIcon(Msg.getString("img.background")); //$NON-NLS-1$
			Dimension screen_size =
				Toolkit.getDefaultToolkit().getScreenSize();
			Image backgroundImage =
				createImage((int) screen_size.getWidth(),
						(int) screen_size.getHeight());
			Graphics backgroundGraphics = backgroundImage.getGraphics();

			for (int x = 0; x < backgroundImage.getWidth(this);
			x += baseImageIcon.getIconWidth()) {
				for (int y = 0; y < backgroundImage.getHeight(this);
				y += baseImageIcon.getIconHeight()) {
					backgroundGraphics.drawImage(
							baseImageIcon.getImage(), x, y, this);
				}
			}

			backgroundImageIcon.setImage(backgroundImage);

			backgroundLabel.setSize(getSize());

			firstDisplay = false;
		}

		// Set the backgroundLabel size to the size of the desktop
		backgroundLabel.setSize(getSize());

	}

	/**
	 * sets up this class with two listeners
	 */
	// 2014-12-19 Added prepareListeners()
	public void prepareListeners() {
	   	//logger.info("MainDesktopPane's prepareListeners() is on " + Thread.currentThread().getName() + " Thread");

		// Add addUnitManagerListener()
		UnitManager unitManager = Simulation.instance().getUnitManager();
		unitManager.addUnitManagerListener(this);

		// Add addUnitListener()
		Collection<Settlement> settlements = unitManager.getSettlements();
		List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
		Settlement settlement = settlementList.get(0);
		List<Building> buildings = settlement.getBuildingManager().getACopyOfBuildings();
		building = buildings.get(0);
		//building.addUnitListener(this); // not working
		Iterator<Settlement> i = settlementList.iterator();
		while (i.hasNext()) {
			i.next().addUnitListener(this);
		}

	   	//logger.info("MainDesktopPane's prepareListeners() is done");
	}
	
	// Additional Component Listener methods implemented but not used.
	@Override
	public void componentMoved(ComponentEvent e) {
		logger.info("DesktopPane : componentMoved()");
		updateToolWindow();
	}
	
	@Override
	public void componentShown(ComponentEvent e) {
		logger.info("DesktopPane : componentShown()");
		JInternalFrame[] frames = this.getAllFrames();
		for (JInternalFrame f : frames) {
			//((ToolWindow)f).update();
			f.updateUI();
			SwingUtilities.updateComponentTreeUI(f);
		}
	}
	
	@Override
	public void componentHidden(ComponentEvent e) {}

	public void updateToolWindow() {
		logger.info("DesktopPane : updateToolWindow()");
		JInternalFrame[] frames = this.getAllFrames();
		for (JInternalFrame f : frames) {
			//f.updateUI();	
			//SwingUtilities.updateComponentTreeUI(f);		
			((ToolWindow)f).update();
		}
	}

	@Override
	public Component add(Component comp) {
		super.add(comp);
		centerJIF(comp);
		return comp;
	}
	
	public void centerJIF(Component comp) {
	    Dimension desktopSize = getSize();
	    Dimension jInternalFrameSize = comp.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    comp.setLocation(width, height);
	    comp.setVisible(true);
	}
	
	public void unitManagerUpdate(UnitManagerEvent event) {

		Object unit = event.getUnit();
		if (unit instanceof Settlement) {
			Settlement settlement = (Settlement) unit;
			UnitManagerEventType eventType = event.getEventType();
			if (eventType == UnitManagerEventType.ADD_UNIT) { // REMOVE_UNIT;
				//System.out.println("MainDesktopPane : " + settlement.getName() + " just added");
				settlement.addUnitListener(this);
			}
			else if (eventType == UnitManagerEventType.REMOVE_UNIT) { // REMOVE_UNIT;
				//System.out.println("MainDesktopPane : " + settlement.getName() + " just deleted");
				settlement.removeUnitListener(this);
			}
			
			updateToolWindow();
		}
	}


	public void unitUpdate(UnitEvent event) {
		UnitEventType eventType = event.getType();
		//System.out.println("MainDesktopPane : unitUpdate() " + eventType);
		Object target = event.getTarget();
		if (eventType == UnitEventType.START_TRANSPORT_WIZARD_EVENT) {

			building = (Building) target; // overwrite the dummy building object made by the constructor
			BuildingManager mgr = building.getBuildingManager();
			//settlement = mgr.getSettlement();

			if (!isTransportingBuilding) {
				isTransportingBuilding = true;	
				mainScene.openTransportWizard(mgr);
				Simulation.instance().getTransportManager().setIsTransportingBuilding(false);
			}
			else {
				//mgr.getResupply();
			}

		} else if (eventType == UnitEventType.END_TRANSPORT_WIZARD_EVENT) {
			isTransportingBuilding = false;
		}

		else if (eventType == UnitEventType.START_CONSTRUCTION_WIZARD_EVENT) {
			BuildingConstructionMission mission = (BuildingConstructionMission) target;

			if (!isConstructingSite) {
				isConstructingSite = true;
				mainScene.openConstructionWizard(mission);

			}
		}

		else if (eventType == UnitEventType.END_CONSTRUCTION_WIZARD_EVENT) {
			isConstructingSite = false;
		}

	}

	public void destroy() {
		backgroundImageIcon  = null;
		backgroundLabel = null;
		building = null;
		mainScene = null;
	}

}