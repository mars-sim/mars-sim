/**
 * Mars Simulation Project
 * ToolWindow.java
 * @version 3.1.0 2017-02-03
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.toolWindow;

import javax.swing.JInternalFrame;
import javax.swing.JRootPane;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.javafx.MainSceneMenu;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;

import javafx.application.Platform;
import javafx.scene.control.CheckMenuItem;

/**
 * The ToolWindow class is an abstract UI window for a tool.
 * Particular tool windows should be derived from this.
 */
@SuppressWarnings("restriction")
public abstract class ToolWindow
extends JInternalFrame {

	// Data members
	/** The name of the tool the window is for. */
	protected String name;
	private CheckMenuItem item;
	private MainSceneMenu msm;
	/** The main desktop. */
	protected MainDesktopPane desktop;
	private static MasterClock masterClock = Simulation.instance().getMasterClock();

	protected MainScene mainScene;
	protected MonitorWindow monitorWindow;
	//private SingleSelectionModel<?> ssm;

	/** True if window is open. */
	protected boolean opened;

	/**
	 * Constructor.
	 * @param name the name of the tool
	 * @param desktop the main desktop.
	 */
	public ToolWindow(String name, MainDesktopPane desktop) {

		// use JInternalFrame constructor
		super(
			name,
			true, // resizable
			true, // closable
			false, // maximizable
			false // iconifiable
		);

		// Initialize data members
		this.name = name;
		this.desktop = desktop;
		this.mainScene = desktop.getMainScene();

		// 2016-10-21 Remove title bar
	    //putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
	    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
	    BasicInternalFrameUI bi = (BasicInternalFrameUI)super.getUI();
	    bi.setNorthPane(null);
	    setBorder(null);

	    //getRootPane().setOpaque(false);
	    //getRootPane().setBackground(new Color(0,0,0,128));
	    //setOpaque(false);
	    //setBackground(new Color(0,0,0,128));

		if (this instanceof MonitorWindow)
			this.monitorWindow = (MonitorWindow)this;

		opened = false;

		if (mainScene != null) {
			msm = mainScene.getMainSceneMenu();
			//ssm = mainScene.getJFXTabPane().getSelectionModel();
		}

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		//setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// Set internal frame listener
		//ToolFrameListener tool = new ToolFrameListener(this);
		//addInternalFrameListener(tool);

		addInternalFrameListener(new ToolFrameListener());
	}

	/**
	 * Gets the tool name.
	 * @return tool name
	 */
	public String getToolName() {
		return name;
	}

	/**
	 * Sets the tool name.
	 * @param tool name
	 */
	public void setTitleName(String value) {
		setName(value);
	}

	/**
	 * Checks if the tool window has previously been opened.
	 * @return true if tool window has previously been opened.
	 */
	public boolean wasOpened() {
		return opened;
	}

	/**
	 * Sets if the window has previously been opened.
	 * @param opened true if previously opened.
	 */
	public void setWasOpened(boolean opened) {
		this.opened = opened;
	}

	public void closeMaps() {
		//if (desktop.isToolWindowOpen(SettlementWindow.NAME)) {
			mainScene.closeMaps();
		//}
	}
	/**
	 * Update window.
	 */
    // 2015-06-05 Added checking if the tool window is invisible/closed while its check menu item is still toggle on
    // 2015-10-01 Added Platform.runLater()
	@SuppressWarnings("restriction")
	public void update() {

		if (mainScene != null && !masterClock.isPaused()) {
/*
			if (ssm == null)
				ssm = mainScene.getJFXTabPane().getSelectionModel();

			if (ssm.isSelected(MainScene.MAIN_TAB)) {
				closeMaps();
				//System.out.println("ToolWindow : closing other tools !");
				//desktop.closeToolWindow(MonitorWindow.NAME);
				//desktop.closeToolWindow(ResupplyWindow.NAME);
				//desktop.closeToolWindow(ScienceWindow.NAME);
				//desktop.closeToolWindow(MissionWindow.NAME);
				desktop.closeToolWindow(GuideWindow.NAME);
			}

			else if (ssm.isSelected(MainScene.MAP_TAB)) {
				//System.out.println("ToolWindow : closing other tools !");
				//desktop.closeToolWindow(MonitorWindow.NAME);
				//desktop.closeToolWindow(ResupplyWindow.NAME);
				//desktop.closeToolWindow(ScienceWindow.NAME);
				//desktop.closeToolWindow(MissionWindow.NAME);
				desktop.closeToolWindow(GuideWindow.NAME);
			}

			else if (ssm.isSelected(MainScene.HELP_TAB)) {
				closeMaps();
				//desktop.openToolWindow(GuideWindow.NAME);
				//desktop.closeToolWindow(MonitorWindow.NAME);
				//desktop.closeToolWindow(ResupplyWindow.NAME);
				//desktop.closeToolWindow(ScienceWindow.NAME);
				//desktop.closeToolWindow(MissionWindow.NAME);
			}


			else if (mainScene.getJFXTabPane().getSelectionModel().isSelected(MainScene.MONITOR_TAB)) {
				closeMaps();
				//desktop.openToolWindow(MonitorWindow.NAME);
				desktop.closeToolWindow(ResupplyWindow.NAME);
				desktop.closeToolWindow(ScienceWindow.NAME);
				desktop.closeToolWindow(MissionWindow.NAME);
				desktop.closeToolWindow(GuideWindow.NAME);
			}

			else if (mainScene.getJFXTabPane().getSelectionModel().isSelected(MainScene.MISSION_TAB)) {
				closeMaps();
				//desktop.openToolWindow(MissionWindow.NAME);
				desktop.closeToolWindow(MonitorWindow.NAME);
				desktop.closeToolWindow(ResupplyWindow.NAME);
				desktop.closeToolWindow(ScienceWindow.NAME);
				desktop.closeToolWindow(GuideWindow.NAME);
			}

			else if (mainScene.getJFXTabPane().getSelectionModel().isSelected(MainScene.RESUPPLY_TAB)) {
				closeMaps();
				//desktop.openToolWindow(ResupplyWindow.NAME);
				desktop.closeToolWindow(MonitorWindow.NAME);
				desktop.closeToolWindow(ScienceWindow.NAME);
				desktop.closeToolWindow(MissionWindow.NAME);
				desktop.closeToolWindow(GuideWindow.NAME);
			}

			else if (mainScene.getJFXTabPane().getSelectionModel().isSelected(MainScene.SCIENCE_TAB)) {
				closeMaps();
				//desktop.openToolWindow(ScienceWindow.NAME);
				//desktop.closeToolWindow(MonitorWindow.NAME);
				//desktop.closeToolWindow(ResupplyWindow.NAME);
				//desktop.closeToolWindow(MissionWindow.NAME);
				desktop.closeToolWindow(GuideWindow.NAME);
			}

*/

			if ( this.isVisible() || this.isShowing() ) {
				//System.out.println("this.getToolName() is "+ this.getToolName());
				// Note: need to refresh the table column/row header
				if (this.getToolName().equals(MonitorWindow.NAME))
					monitorWindow.refreshTable();
					//pack(); // create time lag, and draw artifact

					//mainScene.setLookAndFeel(1); causing java.lang.NullPointerException at com.jidesoft.plaf.basic.BasicJideTabbedPaneUI.getFontMetrics(BasicJideTabbedPaneUI.java:5063)
					//SwingUtilities.updateComponentTreeUI(this); causing java.lang.NullPointerException
					//Platform.runLater(() -> {
						//mainScene.changeTheme(mainScene.getTheme());
					//});
					//SwingUtilities.invokeLater(() -> SwingUtilities.updateComponentTreeUI(this));
					// Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException when pressing the terminate button in eclipse
					//monitorWindow.tabChanged(false); // create time lag, draw artifact and search text out of focus
				// create time lag, draw artifact and search text out of focus
			}

			else if ( !this.isVisible() || !this.isShowing() ) { // || !this.isSelected()) { // || this.wasOpened()) {
				//System.out.println(name + " is not visible");
				Platform.runLater(() -> {
					if (msm == null)
						msm = mainScene.getMainSceneMenu();
					item = msm.getCheckMenuItem(name);
					//System.out.println(item + " is obtained");
					if (item != null) {
						// Note: need to accommodate if item is a guide window, as it is always null
						//System.out.println(item + " is obtained");
						if (item.isSelected()) {
							msm.uncheckToolWindow(name);
							//System.out.println(name + " is unchecked");
						}
					}
				});
			}
		}
	}

	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		item = null;
		msm = null;
		desktop = null;
		masterClock = null;
		mainScene = null;
		monitorWindow = null;
	}
}