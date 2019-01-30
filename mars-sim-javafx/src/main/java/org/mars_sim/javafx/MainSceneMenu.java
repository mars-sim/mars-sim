/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.1.0 2017-02-03
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.javafx;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.input.Input;

import de.codecentric.centerdevice.MenuToolkit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class MainSceneMenu extends MenuBar {

//	private static Logger logger = Logger.getLogger(MainSceneMenu.class.getName());

	private boolean isFullScreenCache = false;

	private CheckMenuItem searchToolItem;
	private CheckMenuItem timeToolItem;
	private CheckMenuItem marsNavigatorItem;
	private CheckMenuItem settlementMapToolItem;
	private CheckMenuItem monitorToolItem;
	private CheckMenuItem missionToolItem;
	private CheckMenuItem scienceToolItem;
	private CheckMenuItem resupplyToolItem;
	private CheckMenuItem guideToolItem;
	private CheckMenuItem showFullScreenItem;
	private CheckMenuItem notificationItem;
	private CheckMenuItem effectMuteItem;
	private CheckMenuItem musicMuteItem;

	private MenuItem quotationItem;
	private MenuItem musicVolumeUpItem;
	private MenuItem musicVolumeDownItem;
	private MenuItem effectVolumeUpItem;
	private MenuItem effectVolumeDownItem;

	private Stage stage;
	private MainScene mainScene;
	private MainDesktopPane desktop;

	/**
	 * Constructor.
	 * 
	 * @param mainWindow the main window pane
	 * @param desktop    our main frame
	 */
	public MainSceneMenu(MainScene mainScene, MainDesktopPane desktop) {
		super();
		// logger.info("MainSceneMenu's constructor is on " +
		// Thread.currentThread().getName() + " Thread");

		this.mainScene = mainScene;
		this.desktop = desktop;
		// Puts the background task of responding to the pull down menu in a thread pool
		//Simulation.instance().getSimExecutor().submit(new CreateMenuTask());
		 
		createGUI();
	}

//	class CreateMenuTask implements Runnable {
//		public void run() {
//			createGUI();
//		}
//	}

	public void createGUI() {
		// logger.info("MainSceneMenu's CreateGUI() is on " +
		// Thread.currentThread().getName() + " Thread");
		this.stage = mainScene.getStage();

		// --- Menu File
		Menu menuFile = new Menu("File");
		// MenuItem newItem = new MenuItem("New...");
		// newItem.setAccelerator(new KeyCodeCombination(KeyCode.N,
		// KeyCombination.CONTROL_DOWN));
		// MenuItem openItem = new MenuItem("Open...");
		// openItem.setAccelerator(new KeyCodeCombination(KeyCode.O,
		// KeyCombination.CONTROL_DOWN));
		// MenuItem openAutoSaveItem = new MenuItem("Open autosave");
		// openAutoSaveItem.setAccelerator(new KeyCodeCombination(KeyCode.U,
		// KeyCombination.CONTROL_DOWN));//, KeyCombination.SHIFT_DOWN));
		MenuItem saveItem = new MenuItem("Save");
		saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		MenuItem saveAsItem = new MenuItem("Save As...");
		saveAsItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
		MenuItem exitItem = new MenuItem("Exit");
		exitItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
		SeparatorMenuItem SeparatorMenuItem1 = new SeparatorMenuItem();
		SeparatorMenuItem SeparatorMenuItem2 = new SeparatorMenuItem();
		SeparatorMenuItem SeparatorMenuItem3 = new SeparatorMenuItem();

		menuFile.getItems().addAll(SeparatorMenuItem1, // newItem, openItem, openAutoSaveItem,
				SeparatorMenuItem2, saveItem, saveAsItem, SeparatorMenuItem3, exitItem);

		// --- Menu Tools
		Menu menuTools = new Menu("Tools");

		marsNavigatorItem = createMenuItem("Mars Navigator Minimap", NavigatorWindow.NAME);
		// marsNavigatorItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));

		searchToolItem = createMenuItem("Search Tool", SearchWindow.NAME);
		// searchToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F2));

		timeToolItem = createMenuItem("Time Tool", TimeWindow.NAME);
		// timeToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F3));

		settlementMapToolItem = createMenuItem("Settlement Map", SettlementWindow.NAME);
		// settlementMapToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F4));

		monitorToolItem = createMenuItem("Monitor Tool", MonitorWindow.NAME);
		// monitorToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F4));

		missionToolItem = createMenuItem("Mission Tool", MissionWindow.NAME);
		// missionToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));

		scienceToolItem = createMenuItem("Science Tool", ScienceWindow.NAME);
		// scienceToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F6));

		resupplyToolItem = createMenuItem("Resupply Tool", ResupplyWindow.NAME);
		// resupplyToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F7));

		guideToolItem = createMenuItem("Help Browser", GuideWindow.NAME);

		// marsViewerItem = createMenuItem("Mars Viewer", MarsViewer.NAME);
		// marsViewerItem.setAccelerator(new KeyCodeCombination(KeyCode.F9));

		// webToolItem = new CheckMenuItem("Web Tool");
		// webToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F10));

		menuTools.getItems().addAll(searchToolItem, timeToolItem, marsNavigatorItem, settlementMapToolItem,
				monitorToolItem, missionToolItem, scienceToolItem, resupplyToolItem, guideToolItem);
		// , marsViewerItem, webToolItem);

		// --- Menu Settings
		Menu menuSettings = new Menu("Settings");

		showFullScreenItem = new CheckMenuItem("Full Screen Mode");
		showFullScreenItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
		showFullScreenItem.setSelected(false);

		// skinThemeItem = new MenuItem("Skin Theme");
		// skinThemeItem.setAccelerator(new KeyCodeCombination(KeyCode.T,
		// KeyCombination.CONTROL_DOWN));
		// skinThemeItem.setSelected(false);

		Menu skinThemeItem = new Menu("Skin Theme");
		ToggleGroup skinThemeToggleGroup = new ToggleGroup();

//        RadioMenuItem oneItem = new RadioMenuItem("Olive");
//        oneItem.setToggleGroup(skinThemeToggleGroup);
//        //oneItem.setSelected(true);
//        RadioMenuItem twoItem = new RadioMenuItem("Burgundy");
//        twoItem.setToggleGroup(skinThemeToggleGroup);
//        RadioMenuItem threeItem = new RadioMenuItem("DarkTabaco");
//        threeItem.setToggleGroup(skinThemeToggleGroup);
//        RadioMenuItem fourItem = new RadioMenuItem("Grey");
//        fourItem.setToggleGroup(skinThemeToggleGroup);
//        RadioMenuItem fiveItem = new RadioMenuItem("Violet");
//        fiveItem.setToggleGroup(skinThemeToggleGroup);

		RadioMenuItem sixItem = new RadioMenuItem("Snow Blue");
		sixItem.setToggleGroup(skinThemeToggleGroup);

		RadioMenuItem sevenItem = new RadioMenuItem("Mud Orange");
		sevenItem.setToggleGroup(skinThemeToggleGroup);

		if (MainScene.OS.contains("linux")) {
			sixItem.setSelected(true);
			skinThemeItem.getItems().addAll(sixItem);
		} else {
			sevenItem.setSelected(true);
			skinThemeItem.getItems().addAll(sixItem, sevenItem);
		}

		// CheckMenuItem showUnitBarItem = new CheckMenuItem("Show Unit Bar");
		// showUnitBarItem.setAccelerator(new KeyCodeCombination(KeyCode.U,
		// KeyCombination.CONTROL_DOWN));
		// CheckMenuItem showToolBarItem = new CheckMenuItem("Show Tool Bar");
		// showToolBarItem.setAccelerator(new KeyCodeCombination(KeyCode.B,
		// KeyCombination.CONTROL_DOWN));

		SeparatorMenuItem SeparatorMenuItem4 = new SeparatorMenuItem();

		quotationItem = new MenuItem("Quotation");
		quotationItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
		// quotationItem.setSelected(true);

		SeparatorMenuItem SeparatorMenuItem5 = new SeparatorMenuItem();

		notificationItem = new CheckMenuItem("Notifications");
		notificationItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		notificationItem.setSelected(true);

		SeparatorMenuItem SeparatorMenuItem6 = new SeparatorMenuItem();

		musicVolumeUpItem = new MenuItem("Music Volume Up");
		musicVolumeUpItem.setAccelerator(new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN));
		musicVolumeDownItem = new MenuItem("Music Volume Down");
		musicVolumeDownItem.setAccelerator(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN));

		effectVolumeUpItem = new MenuItem("Sound Effect Volume Up");
		effectVolumeUpItem.setAccelerator(new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN));
		effectVolumeDownItem = new MenuItem("Sound Effect Volume Down");
		effectVolumeDownItem.setAccelerator(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN));

		musicMuteItem = new CheckMenuItem("Mute Background Music");
		musicMuteItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
		effectMuteItem = new CheckMenuItem("Mute Sound Effect");
		effectMuteItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));

		menuSettings.getItems().addAll(showFullScreenItem, skinThemeItem, SeparatorMenuItem4, quotationItem,
				SeparatorMenuItem5, notificationItem, SeparatorMenuItem6, musicVolumeUpItem, musicVolumeDownItem,
				effectVolumeUpItem, effectVolumeDownItem, musicMuteItem, effectMuteItem); // showUnitBarItem,showToolBarItem,

		// --- Menu Help
//        Menu menuHelp = new Menu("Help");
//        MenuItem aboutItem = new MenuItem("About");
//        MenuItem tutorialItem = new MenuItem("Tutorial");
//        tutorialItem.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN));
//        SeparatorMenuItem SeparatorMenuItem7 = new SeparatorMenuItem();
//        MenuItem userGuideItem = new MenuItem("User Guide");
//        userGuideItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));
//
//        menuHelp.getItems().addAll(aboutItem, tutorialItem,SeparatorMenuItem7, userGuideItem);

		Platform.runLater(() -> {

			// Add nsmenufx.
			if (MainScene.OS.contains("mac")) {

				MenuToolkit tk = MenuToolkit.toolkit(Locale.getDefault());
				tk.setApplicationMenu(tk.createDefaultApplicationMenu("mars-sim"));

				MenuBar menuBar = (MenuBar) this;
				// Add the default application menu
				// getMenus().add(tk.createDefaultApplicationMenu("mars-sim"));
				// Create default application menu with app name "test"
				// Menu defaultApplicationMenu = tk.createDefaultApplicationMenu("test");
				// Replace the autogenerated application menu
				// tk.setApplicationMenu(defaultApplicationMenu);
				// Since we now have a reference to the menu, we can rename items
				// defaultApplicationMenu.getItems().get(1).setText("Hide all the otters");
				tk.setApplicationMenu(tk.createDefaultApplicationMenu("mars-sims"));
				// Use the menu bar for all stages including new ones
				// tk.setGlobalMenuBar(bar);

				Menu java = new Menu("Jave");
				MenuItem quit = tk.createQuitMenuItem("mars-sim");
				java.getItems().addAll(quit);

				menuBar.getMenus().addAll(java, menuFile, menuTools, menuSettings);// , menuHelp); // menuNotification,

				tk.setMenuBar(stage, menuBar);

			} else
				super.getMenus().addAll(menuFile, menuTools, menuSettings);// , menuHelp); // menuNotification,

		});

//        newItem.setOnAction(new EventHandler<ActionEvent>() {
//     	   @Override
//     	   public void handle(ActionEvent e) {
//     		   mainScene.newSimulation();
//     	   }
//     	});
//
//        openItem.setOnAction(new EventHandler<ActionEvent>() {
//     	   @Override
//     	   public void handle(ActionEvent e) {
//     		   mainScene.loadSimulation(MainScene.OTHER);
//     	   }
//     	});
//
//        openAutoSaveItem.setOnAction(new EventHandler<ActionEvent>() {
//     	   @Override
//     	   public void handle(ActionEvent e) {
//     		   mainScene.loadSimulation(MainScene.AUTOSAVE);
//     	   }
//     	});

		exitItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Input input = FXGL.getInput();
				input.mockKeyPress(KeyCode.ESCAPE);
				input.mockKeyRelease(KeyCode.ESCAPE);
				// mainScene.dialogOnExit();
				// e.consume();
			}
		});

		saveItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				mainScene.saveSimulation(Simulation.SAVE_DEFAULT);
			}
		});

		saveAsItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				mainScene.saveSimulation(Simulation.SAVE_AS);
			}
		});

		showFullScreenItem.setOnAction(e -> {

			boolean isFullScreen = mainScene.getStage().isFullScreen();
			if (!isFullScreen) {
				// mainScene.getStage().sizeToScene();
				// System.out.println("isFullScreen is false");
				showFullScreenItem.setSelected(true);
				if (!isFullScreenCache)
					mainScene.getStage().setFullScreen(true);
				// TODO: move quotation and pause popup to front.
				// mainScene.getStage().toBack();
			} else {
				// System.out.println("isFullScreen is true");
				showFullScreenItem.setSelected(false);
				if (isFullScreenCache)
					mainScene.getStage().setFullScreen(false);
			}
			isFullScreenCache = mainScene.getStage().isFullScreen();
		});

		sixItem.setOnAction(e -> {
			mainScene.setTheme(6);
			// SwingUtilities.invokeLater(() -> {
			// mainScene.getSwingNode().setContent(desktop);
			// });
		});

		if (!MainScene.OS.contains("linux")) {
			sevenItem.setOnAction(e -> {
				mainScene.setTheme(7);
				// SwingUtilities.invokeLater(() -> {
				// mainScene.getSwingNode().setContent(desktop);
				// });
			});
		}

		quotationItem.setOnAction(e -> {
			mainScene.popAQuote();
			desktop.requestFocus();
		});

		notificationItem.setOnAction(e -> {

			boolean isNotificationOn = !desktop.getEventTableModel().isNoFiring();

			if (isNotificationOn) {
				// System.out.println("turn off notification");
				notificationItem.setSelected(false);
				// if (isNotificationOnCache)
				desktop.getEventTableModel().setNoFiring(true);
			} else {
				// System.out.println("turn on notification");
				notificationItem.setSelected(true);
				// if (!isNotificationOnCache)
				desktop.getEventTableModel().setNoFiring(false);
			}

			// isNotificationOnCache = isNotificationOn;

		});

		musicVolumeUpItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// float oldvolume = desktop.getSoundPlayer().getVolume();
				// desktop.getSoundPlayer().setVolume(oldvolume+0.1F);
				desktop.getSoundPlayer().musicVolumeUp();
			}
		});

		musicVolumeDownItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// float oldvolume = desktop.getSoundPlayer().getVolume();
				// desktop.getSoundPlayer().setVolume(oldvolume-0.1F);
				desktop.getSoundPlayer().musicVolumeDown();
			}
		});

		effectVolumeUpItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// float oldvolume = desktop.getSoundPlayer().getVolume();
				// desktop.getSoundPlayer().setVolume(oldvolume+0.1F);
				desktop.getSoundPlayer().soundVolumeUp();
			}
		});

		effectVolumeDownItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// float oldvolume = desktop.getSoundPlayer().getVolume();
				// desktop.getSoundPlayer().setVolume(oldvolume-0.1F);
				desktop.getSoundPlayer().soundVolumeDown();
			}
		});

		musicMuteItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (musicMuteItem.isSelected())
					mainScene.muteControls(false, true);// desktop.getSoundPlayer().pause(false, true);
				else
					mainScene.unmuteControls(false, true);// desktop.getSoundPlayer().restore(false, true);
			}
		});

		effectMuteItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (effectMuteItem.isSelected())
					mainScene.muteControls(true, false);// desktop.getSoundPlayer().pause(true, false);
				else
					mainScene.unmuteControls(true, false);// desktop.getSoundPlayer().restore(true, false);
			}
		});

//        aboutItem.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent arg0) {
//    			desktop.openToolWindow(GuideWindow.NAME);
//    			GuideWindow ourGuide;
//    			ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
//    			ourGuide.setURL(Msg.getString("doc.about")); //$NON-NLS-1$
//    		}
//        });
//
//        tutorialItem.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent arg0) {
//    			desktop.openToolWindow(GuideWindow.NAME);
//    			GuideWindow ourGuide;
//    			ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
//    			ourGuide.setURL(Msg.getString("doc.tutorial")); //$NON-NLS-1$
//    		}
//        });
//
//        userGuideItem.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent arg0) {
//    			desktop.openToolWindow(GuideWindow.NAME);
//    			GuideWindow ourGuide;
//    			ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
//    			ourGuide.setURL(Msg.getString("doc.guide")); //$NON-NLS-1$
//    		}
//        });

	}

	// Toggle the full screen mode off
	public void updateFullScreenMode() {
		showFullScreenItem.setSelected(false);
	}

	public CheckMenuItem getMarsNavigatorItem() {
		return marsNavigatorItem;
	}

	public CheckMenuItem getSearchToolItem() {
		return searchToolItem;
	}

	public CheckMenuItem getTimeToolItem() {
		return timeToolItem;
	}

	public CheckMenuItem getSettlementMapToolItem() {
		return settlementMapToolItem;
	}

	public CheckMenuItem getMonitorToolItem() {
		return monitorToolItem;
	}

	public CheckMenuItem getMissionToolItem() {
		return missionToolItem;
	}

	public CheckMenuItem getScienceToolItem() {
		return scienceToolItem;
	}

	public CheckMenuItem getResupplyToolItem() {
		return resupplyToolItem;
	}

	public CheckMenuItem getHelpBrowserItem() {
		return guideToolItem;
	}

	// public CheckMenuItem getMarsViewerItem() {
	// return marsViewerItem;
	// }

	/**
	 * Create menu items
	 * 
	 * @param title
	 * @param toolName
	 * @return CheckMenuItem
	 */
	private CheckMenuItem createMenuItem(String title, String toolName) {
		CheckMenuItem cmi = new CheckMenuItem(title);
		cmi.setSelected(false);

		cmi.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
				if (new_val) {
					long SLEEP_TIME = 100;
					cmi.setSelected(true);
					Platform.runLater(() -> {
						// mainScene.openSwingTab();
					});
					try {
						TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					SwingUtilities.invokeLater(() -> {
						desktop.openToolWindow(toolName);
						// desktop.repaint();
					});
					// desktop.repaint();
				} else {
					cmi.setSelected(false);
					SwingUtilities.invokeLater(() -> {
						desktop.closeToolWindow(toolName);
						// desktop.repaint();
					});
				}
			}
		});

		return cmi;
	}

	/**
	 * Uncheck Too lWindow
	 * 
	 * @param toolName
	 */
	public void uncheckToolWindow(String toolName) {

		if (toolName.equals(SearchWindow.NAME)) {
			getSearchToolItem().setSelected(false);
		}

		else if (toolName.equals(TimeWindow.NAME)) {
			getTimeToolItem().setSelected(false);
		}

		else if (toolName.equals(SettlementWindow.NAME)) {
			getSettlementMapToolItem().setSelected(false);
		}

		else if (toolName.equals(NavigatorWindow.NAME)) {
			getMarsNavigatorItem().setSelected(false);
		}

		else if (toolName.equals(MonitorWindow.NAME)) {
			getMonitorToolItem().setSelected(false);
			// System.out.println(toolName + " is unchecked");
		}

		else if (toolName.equals(MissionWindow.NAME)) {
			getMissionToolItem().setSelected(false);
		}

		else if (toolName.equals(ScienceWindow.NAME)) {
			getScienceToolItem().setSelected(false);
		}

		else if (toolName.equals(ResupplyWindow.NAME)) {
			getResupplyToolItem().setSelected(false);
		}

		else if (toolName.equals(GuideWindow.NAME)) {
			getHelpBrowserItem().setSelected(false);
		}

		// else if (toolName.equals(MarsViewer.NAME)) {
		// getMarsViewerItem().setSelected(false);
		// }

	}

	public CheckMenuItem getMusicMuteItem() {
		return musicMuteItem;
	}

	public CheckMenuItem getSoundEffectMuteItem() {
		return effectMuteItem;
	}

	public CheckMenuItem getNotificationItem() {
		return notificationItem;
	}

	public CheckMenuItem getShowFullScreenItem() {
		return showFullScreenItem;
	}

	public CheckMenuItem getCheckMenuItem(String toolName) {
		if (toolName.equals(SearchWindow.NAME)) {
			return getSearchToolItem();
		}

		else if (toolName.equals(TimeWindow.NAME)) {
			return getTimeToolItem();
		}

		else if (toolName.equals(SettlementWindow.NAME)) {
			return getSettlementMapToolItem();
		}

		else if (toolName.equals(NavigatorWindow.NAME)) {
			// System.out.println("closing nav");
			return getMarsNavigatorItem();
		}

		else if (toolName.equals(MonitorWindow.NAME)) {
			return getMonitorToolItem();
		}

		else if (toolName.equals(MissionWindow.NAME)) {
			return getMissionToolItem();
		}

		else if (toolName.equals(ScienceWindow.NAME)) {
			return getScienceToolItem();
		}

		else if (toolName.equals(ResupplyWindow.NAME)) {
			return getResupplyToolItem();
		}

		else if (toolName.equals(GuideWindow.NAME)) {
			return getHelpBrowserItem();
		}

		// else if (toolName.equals(MarsNode.NAME)) {
		// return getSettlementMapToolItem();
		// }

		// else if (toolName.equals(Browser.NAME)) {
		// return getResupplyToolItem();
		// }

		// else if (toolName.equals(MarsViewer.NAME)) {
		// return getMarsViewerItem();
		// }

		else
			return null;
	}

	public void destroy() {

		searchToolItem = null;
		timeToolItem = null;
		marsNavigatorItem = null;
		settlementMapToolItem = null;
		monitorToolItem = null;
		missionToolItem = null;
		scienceToolItem = null;
		resupplyToolItem = null;
		guideToolItem = null;
		showFullScreenItem = null;
		notificationItem = null;
		effectMuteItem = null;
		musicMuteItem = null;

		quotationItem = null;
		musicVolumeUpItem = null;
		musicVolumeDownItem = null;
		effectVolumeUpItem = null;
		effectVolumeDownItem = null;
	
		stage = null;
		mainScene.destroy();
		mainScene = null;
		desktop.destroy();
		desktop = null;
	}
}