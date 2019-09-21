/**
 * Mars Simulation Project
 * MainToolBar.java
 * @version 3.1.0 2019-09-20
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.javafx;

import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

public class MainToolBar extends ToolBar {

	public MainToolBar () {
		super();
		setOrientation(Orientation.HORIZONTAL);

		//ImageView fullScreenimage = new ImageView("/fxui/icons/appbar.page.new.png");
		//fullScreenimage.setFitHeight(40);
		//fullScreenimage.setFitWidth(40);
        //Button btnFullScreen = new Button(null, fullScreenimage );
        //btnFullScreen.setTooltip(new Tooltip("Full Screen Mode"));
        
		ImageView newimage = new ImageView("/fxui/icons/appbar.page.new.png");
		newimage.setFitHeight(40);
		newimage.setFitWidth(40);
        Button btnNew = new Button(null, newimage );
        btnNew.setTooltip(new Tooltip("New"));
        
        ImageView openimage = new ImageView("/fxui/icons/appbar.folder.open.png");
        openimage.setFitHeight(40);
        openimage.setFitWidth(40);
        Button btnOpen = new Button(null, openimage);
        btnOpen.setTooltip(new Tooltip("Open"));

        ImageView saveimage = new ImageView("/fxui/icons/appbar.save.png");
        saveimage.setFitHeight(40);
        saveimage.setFitWidth(40);
        Button btnSave = new Button(null, saveimage);
        btnSave.setTooltip(new Tooltip("Save"));

        ImageView exitimage = new ImageView("/fxui/icons/appbar.door.leave.png");
        exitimage.setFitHeight(40);
        exitimage.setFitWidth(40);
        Button btnExit = new Button(null, exitimage);
        btnExit.setTooltip(new Tooltip("Exit"));
        
        ImageView mapimage = new ImageView("/fxui/icons/appbar.map.folds.png");
        mapimage.setFitHeight(40);
        mapimage.setFitWidth(40);
        Button btnMap = new Button(null, mapimage);
        btnMap.setTooltip(new Tooltip("Settlement Tool"));
        
        ImageView missionimage = new ImageView("/fxui/icons/appbar.flag.wavy.png");
        missionimage.setFitHeight(40);
        missionimage.setFitWidth(40);
        Button btnMission = new Button(null, missionimage);
        btnMission.setTooltip(new Tooltip("Mission Tool"));

        ImageView timeimage = new ImageView("/fxui/icons/appbar.hourglass.png");
        timeimage.setFitHeight(40);
        timeimage.setFitWidth(40);
        Button btnTime = new Button(null, timeimage);
        btnTime.setTooltip(new Tooltip("Time Tool"));

        ImageView crateimage = new ImageView("/fxui/icons/appbar.box.layered.png");
        crateimage.setFitHeight(40);
        crateimage.setFitWidth(40);
        Button btnResupply = new Button(null, crateimage);
        btnResupply.setTooltip(new Tooltip("Resupply Tool"));

        ImageView searchimage = new ImageView("/fxui/icons/appbar.eye.png");
        searchimage.setFitHeight(40);
        searchimage.setFitWidth(40);
        Button btnSearch = new Button(null, searchimage);
        btnSearch.setTooltip(new Tooltip("Search Tool"));
        
        ImageView scienceimage = new ImageView("/fxui/icons/appbar.potion.png");
        scienceimage.setFitHeight(40);
        scienceimage.setFitWidth(40);
        Button btnScience = new Button(null, scienceimage);
        btnScience.setTooltip(new Tooltip("Science Tool"));

        ImageView monitorimage = new ImageView("/fxui/icons/appbar.magnify.png");
        monitorimage.setFitHeight(40);
        monitorimage.setFitWidth(40);
        Button btnMonitor = new Button(null, monitorimage);
        btnMonitor.setTooltip(new Tooltip("Monitor Tool"));

        ImageView globeimage = new ImageView("/fxui/icons/appbar.globe.wire.png");
        globeimage.setFitHeight(40);
        globeimage.setFitWidth(40);
        Button btnGlobe = new Button(null, globeimage);
        btnGlobe.setTooltip(new Tooltip("Mars Navigator Tool"));

        ImageView questionimage = new ImageView("/fxui/icons/appbar.question.png");
        questionimage.setFitHeight(40);
        questionimage.setFitWidth(40);
        Button btnQuestion = new Button(null, questionimage);
        btnQuestion.setTooltip(new Tooltip("User Guide"));

        getItems().addAll(
        		//btnFullScreen,
                btnNew,
                btnOpen,
                btnSave,
                btnExit,
                new Separator(),
                btnGlobe,
                btnSearch,
                btnTime,
                btnMonitor,
                btnMission,
                btnMap,
                btnScience,
                btnResupply,
                new Separator(),
                btnQuestion
            );
		
	}
	
}
