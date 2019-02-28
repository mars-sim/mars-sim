/**
 * Mars Simulation Project
 * MainWindowMenu.java
 * @version 3.1.0 2019-02-28
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
import org.mars_sim.msp.ui.swing.notification.NotificationMenu;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.tool.commander.CommanderWindow;
//import org.mars_sim.msp.ui.swing.tool.MarsViewer;
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
public class MainWindowMenu extends JMenuBar implements ActionListener, MenuListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The main window frame. */
	private MainWindow mainWindow;
	/** New menu item. */
	// private JMenuItem newItem;
	/** Load menu item. */
	private JMenuItem loadItem;
	/** Load Autosave menu item. */
	private JMenuItem loadAutosaveItem;
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
	/** Commander Dashboard menu item. */
	private JCheckBoxMenuItem commanderDashboardItem;
	/** Mars Viewer menu item. */
	// private JCheckBoxMenuItem marsViewerItem;

	/** Unit Bar menu item. */
	private JCheckBoxMenuItem showUnitBarItem;
	/** Tool Bar menu item. */
	private JCheckBoxMenuItem showToolBarItem;

	// Notification Menu
	private NotificationMenu notificationMenu;
	// private MainDesktopPane desktop;

	/** Music mute menu item. */
	private JCheckBoxMenuItem musicMuteItem;
	/** Sound Effect mute menu item. */
	private JCheckBoxMenuItem effectMuteItem;
	/** Volume Up menu item. */
	private JMenuItem musicVolumeUpItem;
	/** Volume Down menu item. */
	private JMenuItem musicVolumeDownItem;
	/** Volume Up menu item. */
	private JMenuItem effectVolumeUpItem;
	/** Volume Down menu item. */
	private JMenuItem effectVolumeDownItem;
	/** Music volume slider menu item. */
	private JSlider musicVolumeItem;
	/** Sound effect volume slider menu item. */
	private JSlider effectVolumeItem;
	/** The Home/About page of the Mars Simulation Project menu item. */
	private JMenuItem homeAboutItem;
	/** Tutorial menu item. */
	private JMenuItem tutorialItem;
	/** User Guide menu item. */
	private JMenuItem guideItem;

	/**
	 * Constructor.
	 * 
	 * @param mainWindow
	 *            the main window pane
	 */
	public MainWindowMenu(MainWindow mainWindow, MainDesktopPane desktop) {

		// Use JMenuBar constructor
		super();

		// Initialize data members
		this.mainWindow = mainWindow;
		// this.desktop = desktop;

		// Create file menu
		JMenu fileMenu = new JMenu(Msg.getString("mainMenu.file")); //$NON-NLS-1$
		fileMenu.setMnemonic(KeyEvent.VK_F);
		add(fileMenu);

		// Create load menu item
		ImageIcon loadicon = new ImageIcon(getClass().getResource(Msg.getString("img.open"))); //$NON-NLS-1$
		loadItem = new JMenuItem(Msg.getString("mainMenu.open"), loadicon); //$NON-NLS-1$
		loadItem.addActionListener(this);
		loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK, false));
		loadItem.setToolTipText(Msg.getString("mainMenu.tooltip.open")); //$NON-NLS-1$
		fileMenu.add(loadItem);

		// Create load autosave menu item
		ImageIcon loadAutosaveicon = new ImageIcon(getClass().getResource(Msg.getString("img.openAutosave"))); //$NON-NLS-1$
		loadAutosaveItem = new JMenuItem(Msg.getString("mainMenu.openAutosave"), loadAutosaveicon); //$NON-NLS-1$
		loadAutosaveItem.addActionListener(this);
		loadAutosaveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK, false));
		loadAutosaveItem.setToolTipText(Msg.getString("mainMenu.tooltip.openAutosave")); //$NON-NLS-1$
		fileMenu.add(loadAutosaveItem);

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
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK, false));
//				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK, false));
		saveAsItem.setToolTipText(Msg.getString("mainMenu.tooltip.saveAs")); //$NON-NLS-1$
		fileMenu.add(saveAsItem);

		fileMenu.add(new JSeparator());

//		add(new JSeparator());
		
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
		toolsMenu.setMnemonic(KeyEvent.VK_T);
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

		// Create commander dashboard menu item
		ImageIcon commandericon = new ImageIcon(getClass().getResource(Msg.getString("img.dashboard"))); //$NON-NLS-1$
		commanderDashboardItem = new JCheckBoxMenuItem(CommanderWindow.NAME, commandericon);
		commanderDashboardItem.addActionListener(this);
		commanderDashboardItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false));
		commanderDashboardItem.setToolTipText(Msg.getString("mainMenu.tooltip.dashboard")); //$NON-NLS-1$
		toolsMenu.add(commanderDashboardItem);	
		
		// Create settings menu
		JMenu settingsMenu = new JMenu(Msg.getString("mainMenu.settings")); //$NON-NLS-1$
		settingsMenu.setMnemonic(KeyEvent.VK_S);
		settingsMenu.addMenuListener(this);
		add(settingsMenu);

		// Create Show Unit Bar menu item
		showUnitBarItem = new JCheckBoxMenuItem(Msg.getString("mainMenu.unitbar")); //$NON-NLS-1$
		showUnitBarItem.addActionListener(this);
		showUnitBarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK, false));
		showUnitBarItem.setToolTipText(Msg.getString("mainMenu.tooltip.unitbar")); //$NON-NLS-1$
		showUnitBarItem.setState(false);
		settingsMenu.add(showUnitBarItem);

		// Create Show Tool Bar menu item
		showToolBarItem = new JCheckBoxMenuItem(Msg.getString("mainMenu.toolbar")); //$NON-NLS-1$
		showToolBarItem.addActionListener(this);
		showToolBarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK, false));
		showToolBarItem.setToolTipText(Msg.getString("mainMenu.tooltip.toolbar")); //$NON-NLS-1$
		settingsMenu.add(showToolBarItem);

		settingsMenu.add(new JSeparator());
		// settingsMenu.addSeparator();

		// Create Volume slider menu item
		// MainDesktopPane desktop = mainWindow.getDesktop();
		final AudioPlayer soundPlayer = desktop.getSoundPlayer();
		double volume = soundPlayer.getMusicVolume();
		int intVolume = (int) Math.round(volume * 10.0);

		musicVolumeItem = new JSliderMW(JSlider.HORIZONTAL, 0, 10, intVolume); // $NON-NLS-1$
		musicVolumeItem.setMajorTickSpacing(1);
		musicVolumeItem.setPaintTicks(true);
		musicVolumeItem.setPaintLabels(true);
		musicVolumeItem.setPaintTrack(true);
		musicVolumeItem.setSnapToTicks(true);

		musicVolumeItem.setToolTipText(Msg.getString("mainMenu.tooltip.musicVolumeslider")); //$NON-NLS-1$
		musicVolumeItem.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float newVolume = (float) musicVolumeItem.getValue() / 10F;
				soundPlayer.setMusicVolume(newVolume);
			}
		});
		settingsMenu.add(musicVolumeItem);

		// Create Volume Up menu item
		musicVolumeUpItem = new JMenuItem(Msg.getString("mainMenu.musicVolumeUp")); //$NON-NLS-1$
		musicVolumeUpItem.addActionListener(this);
		musicVolumeUpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK, false));
		musicVolumeUpItem.setToolTipText(Msg.getString("mainMenu.tooltip.volumeUp")); //$NON-NLS-1$
		settingsMenu.add(musicVolumeUpItem);

		// Create Volume Down menu item
		musicVolumeDownItem = new JMenuItem(Msg.getString("mainMenu.musicVolumeDown")); //$NON-NLS-1$
		musicVolumeDownItem.addActionListener(this);
		musicVolumeDownItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK, false));
		musicVolumeDownItem.setToolTipText(Msg.getString("mainMenu.tooltip.volumeDown")); //$NON-NLS-1$
		settingsMenu.add(musicVolumeDownItem);
		
		effectVolumeItem = new JSliderMW(JSlider.HORIZONTAL, 0, 10, intVolume); // $NON-NLS-1$
		effectVolumeItem.setMajorTickSpacing(1);
		effectVolumeItem.setPaintTicks(true);
		effectVolumeItem.setPaintLabels(true);
		effectVolumeItem.setPaintTrack(true);
		effectVolumeItem.setSnapToTicks(true);

		effectVolumeItem.setToolTipText(Msg.getString("mainMenu.tooltip.effectVolumeSlider")); //$NON-NLS-1$
		effectVolumeItem.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float newVolume = (float) effectVolumeItem.getValue() / 10F;
				soundPlayer.setSoundVolume(newVolume);
			}
		});
		settingsMenu.add(effectVolumeItem);

		// Create Volume Up menu item
		effectVolumeUpItem = new JMenuItem(Msg.getString("mainMenu.effectVolumeUp")); //$NON-NLS-1$
		effectVolumeUpItem.addActionListener(this);
		effectVolumeUpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK, false));
		effectVolumeUpItem.setToolTipText(Msg.getString("mainMenu.tooltip.effectVolumeUp")); //$NON-NLS-1$
		settingsMenu.add(effectVolumeUpItem);

		// Create Volume Down menu item
		effectVolumeDownItem = new JMenuItem(Msg.getString("mainMenu.effectVolumeDown")); //$NON-NLS-1$
		effectVolumeDownItem.addActionListener(this);
		effectVolumeDownItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK, false));
		effectVolumeDownItem.setToolTipText(Msg.getString("mainMenu.tooltip.effectVolumeDown")); //$NON-NLS-1$
		settingsMenu.add(effectVolumeDownItem);

		// Create Mute menu item
		musicMuteItem = new JCheckBoxMenuItem(Msg.getString("mainMenu.mute.music")); //$NON-NLS-1$
		musicMuteItem.addActionListener(this);
		musicMuteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK, false));
		musicMuteItem.setToolTipText(Msg.getString("mainMenu.tooltip.mute.music")); //$NON-NLS-1$
		settingsMenu.add(musicMuteItem);

		effectMuteItem = new JCheckBoxMenuItem(Msg.getString("mainMenu.mute.effect")); //$NON-NLS-1$
		effectMuteItem.addActionListener(this);
		effectMuteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK, false));
		effectMuteItem.setToolTipText(Msg.getString("mainMenu.tooltip.mute.effect")); //$NON-NLS-1$
		settingsMenu.add(effectMuteItem);

		// Add notificationMenu
		notificationMenu = new NotificationMenu(this);

		// Create help menu
		JMenu helpMenu = new JMenu(Msg.getString("mainMenu.help")); //$NON-NLS-1$
		helpMenu.setMnemonic(KeyEvent.VK_H); // alt + H
		helpMenu.addMenuListener(this);
		add(helpMenu);

		// Create About Mars Simulation Project menu item
		homeAboutItem = new JMenuItem(Msg.getString("mainMenu.about")); //$NON-NLS-1$
		homeAboutItem.addActionListener(this);
		homeAboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK, false));
		homeAboutItem.setToolTipText(Msg.getString("mainMenu.tooltip.about")); //$NON-NLS-1$
		helpMenu.add(homeAboutItem);
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

	/**
	 * Gets the Notification Menu.
	 * 
	 * @return notificationMenu
	 */
	public NotificationMenu getNotificationMenu() {
		return notificationMenu;
	}

	/**
	 * Gets the Main Window.
	 * 
	 * @return mainWindow
	 */
	public MainWindow getMainWindow() {
		return mainWindow;
	}

	/** ActionListener method overriding. */
	@Override
	public final void actionPerformed(ActionEvent event) {
		JMenuItem selectedItem = (JMenuItem) event.getSource();
		MainDesktopPane desktop = mainWindow.getDesktop();

		if (selectedItem == exitItem)
			mainWindow.exitSimulation();
		// else if (selectedItem == newItem) mainWindow.newSimulationProcess();
		else if (selectedItem == saveItem)
			mainWindow.saveSimulation(true, false);
		else if (selectedItem == saveAsItem)
			mainWindow.saveSimulation(false, false);
		else if (selectedItem == loadItem)
			mainWindow.loadSimulation(false);
		else if (selectedItem == loadAutosaveItem)
			mainWindow.loadSimulation(true);
		
		else if (selectedItem == marsNavigatorItem) {
			if (marsNavigatorItem.isSelected())
				desktop.openToolWindow(NavigatorWindow.NAME);
			else
				desktop.closeToolWindow(NavigatorWindow.NAME);
		}

		else if (selectedItem == searchToolItem) {
			if (searchToolItem.isSelected())
				desktop.openToolWindow(SearchWindow.NAME);
			else
				desktop.closeToolWindow(SearchWindow.NAME);
		}

		else if (selectedItem == timeToolItem) {
			if (timeToolItem.isSelected())
				desktop.openToolWindow(TimeWindow.NAME);
			else
				desktop.closeToolWindow(TimeWindow.NAME);
		}

		else if (selectedItem == monitorToolItem) {
			if (monitorToolItem.isSelected())
				desktop.openToolWindow(MonitorWindow.NAME);
			else
				desktop.closeToolWindow(MonitorWindow.NAME);
		}

		else if (selectedItem == missionToolItem) {
			if (missionToolItem.isSelected())
				desktop.openToolWindow(MissionWindow.NAME);
			else
				desktop.closeToolWindow(MissionWindow.NAME);
		}

		else if (selectedItem == settlementToolItem) {
			if (settlementToolItem.isSelected())
				desktop.openToolWindow(SettlementWindow.NAME);
			else
				desktop.closeToolWindow(SettlementWindow.NAME);
		}

		else if (selectedItem == scienceToolItem) {
			if (scienceToolItem.isSelected())
				desktop.openToolWindow(ScienceWindow.NAME);
			else
				desktop.closeToolWindow(ScienceWindow.NAME);
		}

		else if (selectedItem == resupplyToolItem) {
			if (resupplyToolItem.isSelected())
				desktop.openToolWindow(ResupplyWindow.NAME);
			else
				desktop.closeToolWindow(ResupplyWindow.NAME);
		}

		else if (selectedItem == commanderDashboardItem) {
			if (commanderDashboardItem.isSelected())
				desktop.openToolWindow(CommanderWindow.NAME);
			else
				desktop.closeToolWindow(CommanderWindow.NAME);
		}
		
		else if (selectedItem == showUnitBarItem) {
			desktop.getMainWindow().getUnitToolBar().setVisible(showUnitBarItem.isSelected());
		}

		else if (selectedItem == showToolBarItem) {
			desktop.getMainWindow().getToolToolBar().setVisible(showToolBarItem.isSelected());
		}

		else if (selectedItem == musicVolumeUpItem) {
			double oldvolume = desktop.getSoundPlayer().getMusicVolume();
			desktop.getSoundPlayer().setMusicVolume(oldvolume + 0.05F);
		}

		else if (selectedItem == musicVolumeDownItem) {
			double oldvolume = desktop.getSoundPlayer().getMusicVolume();
			desktop.getSoundPlayer().setMusicVolume(oldvolume - 0.05F);
		}

		else if (selectedItem == effectVolumeUpItem) {
			double oldvolume = desktop.getSoundPlayer().getEffectVolume();
			desktop.getSoundPlayer().setSoundVolume(oldvolume + 0.05F);
		}

		else if (selectedItem == effectVolumeDownItem) {
			double oldvolume = desktop.getSoundPlayer().getEffectVolume();
			desktop.getSoundPlayer().setSoundVolume(oldvolume - 0.05F);
		}

		else if (selectedItem == musicMuteItem) {
			if (musicMuteItem.isSelected())
				desktop.getSoundPlayer().mutePlayer(false, true);
			else
				desktop.getSoundPlayer().unmutePlayer(false, true);
		}

		else if (selectedItem == effectMuteItem) {
			if (effectMuteItem.isSelected())
				desktop.getSoundPlayer().mutePlayer(true, false);
			else
				desktop.getSoundPlayer().unmutePlayer(true, false);
		}

		else if (selectedItem == homeAboutItem) {
			desktop.openToolWindow(GuideWindow.NAME);
			GuideWindow ourGuide;
			ourGuide = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
			ourGuide.setURL(Msg.getString("doc.about")); //$NON-NLS-1$
		}

		else if (selectedItem == guideItem) {
			desktop.openToolWindow(GuideWindow.NAME);
			GuideWindow ourGuide;
			ourGuide = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
			ourGuide.setURL(Msg.getString("doc.guide")); //$NON-NLS-1$
		}

		else if (selectedItem == tutorialItem) {
			desktop.openToolWindow(GuideWindow.NAME);
			GuideWindow ourGuide;
			ourGuide = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
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
		commanderDashboardItem.setSelected(desktop.isToolWindowOpen(CommanderWindow.NAME));
		
		showUnitBarItem.setSelected(desktop.getMainWindow().getUnitToolBar().isVisible());
		showToolBarItem.setSelected(desktop.getMainWindow().getToolToolBar().isVisible());

		musicVolumeItem.setValue((int) Math.round(desktop.getSoundPlayer().getMusicVolume() * 10));
		musicVolumeItem.setEnabled(!desktop.getSoundPlayer().isMusicMute());
		effectVolumeItem.setValue((int) Math.round(desktop.getSoundPlayer().getEffectVolume() * 10));
		effectVolumeItem.setEnabled(!desktop.getSoundPlayer().isSoundMute());
		musicMuteItem.setSelected(desktop.getSoundPlayer().isMusicMute());
		effectMuteItem.setSelected(desktop.getSoundPlayer().isSoundMute());
	}

	public void menuCanceled(MenuEvent event) {
	}

	public void menuDeselected(MenuEvent event) {
	}
}