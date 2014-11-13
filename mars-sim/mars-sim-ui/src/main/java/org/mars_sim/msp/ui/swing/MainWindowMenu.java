/**
 * Mars Simulation Project
 * MainWindowMenu.java
 * @version 3.07 2014-11-11
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;

/**
 * The MainWindowMenu class is the menu for the main window.
 */
public class MainWindowMenu
extends JMenuBar
implements ActionListener, MenuListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The main window frame. */
	private MainWindow mainWindow;
	/** New menu item. */
	private JMenuItem newItem;
	/** Load menu item. */
	private JMenuItem loadItem;
	/** Save menu item. */
	private JMenuItem saveItem;
	/** Save As menu item. */
	private JMenuItem saveAsItem;
	/** Exit menu item. */
	private JMenuItem exitItem;
	/** Mars navigator menu item. */
	private JCheckBoxMenuItem marsNavigatorItem;
	/** Search tool menu item. */
	private JCheckBoxMenuItem searchToolItem;
	/** Time tool menu item. */
	private JCheckBoxMenuItem timeToolItem;
	/** Monitor tool menu item. */
	private JCheckBoxMenuItem monitorToolItem;
	/** Mission tool menu item. */
	private JCheckBoxMenuItem missionToolItem;
	/** Settlement tool menu item. */
	private JCheckBoxMenuItem settlementToolItem;
	/** Science tool menu item. */
	private JCheckBoxMenuItem scienceToolItem;
	/** Resupply tool menu item. */
	private JCheckBoxMenuItem resupplyToolItem;
	/** Unit Bar menu item. */
	private JCheckBoxMenuItem showUnitBarItem;
	/** Tool Bar menu item. */
	private JCheckBoxMenuItem showToolBarItem;
	/** Mute menu item. */
	private JCheckBoxMenuItem muteItem;
	/** Volume Up menu item. */
	private JMenuItem volumeUpItem;
	/** Volume Down menu item. */
	private JMenuItem volumeDownItem;
	/** Volume Slider menu item. */
	private JSlider volumeItem;
	/** About Mars Simulation Project menu item. */
	private JMenuItem aboutMspItem;
	/** Tutorial menu item. */
	private JMenuItem tutorialItem;
	/** User Guide menu item. */
	private JMenuItem guideItem;	
	
	
	
	/** 
	 * Constructor.
	 * @param mainWindow the main window pane
	 */
	public MainWindowMenu(MainWindow mainWindow) {

		// Use JMenuBar constructor
		super();

		// Initialize data members
		this.mainWindow = mainWindow;

		// Create file menu
		JMenu fileMenu = new JMenu(Msg.getString("mainMenu.file")); //$NON-NLS-1$
		add(fileMenu);

		// Create new menu item
		ImageIcon newicon = new ImageIcon(getClass().getResource(Msg.getString("img.new"))); //$NON-NLS-1$
		newItem = new JMenuItem(Msg.getString("mainMenu.new"), newicon); //$NON-NLS-1$
		newItem.addActionListener(this);
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, false));
		newItem.setToolTipText(Msg.getString("mainMenu.tooltip.new")); //$NON-NLS-1$
		fileMenu.add(newItem);

		// Create load menu item
		ImageIcon loadicon = new ImageIcon(getClass().getResource(Msg.getString("img.open"))); //$NON-NLS-1$
		loadItem = new JMenuItem(Msg.getString("mainMenu.open"),loadicon); //$NON-NLS-1$
		loadItem.addActionListener(this);
		loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK, false));
		loadItem.setToolTipText(Msg.getString("mainMenu.tooltip.open")); //$NON-NLS-1$
		fileMenu.add(loadItem);

		fileMenu.add(new JSeparator());

		// Create save menu item
		ImageIcon saveicon = new ImageIcon(getClass().getResource(Msg.getString("img.save"))); //$NON-NLS-1$
		saveItem = new JMenuItem(Msg.getString("mainMenu.save"), saveicon); //$NON-NLS-1$
		saveItem.addActionListener(this);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK, false));
		saveItem.setToolTipText(Msg.getString("mainMenu.tooltip.save")); //$NON-NLS-1$
		fileMenu.add(saveItem);

		// Create save as menu item
		ImageIcon saveasicon = new ImageIcon(getClass().getResource(Msg.getString("img.saveAs"))); //$NON-NLS-1$
		saveAsItem = new JMenuItem(Msg.getString("mainMenu.saveAs"), saveasicon); //$NON-NLS-1$
		saveAsItem.addActionListener(this);
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK, false));
		saveAsItem.setToolTipText(Msg.getString("mainMenu.tooltip.saveAs")); //$NON-NLS-1$
		fileMenu.add(saveAsItem);

		fileMenu.add(new JSeparator());

		// Create exit menu item
		ImageIcon exiticon = new ImageIcon(getClass().getResource(Msg.getString("img.exit"))); //$NON-NLS-1$
		exitItem = new JMenuItem(Msg.getString("mainMenu.exit"), exiticon); //$NON-NLS-1$
		exitItem.addActionListener(this);
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK, false));
		exitItem.setToolTipText(Msg.getString("mainMenu.tooltip.exit")); //$NON-NLS-1$
		fileMenu.add(exitItem);

		// Create tools menu
		JMenu toolsMenu = new JMenu(Msg.getString("mainMenu.tools")); //$NON-NLS-1$
		toolsMenu.addMenuListener(this);
		add(toolsMenu);

		// Create Mars navigator menu item
		ImageIcon marsnavigatoricon = new ImageIcon(getClass().getResource(Msg.getString("img.planet"))); //$NON-NLS-1$
		marsNavigatorItem = new JCheckBoxMenuItem(NavigatorWindow.NAME, marsnavigatoricon);
		marsNavigatorItem.addActionListener(this);
		marsNavigatorItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false));
		toolsMenu.add(marsNavigatorItem);

		// Create search tool menu item
		ImageIcon searchicon = new ImageIcon(getClass().getResource(Msg.getString("img.find"))); //$NON-NLS-1$
		searchToolItem = new JCheckBoxMenuItem(SearchWindow.NAME, searchicon);
		searchToolItem.addActionListener(this);
		searchToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false));
		toolsMenu.add(searchToolItem);

		// Create time tool menu item
		ImageIcon timeicon = new ImageIcon(getClass().getResource(Msg.getString("img.time"))); //$NON-NLS-1$
		timeToolItem = new JCheckBoxMenuItem(TimeWindow.NAME, timeicon);
		timeToolItem.addActionListener(this);
		timeToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false));
		toolsMenu.add(timeToolItem);

		// Create monitor tool menu item
		ImageIcon monitoricon = new ImageIcon(getClass().getResource(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorToolItem = new JCheckBoxMenuItem(MonitorWindow.NAME, monitoricon);
		monitorToolItem.addActionListener(this);
		monitorToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false));
		toolsMenu.add(monitorToolItem);


		// Create mission tool menu item
		ImageIcon missionicon = new ImageIcon(getClass().getResource(Msg.getString("img.mission"))); //$NON-NLS-1$
		missionToolItem = new JCheckBoxMenuItem(MissionWindow.NAME, missionicon);
		missionToolItem.addActionListener(this);
		missionToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false));
		toolsMenu.add(missionToolItem);

		// Create settlement map tool menu item
		ImageIcon settlementtoolicon = new ImageIcon(getClass().getResource(Msg.getString("img.settlementMapTool"))); //$NON-NLS-1$
		settlementToolItem = new JCheckBoxMenuItem(SettlementWindow.NAME, settlementtoolicon);
		settlementToolItem.addActionListener(this);
		settlementToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0, false));
		toolsMenu.add(settlementToolItem);

		// Create science tool menu item
		ImageIcon scienceicon = new ImageIcon(getClass().getResource(Msg.getString("img.science"))); //$NON-NLS-1$
		scienceToolItem = new JCheckBoxMenuItem(ScienceWindow.NAME, scienceicon);
		scienceToolItem.addActionListener(this);
		scienceToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false));
		toolsMenu.add(scienceToolItem);

		// Create resupply tool menu item
		ImageIcon resupplyicon = new ImageIcon(getClass().getResource(Msg.getString("img.resupply"))); //$NON-NLS-1$
		resupplyToolItem = new JCheckBoxMenuItem(ResupplyWindow.NAME, resupplyicon);
		resupplyToolItem.addActionListener(this);
		resupplyToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0, false));
		toolsMenu.add(resupplyToolItem);
		
		// Create settings menu 
		JMenu settingsMenu = new JMenu(Msg.getString("mainMenu.settings")); //$NON-NLS-1$
		settingsMenu.addMenuListener(this);
		add(settingsMenu);

		// Create Show Unit Bar menu item
		showUnitBarItem = new JCheckBoxMenuItem(Msg.getString("mainMenu.unitbar")); //$NON-NLS-1$
		showUnitBarItem.addActionListener(this);
		showUnitBarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK, false));
		showUnitBarItem.setToolTipText(Msg.getString("mainMenu.tooltip.unitbar")); //$NON-NLS-1$
		settingsMenu.add(showUnitBarItem);
		
		// Create Show Tool Bar menu item
		showToolBarItem = new JCheckBoxMenuItem(Msg.getString("mainMenu.toolbar")); //$NON-NLS-1$
		showToolBarItem.addActionListener(this);
		showToolBarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK, false));
		showToolBarItem.setToolTipText(Msg.getString("mainMenu.tooltip.toolbar")); //$NON-NLS-1$
		settingsMenu.add(showToolBarItem);

		settingsMenu.add(new JSeparator());

		// Create Volume slider menu item
		
		MainDesktopPane desktop = mainWindow.getDesktop();
		final AudioPlayer soundPlayer = desktop.getSoundPlayer();
		float volume = soundPlayer.getVolume();
		int intVolume = Math.round(volume * 10F);

		volumeItem = new JSliderMW(JSlider.HORIZONTAL, 0, 10, intVolume);; //$NON-NLS-1$
		volumeItem.setMajorTickSpacing(1);
		volumeItem.setPaintTicks(true);
		volumeItem.setPaintLabels(true);
		volumeItem.setPaintTrack(true);
		volumeItem.setSnapToTicks(true);

		volumeItem.setToolTipText(Msg.getString("mainMenu.tooltip.volumeslider")); //$NON-NLS-1$
		volumeItem.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float newVolume = (float) volumeItem.getValue() / 10F;
				soundPlayer.setVolume(newVolume); //needs fix
			}		
			});
		settingsMenu.add(volumeItem);

		// Create Volume Up menu item
		volumeUpItem = new JMenuItem(Msg.getString("mainMenu.volumeUp")); //$NON-NLS-1$
		volumeUpItem.addActionListener(this);
		volumeUpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, KeyEvent.CTRL_DOWN_MASK, false));
		volumeUpItem.setToolTipText(Msg.getString("mainMenu.tooltip.volumeUp")); //$NON-NLS-1$
		settingsMenu.add(volumeUpItem);
		
		// Create Volume Up menu item
		volumeDownItem = new JMenuItem(Msg.getString("mainMenu.volumeDown")); //$NON-NLS-1$
		volumeDownItem.addActionListener(this);
		volumeDownItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, KeyEvent.CTRL_DOWN_MASK, false));
		volumeDownItem.setToolTipText(Msg.getString("mainMenu.tooltip.volumeDown")); //$NON-NLS-1$
		settingsMenu.add(volumeDownItem);
		
		muteItem = new JCheckBoxMenuItem(Msg.getString("mainMenu.mute")); //$NON-NLS-1$
		muteItem.addActionListener(this);
		muteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK, false));
		muteItem.setToolTipText(Msg.getString("mainMenu.tooltip.mute")); //$NON-NLS-1$
		settingsMenu.add(muteItem);	
		
		// Create help menu
		JMenu helpMenu = new JMenu(Msg.getString("mainMenu.help")); //$NON-NLS-1$
		helpMenu.addMenuListener(this);
		add(helpMenu);

		// Create About Mars Simulation Project menu item
		aboutMspItem = new JMenuItem(Msg.getString("mainMenu.about")); //$NON-NLS-1$
		aboutMspItem.addActionListener(this);
		aboutMspItem.setToolTipText(Msg.getString("mainMenu.tooltip.about")); //$NON-NLS-1$
		helpMenu.add(aboutMspItem);

		helpMenu.add(new JSeparator());

		// Create Tutorial menu item
		tutorialItem = new JMenuItem(Msg.getString("mainMenu.tutorial")); //$NON-NLS-1$
		tutorialItem.addActionListener(this);
		tutorialItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK, false));
		tutorialItem.setToolTipText(Msg.getString("mainMenu.tooltip.tutorial")); //$NON-NLS-1$
		helpMenu.add(tutorialItem);

		// Create User Guide menu item
		ImageIcon guideicon = new ImageIcon(getClass().getResource(Msg.getString("img.guide"))); //$NON-NLS-1$
		guideItem = new JMenuItem(Msg.getString("mainMenu.guide"), guideicon); //$NON-NLS-1$
		guideItem.addActionListener(this);
		guideItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK, false));
		guideItem.setToolTipText(Msg.getString("mainMenu.tooltip.guide")); //$NON-NLS-1$
		helpMenu.add(guideItem);
	}

	/** ActionListener method overriding. */
	@Override
	public final void actionPerformed(ActionEvent event) {
		// TODO:This runs through an awful lot of if-then-else statements 
		// and we could save cycles by using a switch-case statement [lechimp 12/11/14]
		JMenuItem selectedItem = (JMenuItem) event.getSource();
		MainDesktopPane desktop = mainWindow.getDesktop();

		if (selectedItem == exitItem) mainWindow.exitSimulation();
		else if (selectedItem == newItem) mainWindow.newSimulation();
		else if (selectedItem == saveItem) mainWindow.saveSimulation(true);
		else if (selectedItem == saveAsItem) mainWindow.saveSimulation(false);
		else if (selectedItem == loadItem) mainWindow.loadSimulation();


		if (selectedItem == marsNavigatorItem) {
			if (marsNavigatorItem.isSelected()) desktop.openToolWindow(NavigatorWindow.NAME);
			else desktop.closeToolWindow(NavigatorWindow.NAME);
		}

		if (selectedItem == searchToolItem) {
			if (searchToolItem.isSelected()) desktop.openToolWindow(SearchWindow.NAME);
			else desktop.closeToolWindow(SearchWindow.NAME);
		}

		if (selectedItem == timeToolItem) {
			if (timeToolItem.isSelected()) desktop.openToolWindow(TimeWindow.NAME);
			else desktop.closeToolWindow(TimeWindow.NAME);
		}

		if (selectedItem == monitorToolItem) {
			if (monitorToolItem.isSelected()) desktop.openToolWindow(MonitorWindow.NAME);
			else desktop.closeToolWindow(MonitorWindow.NAME);
		}


		if (selectedItem == missionToolItem) {
			if (missionToolItem.isSelected()) desktop.openToolWindow(MissionWindow.NAME);
			else desktop.closeToolWindow(MissionWindow.NAME);
		}

		if (selectedItem == settlementToolItem) {
			if (settlementToolItem.isSelected()) desktop.openToolWindow(SettlementWindow.NAME);
			else desktop.closeToolWindow(SettlementWindow.NAME);
		}

		if (selectedItem == scienceToolItem) {
			if (scienceToolItem.isSelected()) desktop.openToolWindow(ScienceWindow.NAME);
			else desktop.closeToolWindow(ScienceWindow.NAME);
		}

		if (selectedItem == resupplyToolItem) {
			if (resupplyToolItem.isSelected()) desktop.openToolWindow(ResupplyWindow.NAME);
			else desktop.closeToolWindow(ResupplyWindow.NAME);
		}
		if (selectedItem == showUnitBarItem) {
			desktop.getMainWindow().getUnitToolBar().setVisible(showUnitBarItem.isSelected());}

		if (selectedItem == showToolBarItem) {
			desktop.getMainWindow().getToolToolBar().setVisible(showToolBarItem.isSelected());}

		if (selectedItem == muteItem) {
			desktop.getSoundPlayer().setMute(muteItem.isSelected());}

		if (selectedItem == aboutMspItem) {
			desktop.openToolWindow(GuideWindow.NAME);
			GuideWindow ourGuide;
			ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
			ourGuide.setURL(Msg.getString("doc.about")); //$NON-NLS-1$
		}

		if (selectedItem == guideItem) {
			desktop.openToolWindow(GuideWindow.NAME);
			GuideWindow ourGuide;
			ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
			ourGuide.setURL(Msg.getString("doc.guide")); //$NON-NLS-1$
		}

		if (selectedItem == tutorialItem) {
			desktop.openToolWindow(GuideWindow.NAME);
			GuideWindow ourGuide;
			ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
			ourGuide.setURL(Msg.getString("doc.tutorial")); //$NON-NLS-1$
		}
	}

	/** MenuListener method overriding. */
	@Override
	public final void menuSelected(MenuEvent event) {
		MainDesktopPane desktop = mainWindow.getDesktop();
		marsNavigatorItem.setSelected(desktop.isToolWindowOpen(NavigatorWindow.NAME));
		searchToolItem.setSelected(desktop.isToolWindowOpen(SearchWindow.NAME));
		timeToolItem.setSelected(desktop.isToolWindowOpen(TimeWindow.NAME));
		monitorToolItem.setSelected(desktop.isToolWindowOpen(MonitorWindow.NAME));
		missionToolItem.setSelected(desktop.isToolWindowOpen(MissionWindow.NAME));
		settlementToolItem.setSelected(desktop.isToolWindowOpen(SettlementWindow.NAME));
		scienceToolItem.setSelected(desktop.isToolWindowOpen(ScienceWindow.NAME));
		resupplyToolItem.setSelected(desktop.isToolWindowOpen(ResupplyWindow.NAME));
		showUnitBarItem.setSelected(desktop.getMainWindow().getUnitToolBar().isVisible());
		showToolBarItem.setSelected(desktop.getMainWindow().getToolToolBar().isVisible());
		muteItem.setSelected(desktop.getSoundPlayer().isMute());
	}

	public void menuCanceled(MenuEvent event) {}
	public void menuDeselected(MenuEvent event) {}
}