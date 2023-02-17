/*
 * Mars Simulation Project
 * MainWindowMenu.java
 * @date 2021-08-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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

	private static final String EXIT = "exit";
	private static final String SAVE = "save";
	private static final String SAVE_AS = "saveAs";
	private static final String TOOL = "tool_";
	private static final String OPEN_GUIDE = "guide";
	private static final String TUTORIAL = "tutorial";
	private static final String ABOUT = "about";
	private static final String LAF = "laf";
	private static final String UNIT_TOOLBAR = "unitbar";
	private static final String TOOL_TOOLBAR = "toolbar";
	private static final String EFFECT_UP = "effectUp";
	private static final String EFFECT_DOWN = "effectdown";
	private static final String EFFECT_MUTE = "effectmute";
	private static final String MUSIC_UP = "musicup";
	private static final String MUSIC_DOWN = "musicdown";
	private static final String MUSIC_MUTE = "musicmute";

	// Data members
	/** The main window frame. */
	private MainWindow mainWindow;
	/** The audio player instance. */
	private AudioPlayer soundPlayer;
	/** Unit Bar menu item. */
	private JCheckBoxMenuItem showUnitBarItem;
	/** Tool Bar menu item. */
	private JCheckBoxMenuItem showToolBarItem;

	// Notification Menu
	private NotificationMenu notificationMenu;

	/** Music mute menu item. */
	private JCheckBoxMenuItem musicMuteItem;
	/** Sound Effect mute menu item. */
	private JCheckBoxMenuItem effectMuteItem;
	/** Music volume slider menu item. */
	private JSlider musicVolumeSlider;
	/** Sound effect volume slider menu item. */
	private JSlider effectVolumeSlider;

	private JMenu toolsMenu;

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
		fileMenu.setMnemonic(KeyEvent.VK_F); // Alt + F
		add(fileMenu);

		fileMenu.add(createMenuItem("mainMenu.save", "save", SAVE, null,
									KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK, false)));
		fileMenu.add(createMenuItem("mainMenu.saveAs", "saveAs", SAVE_AS, null,
									KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK, false)));
		fileMenu.add(new JSeparator());
		fileMenu.add(createMenuItem("mainMenu.exit", "exit", EXIT, null,
									KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK, false)));

		// Create tools menu
		toolsMenu = new JMenu(Msg.getString("mainMenu.tools")); //$NON-NLS-1$
		toolsMenu.addMenuListener(this);
		toolsMenu.setMnemonic(KeyEvent.VK_T);
		add(toolsMenu);

		// Create tool menu items
		toolsMenu.add(createCheckMenuItem(NavigatorWindow.NAME, NavigatorWindow.ICON, TOOL, null,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false)));
		toolsMenu.add(createCheckMenuItem(SearchWindow.NAME, SearchWindow.ICON, TOOL, null,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false)));
		toolsMenu.add(createCheckMenuItem(TimeWindow.NAME, TimeWindow.ICON, TOOL, null,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false)));								
		toolsMenu.add(createCheckMenuItem(MonitorWindow.TITLE, MonitorWindow.ICON, TOOL, null,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false)));	
		toolsMenu.add(createCheckMenuItem(MissionWindow.NAME, MissionWindow.ICON, TOOL, null,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false)));	
		toolsMenu.add(createCheckMenuItem(SettlementWindow.NAME, SettlementWindow.ICON, TOOL, null,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0, false)));	
		toolsMenu.add(createCheckMenuItem(ScienceWindow.NAME, ScienceWindow.ICON, TOOL, null,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false)));	
		toolsMenu.add(createCheckMenuItem(ResupplyWindow.NAME, ResupplyWindow.ICON, TOOL, null,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0, false)));	
		toolsMenu.add(createCheckMenuItem(CommanderWindow.NAME, CommanderWindow.ICON, TOOL, null,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false)));	

		// Create settings menu
		JMenu settingsMenu = new JMenu(Msg.getString("mainMenu.settings")); //$NON-NLS-1$
		settingsMenu.setMnemonic(KeyEvent.VK_S); // Alt + S
		settingsMenu.addMenuListener(this);
		add(settingsMenu);

		// Create Show Unit Bar menu item
		showUnitBarItem = createCheckMenuItem(Msg.getString("mainMenu.unitbar"), null,
											  UNIT_TOOLBAR, "mainMenu.tooltip.unitbar"	,
										      KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK, false));
		settingsMenu.add(showUnitBarItem);
		showToolBarItem = createCheckMenuItem(Msg.getString("mainMenu.toolbar"), null,
											  TOOL_TOOLBAR, "mainMenu.tooltip.toolbar"	,
										      KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK, false));
		settingsMenu.add(showToolBarItem);	
		settingsMenu.add(new JSeparator());

		// Create submenu for LAF
		JMenu lafMenu = new JMenu("Look and Feel");
		settingsMenu.add(lafMenu);
		settingsMenu.add(new JSeparator());
		for( String i : StyleManager.getAvailableLAF()) {
			JCheckBoxMenuItem lafItem = new JCheckBoxMenuItem(i);
			if (i.equals(StyleManager.getLAF()))
				lafItem.setSelected(true);
			lafItem.setActionCommand(LAF);
			lafItem.addActionListener(this);
			lafMenu.add(lafItem);
		}

		// Create Background Music Volume slider menu item
		soundPlayer = desktop.getSoundPlayer();
		if (soundPlayer != null) {
			double volume = soundPlayer.getMusicVolume();
			int intVolume = (int) Math.round(volume * 10.0);

			// Create Background Music Volume Slider
			musicVolumeSlider = new JSliderMW(JSlider.HORIZONTAL, 0, 10, intVolume); // $NON-NLS-1$
			musicVolumeSlider.setMajorTickSpacing(1);
			musicVolumeSlider.setPaintTicks(true);
			musicVolumeSlider.setPaintLabels(true);
			musicVolumeSlider.setPaintTrack(true);
			musicVolumeSlider.setSnapToTicks(true);
			musicVolumeSlider.setToolTipText(Msg.getString("mainMenu.tooltip.musicVolumeSlider")); //$NON-NLS-1$
			musicVolumeSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					float newVolume = (float) musicVolumeSlider.getValue() / 10F;
					soundPlayer.setMusicVolume(newVolume);
				}
			});
			settingsMenu.add(musicVolumeSlider);

			settingsMenu.add(createMenuItem("mainMenu.musicVolumeUp", null,
							MUSIC_UP, "mainMenu.musicVolumeUp",
							KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_DOWN_MASK, false)));
			settingsMenu.add(createMenuItem("mainMenu.musicVolumeDown", null,
							MUSIC_DOWN, "mainMenu.musicVolumeDown",
							KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK, false)));
			musicMuteItem = createCheckMenuItem(Msg.getString("mainMenu.muteMusic"), null,
												MUSIC_MUTE, "mainMenu.muteMusic",
												KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK, false));
			settingsMenu.add(musicMuteItem);

			// Create Sound Effect Volume Slider
			effectVolumeSlider = new JSliderMW(JSlider.HORIZONTAL, 0, 10, intVolume); // $NON-NLS-1$
			effectVolumeSlider.setMajorTickSpacing(1);
			effectVolumeSlider.setPaintTicks(true);
			effectVolumeSlider.setPaintLabels(true);
			effectVolumeSlider.setPaintTrack(true);
			effectVolumeSlider.setSnapToTicks(true);

			effectVolumeSlider.setToolTipText(Msg.getString("mainMenu.tooltip.effectVolumeSlider")); //$NON-NLS-1$
			effectVolumeSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					float newVolume = (float) effectVolumeSlider.getValue() / 10F;
					soundPlayer.setSoundVolume(newVolume);
				}
			});
			settingsMenu.add(effectVolumeSlider);

			settingsMenu.add(createMenuItem("mainMenu.effectVolumeUp", null,
											EFFECT_UP, "mainMenu.effectVolumeUp",
											KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.CTRL_DOWN_MASK, false)));
			settingsMenu.add(createMenuItem("mainMenu.effectVolumeDown", null,
											EFFECT_DOWN, "mainMenu.effectVolumeDown",
											KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.CTRL_DOWN_MASK, false)));
			effectMuteItem = createCheckMenuItem(Msg.getString("mainMenu.muteEffect"), null,
												EFFECT_MUTE, "mainMenu.muteEffect",
												KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK, false));
			settingsMenu.add(effectMuteItem);
		}

		// Add notificationMenu
		notificationMenu = new NotificationMenu(this);

		// Create help menu
		JMenu helpMenu = new JMenu(Msg.getString("mainMenu.help")); //$NON-NLS-1$
		helpMenu.setMnemonic(KeyEvent.VK_H); // Alt + H
		helpMenu.addMenuListener(this);
		add(helpMenu);

		// Create About Mars Simulation Project menu item
		helpMenu.add(createMenuItem("mainMenu.about", null, ABOUT, null,
										KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK, false)));
		helpMenu.add(new JSeparator());
		helpMenu.add(createMenuItem("mainMenu.tutorial", null, TUTORIAL, null,
										KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK, false)));
		helpMenu.add(createMenuItem("mainMenu.guide", GuideWindow.ICON, OPEN_GUIDE, null,
										KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK, false)));
	}

	private JMenuItem createMenuItem(String nameKey, String iconName, String command, String tooltipKey, KeyStroke keyStroke) {
		JMenuItem item = new JMenuItem(Msg.getString(nameKey)); //$NON-NLS-1$
		actionMenuItem(item, iconName, command, tooltipKey, keyStroke);
		return item;
	}

	private JCheckBoxMenuItem createCheckMenuItem(String name, String iconName, String command, String tooltipKey,
										KeyStroke keyStroke) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
		actionMenuItem(item, iconName, command, tooltipKey, keyStroke);
		return item;
	}

	private void actionMenuItem(JMenuItem item, String iconName, String command, String tooltipKey,
								KeyStroke keyStroke) {
		if (iconName != null) {
			Icon icon = ImageLoader.getIconByName(iconName);
			item.setIcon(icon);
		}
		if (tooltipKey != null) {
			item.setToolTipText(Msg.getString(tooltipKey)); //$NON-NLS-1$
		}

		item.addActionListener(this);
		item.setActionCommand(command);
		item.setAccelerator(keyStroke);
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
		String command = event.getActionCommand();
		String newGuideURL = null;

		switch(command) {
			case EXIT:
				mainWindow.exitSimulation();
				break;
			case SAVE:
				mainWindow.saveSimulation(true);
				break;
			case SAVE_AS:
				mainWindow.saveSimulation(false);
				break;
			case TOOL: {
				String tool = selectedItem.getText();
				if (selectedItem.isSelected())
					desktop.openToolWindow(tool);
				else
					desktop.closeToolWindow(tool);
			} break;
			case UNIT_TOOLBAR:
				desktop.getMainWindow().getUnitToolBar().setVisible(selectedItem.isSelected());
				break;
			case TOOL_TOOLBAR:
				desktop.getMainWindow().getToolToolBar().setVisible(selectedItem.isSelected());
				break;
			case MUSIC_UP: {
				int newVolume = musicVolumeSlider.getValue() + 1;
				if (newVolume <= 10) {
					soundPlayer.musicVolumeUp();
					musicVolumeSlider.setValue(newVolume);
				}
				else if (newVolume <= 0)
					musicMuteItem.setSelected(true);
				} break;
			case MUSIC_DOWN: {
				int newVolume = musicVolumeSlider.getValue() - 1;
				if (newVolume >= 0) {
					soundPlayer.musicVolumeDown();
					musicVolumeSlider.setValue(newVolume);
				}
				else
					musicMuteItem.setSelected(true);
				} break;
			case EFFECT_UP: {
				int newVolume = effectVolumeSlider.getValue() + 1;
				if (newVolume <= 10) {
					soundPlayer.soundVolumeUp();
					effectVolumeSlider.setValue(newVolume);
				}
			} break;
			case EFFECT_DOWN:{
				int newVolume = effectVolumeSlider.getValue() - 1;
				if (newVolume >= 0) {
					soundPlayer.soundVolumeDown();
					effectVolumeSlider.setValue(newVolume);
				}
			} break;
			case MUSIC_MUTE:
				if (selectedItem.isSelected()) {
					// mute the music
					soundPlayer.muteMusic();
					musicVolumeSlider.setEnabled(false);
					musicMuteItem.revalidate();
					musicMuteItem.repaint();
				}
				else {
					// unmute the music
					soundPlayer.unmuteMusic();
					musicVolumeSlider.setEnabled(true);
					musicMuteItem.revalidate();
					musicMuteItem.repaint();
				}
				break;
			case EFFECT_MUTE:
				if (selectedItem.isSelected()) {
					// mute the sound effect
					soundPlayer.muteSoundEffect();
					effectVolumeSlider.setEnabled(false);
				} else {
					// unmute the sound effect
					soundPlayer.unmuteSoundEffect();
					effectVolumeSlider.setEnabled(true);
				}
				break;
			case ABOUT:
				newGuideURL = Msg.getString("doc.about"); //$NON-NLS-1$
				break;
			case OPEN_GUIDE:
				newGuideURL = Msg.getString("doc.guide"); //$NON-NLS-1$
				break;
			case TUTORIAL:
				newGuideURL = Msg.getString("doc.tutorial"); //$NON-NLS-1$
				break;
			case LAF: {
				String text = selectedItem.getText();
				mainWindow.updateLAF(text);

				// Passive approach
				//StyleManager.setLAF(text);
				//JOptionPane.showMessageDialog(mainWindow.getFrame(), "Restart for Look & Feel to be fully applied.");				
			} break;
			default:
				// Unknown command
				System.err.println("Unknown menu commadn " + command);
		}

		if (newGuideURL != null) {
			desktop.openToolWindow(GuideWindow.NAME);
			GuideWindow ourGuide = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
			ourGuide.setURLString(newGuideURL); //$NON-NLS-1$
		}
	}

	/** MenuListener method overriding. */
	@Override
	public final void menuSelected(MenuEvent event) {
		MainDesktopPane desktop = mainWindow.getDesktop();
		for(Component c : toolsMenu.getComponents()) {
			if (c instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem jc = (JCheckBoxMenuItem) c;
				jc.setSelected(desktop.isToolWindowOpen(jc.getText()));
			}
		}
	
		showUnitBarItem.setSelected(desktop.getMainWindow().getUnitToolBar().isVisible());
		showToolBarItem.setSelected(desktop.getMainWindow().getToolToolBar().isVisible());

		if (soundPlayer != null) {
			musicVolumeSlider.setValue((int) Math.round(soundPlayer.getMusicVolume() * 10));
			musicVolumeSlider.setEnabled(!AudioPlayer.isMusicMute());

			effectVolumeSlider.setValue((int) Math.round(soundPlayer.getEffectVolume() * 10));
			effectVolumeSlider.setEnabled(!AudioPlayer.isEffectMute());

			musicMuteItem.setSelected(AudioPlayer.isMusicMute());
			effectMuteItem.setSelected(AudioPlayer.isEffectMute());
		}
	}

	@Override
	public void menuCanceled(MenuEvent event) {
	}

	@Override
	public void menuDeselected(MenuEvent event) {
	}
}
