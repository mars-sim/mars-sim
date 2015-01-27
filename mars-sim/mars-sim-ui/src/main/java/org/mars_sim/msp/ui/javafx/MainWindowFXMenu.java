/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-01-27
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

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

import org.mars_sim.msp.ui.javafx.MainWindowFX;


public class MainWindowFXMenu extends MenuBar  {

	//should have all the menu stuff moved over from MainWindowFX.
	

	/** 
	 * Constructor.
	 * @param mainWindow the main window pane
	 * @param desktop our main frame
	 */
	public MainWindowFXMenu(MainWindowFX mainWindow) {
	
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
        SeparatorMenuItem separatorMenuItem1 = new SeparatorMenuItem();
        SeparatorMenuItem separatorMenuItem2 = new SeparatorMenuItem();
        SeparatorMenuItem separatorMenuItem3 = new SeparatorMenuItem();

        menuFile.getItems().addAll(newItem, separatorMenuItem1, openItem, openAutoSaveItem, separatorMenuItem2, saveItem, saveAsItem, separatorMenuItem3, exitItem);
        
        // --- Menu Tools
        Menu menuTools = new Menu("Tools");
        MenuItem marsNavigatorItem = new MenuItem("Mars Navigator");
        marsNavigatorItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
        MenuItem searchToolItem = new MenuItem("Search Tool");
        searchToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        MenuItem timeToolItem = new MenuItem("Time Tool");
        timeToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F3));
        MenuItem monitorToolItem = new MenuItem("Monitor Tool");
        monitorToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F4));
        MenuItem missionToolItem = new MenuItem("Mission Tool");
        missionToolItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));
        MenuItem settlementMapTool = new MenuItem("Settlement Map Tool");
        settlementMapTool.setAccelerator(new KeyCodeCombination(KeyCode.F6));
        MenuItem scienceTool = new MenuItem("Science Tool");
        scienceTool.setAccelerator(new KeyCodeCombination(KeyCode.F7));
        MenuItem resupplyTool = new MenuItem("Resupply Tool");
        resupplyTool.setAccelerator(new KeyCodeCombination(KeyCode.F8));

        menuTools.getItems().addAll(marsNavigatorItem, searchToolItem,timeToolItem, monitorToolItem, missionToolItem,settlementMapTool, scienceTool, resupplyTool );
        
        // --- Menu Settings
        Menu menuSettings = new Menu("Settings");
        MenuItem showUnitBarItem = new MenuItem("Show Unit Bar");
        showUnitBarItem.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN));
        MenuItem showToolBarItem = new MenuItem("Show Tool Bar");
        showToolBarItem.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
        SeparatorMenuItem separatorMenuItem4 = new SeparatorMenuItem();
        MenuItem volumeUpItem = new MenuItem("Volume Up");
        volumeUpItem.setAccelerator(new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN));
        MenuItem volumeDownItem = new MenuItem("Volume Down");
        volumeDownItem.setAccelerator(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN));
        MenuItem muteItem = new MenuItem("Mute");
        muteItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));

        menuSettings.getItems().addAll(showUnitBarItem,showToolBarItem, separatorMenuItem4, volumeUpItem, volumeDownItem,muteItem);
        
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
        SeparatorMenuItem separatorMenuItem5 = new SeparatorMenuItem();
        MenuItem userGuideItem = new MenuItem("User Guide");
        userGuideItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));

        menuHelp.getItems().addAll(aboutItem, tutorialItem,separatorMenuItem5, userGuideItem);

        super.getMenus().addAll(menuFile, menuTools, menuSettings, menuNotification, menuHelp);
		
		
	}
}
