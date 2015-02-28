/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-02-25
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

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

import org.mars_sim.msp.core.Msg;
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


public class MainWindowFXMenu extends MenuBar  {

    //private MainDesktopPane desktop;
	private CheckMenuItem showFullScreenItem ;
	
	/** 
	 * Constructor.
	 * @param mainWindow the main window pane
	 * @param desktop our main frame
	 */
	public MainWindowFXMenu(MainScene mainScene, MainDesktopPane desktop) {	
		super();

        // --- Menu File
        Menu menuFile = new Menu("File");
        MenuItem newItem = new MenuItem("New...");
        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        MenuItem openItem = new MenuItem("Open...");
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        MenuItem openAutoSaveItem = new MenuItem("Open autosave");
        openAutoSaveItem.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        MenuItem saveAsItem = new MenuItem("Save As...");
        saveAsItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN , KeyCombination.SHIFT_DOWN));
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        SeparatorMenuItem SeparatorMenuItem1 = new SeparatorMenuItem();
        SeparatorMenuItem SeparatorMenuItem2 = new SeparatorMenuItem();
        SeparatorMenuItem SeparatorMenuItem3 = new SeparatorMenuItem();

        menuFile.getItems().addAll(newItem, SeparatorMenuItem1, openItem, openAutoSaveItem, SeparatorMenuItem2, saveItem, saveAsItem, SeparatorMenuItem3, exitItem);
        
        // --- Menu Tools
        Menu menuTools = new Menu("Tools");
        CheckMenuItem marsNavigatorItem = new CheckMenuItem("Mars Navigator");
        marsNavigatorItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
        CheckMenuItem searchToolItem = new CheckMenuItem("Search Tool");
        searchToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        CheckMenuItem timeToolItem = new CheckMenuItem("Time Tool");
        timeToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F3));
        CheckMenuItem monitorToolItem = new CheckMenuItem("Monitor Tool");
        monitorToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F4));
        CheckMenuItem missionToolItem = new CheckMenuItem("Mission Tool");
        missionToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));
        CheckMenuItem settlementMapToolItem = new CheckMenuItem("Settlement Map Tool");
        settlementMapToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F6));
        CheckMenuItem scienceToolItem = new CheckMenuItem("Science Tool");
        scienceToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F7));
        CheckMenuItem resupplyToolItem = new CheckMenuItem("Resupply Tool");
        resupplyToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F8));

        menuTools.getItems().addAll(marsNavigatorItem, searchToolItem,timeToolItem, monitorToolItem, missionToolItem,settlementMapToolItem, scienceToolItem, resupplyToolItem );
        
        
        // --- Menu Settings
        Menu menuSettings = new Menu("Settings");
        showFullScreenItem = new CheckMenuItem("Full Screen Mode");
        showFullScreenItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
		showFullScreenItem.setSelected(true);
        CheckMenuItem showUnitBarItem = new CheckMenuItem("Show Unit Bar");
        showUnitBarItem.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN));
        CheckMenuItem showToolBarItem = new CheckMenuItem("Show Tool Bar");
        showToolBarItem.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
        
        SeparatorMenuItem SeparatorMenuItem4 = new SeparatorMenuItem();
        
        MenuItem volumeUpItem = new MenuItem("Volume Up");
        volumeUpItem.setAccelerator(new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN));
        MenuItem volumeDownItem = new MenuItem("Volume Down");
        volumeDownItem.setAccelerator(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN));
        CheckMenuItem muteItem = new CheckMenuItem("Mute");
        muteItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));

        menuSettings.getItems().addAll(showFullScreenItem, showUnitBarItem,showToolBarItem, SeparatorMenuItem4, volumeUpItem, volumeDownItem,muteItem);
        
        // --- Menu Notification
        Menu menuNotification = new Menu("Notification");
        Menu messageTypeItem = new Menu("Message Type");
        CheckMenuItem medicalItem = new CheckMenuItem("Medical");
        CheckMenuItem malfunctionItem = new CheckMenuItem("Malfunction");
        messageTypeItem.getItems().addAll(medicalItem, malfunctionItem);
        
        Menu displayTimeItem = new Menu("Display Time");
        ToggleGroup displayTimeToggleGroup = new ToggleGroup();
        RadioMenuItem confirmEachItem = new RadioMenuItem("Confirm each");
        confirmEachItem.setToggleGroup(displayTimeToggleGroup);
        RadioMenuItem threeSecondsItem = new RadioMenuItem("3 seconds");
        threeSecondsItem.setToggleGroup(displayTimeToggleGroup);
        RadioMenuItem twoSecondsItem = new RadioMenuItem("2 seconds");
        twoSecondsItem.setToggleGroup(displayTimeToggleGroup);
        RadioMenuItem oneSecondItem = new RadioMenuItem("1 second");
        oneSecondItem.setToggleGroup(displayTimeToggleGroup);
        displayTimeItem.getItems().addAll(confirmEachItem, threeSecondsItem, twoSecondsItem, oneSecondItem);
        
        Menu queueSizeItem = new Menu("Queue Size");
        ToggleGroup queueSizeToggleGroup = new ToggleGroup();
        RadioMenuItem unlimitedItem = new RadioMenuItem("Unlimited");
        unlimitedItem.setToggleGroup(queueSizeToggleGroup);
        RadioMenuItem threeItem = new RadioMenuItem("3");
        threeItem.setToggleGroup(queueSizeToggleGroup);
        RadioMenuItem oneItem = new RadioMenuItem("1");
        oneItem.setToggleGroup(queueSizeToggleGroup);
        queueSizeItem.getItems().addAll(unlimitedItem, threeItem, oneItem);

        menuNotification.getItems().addAll(messageTypeItem,displayTimeItem,queueSizeItem);
        
        // --- Menu Help
        Menu menuHelp = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        MenuItem tutorialItem = new MenuItem("Tutorial");
        tutorialItem.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
        SeparatorMenuItem SeparatorMenuItem5 = new SeparatorMenuItem();
        MenuItem userGuideItem = new MenuItem("User Guide");
        userGuideItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));

        menuHelp.getItems().addAll(aboutItem, tutorialItem,SeparatorMenuItem5, userGuideItem);

        super.getMenus().addAll(menuFile, menuTools, menuSettings, menuNotification, menuHelp);
		
  
        newItem.setOnAction(new EventHandler<ActionEvent>() {
     	   @Override 
     	   public void handle(ActionEvent e) {
     		   mainScene.newSimulation();
     	   }
     	});
        
        openItem.setOnAction(new EventHandler<ActionEvent>() {
     	   @Override 
     	   public void handle(ActionEvent e) {
     		   mainScene.loadSimulation(false);
     	   }
     	});
        
        openAutoSaveItem.setOnAction(new EventHandler<ActionEvent>() {
     	   @Override 
     	   public void handle(ActionEvent e) {
     		   mainScene.loadSimulation(true);
     	   }
     	});
        
        exitItem.setOnAction(new EventHandler<ActionEvent>() {
        	   @Override 
        	   public void handle(ActionEvent e) {
        		   mainScene.exitSimulation();
        		   mainScene.getStage().close();
        	   }
        	});
        
        saveItem.setOnAction(new EventHandler<ActionEvent>() {
     	   @Override 
     	   public void handle(ActionEvent e) {
     		   mainScene.saveSimulation(true, false);
     	   }
     	});
        
        saveAsItem.setOnAction(new EventHandler<ActionEvent>() {
      	   @Override 
      	   public void handle(ActionEvent e) {
      		   mainScene.saveSimulation(false, false);
      	   }
      	});        
        
        marsNavigatorItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			if (marsNavigatorItem.isSelected()) desktop.openToolWindow(NavigatorWindow.NAME);
    			else desktop.closeToolWindow(NavigatorWindow.NAME);
    			//if (desktop == null) System.out.println("MainWindowFXMenu : marsNav. ");
            }
        });
        
        searchToolItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			if (searchToolItem.isSelected()) desktop.openToolWindow(SearchWindow.NAME);
    			else desktop.closeToolWindow(SearchWindow.NAME);
            }
        });
        
        timeToolItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			if (timeToolItem.isSelected()) desktop.openToolWindow(TimeWindow.NAME);
    			else desktop.closeToolWindow(TimeWindow.NAME);
            }
        });
        
        monitorToolItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			if (monitorToolItem.isSelected()) desktop.openToolWindow(MonitorWindow.NAME);
    			else desktop.closeToolWindow(MonitorWindow.NAME);
            }
        });
        
        missionToolItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			if (missionToolItem.isSelected()) desktop.openToolWindow(MissionWindow.NAME);
    			else desktop.closeToolWindow(MissionWindow.NAME);
            }
        });
        
        settlementMapToolItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			if (settlementMapToolItem.isSelected()) desktop.openToolWindow(SettlementWindow.NAME);
    			else desktop.closeToolWindow(SettlementWindow.NAME);
            }
        });
        
        scienceToolItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			if (scienceToolItem.isSelected()) desktop.openToolWindow(ScienceWindow.NAME);
    			else desktop.closeToolWindow(ScienceWindow.NAME);
            }
        });
        
        resupplyToolItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			if (resupplyToolItem.isSelected()) desktop.openToolWindow(ResupplyWindow.NAME);
    			else desktop.closeToolWindow(ResupplyWindow.NAME);
            }
        });
        
        showFullScreenItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
            	boolean isFullScreen =  mainScene.getStage().isFullScreen();
            	if (!isFullScreen) {
	            	//mainScene.getStage().sizeToScene();
            		showFullScreenItem.setSelected(true);
	            	mainScene.getStage().setFullScreen(true);
            	}
            	else { 
            		showFullScreenItem.setSelected(false);
	            	mainScene.getStage().setFullScreen(false);
            	}
            }
        });

        volumeUpItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
            	float oldvolume = desktop.getSoundPlayer().getVolume();
    			desktop.getSoundPlayer().setVolume(oldvolume+0.1F);
            }
        });
		
        volumeDownItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
            	float oldvolume = desktop.getSoundPlayer().getVolume();
    			desktop.getSoundPlayer().setVolume(oldvolume-0.1F);
            }
        });
        
        muteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			desktop.getSoundPlayer().setMute(muteItem.isSelected());
            }
        });
		
        aboutItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			desktop.openToolWindow(GuideWindow.NAME);
    			GuideWindow ourGuide;
    			ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
    			ourGuide.setURL(Msg.getString("doc.about")); //$NON-NLS-1$
    		}
        });
 
        tutorialItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			desktop.openToolWindow(GuideWindow.NAME);
    			GuideWindow ourGuide;
    			ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
    			ourGuide.setURL(Msg.getString("doc.tutorial")); //$NON-NLS-1$
    		}
        });

        userGuideItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
    			desktop.openToolWindow(GuideWindow.NAME);
    			GuideWindow ourGuide;
    			ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
    			ourGuide.setURL(Msg.getString("doc.guide")); //$NON-NLS-1$
    		}
        });
		
	}
	
	public void exitFullScreen() {
		showFullScreenItem.setSelected(false);
	}
}
