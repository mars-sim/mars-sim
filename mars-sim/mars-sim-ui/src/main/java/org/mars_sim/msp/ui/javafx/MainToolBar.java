/**
 * Mars Simulation Project
 * MainToolBar.java
 * @version 3.08 2015-01-29
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;

public class MainToolBar extends ToolBar {

	public MainToolBar () {
		super();
		setOrientation(Orientation.HORIZONTAL);
		
		ImageView newimage = new ImageView("/fxui/icons/appbar.page.new.png");
		newimage.setFitHeight(40);
		newimage.setFitWidth(40);
        Button btnNew = new Button(null, newimage );
        
        ImageView openimage = new ImageView("/fxui/icons/appbar.folder.open.png");
        openimage.setFitHeight(40);
        openimage.setFitWidth(40);
        Button btnOpen = new Button(null, openimage);
        
        ImageView saveimage = new ImageView("/fxui/icons/appbar.save.png");
        saveimage.setFitHeight(40);
        saveimage.setFitWidth(40);
        Button btnSave = new Button(null, saveimage);

        ImageView exitimage = new ImageView("/fxui/icons/appbar.door.leave.png");
        exitimage.setFitHeight(40);
        exitimage.setFitWidth(40);
        Button btnExit = new Button(null, exitimage);
        
        ImageView mapimage = new ImageView("/fxui/icons/appbar.map.folds.png");
        mapimage.setFitHeight(40);
        mapimage.setFitWidth(40);
        Button btnMap = new Button(null, mapimage);
        
        ImageView missionimage = new ImageView("/fxui/icons/appbar.flag.wavy.png");
        missionimage.setFitHeight(40);
        missionimage.setFitWidth(40);
        Button btnMission = new Button(null, missionimage);
        
        ImageView timeimage = new ImageView("/fxui/icons/appbar.hourglass.png");
        timeimage.setFitHeight(40);
        timeimage.setFitWidth(40);
        Button btnTime = new Button(null, timeimage);

        ImageView scienceimage = new ImageView("/fxui/icons/appbar.potion.png");
        scienceimage.setFitHeight(40);
        scienceimage.setFitWidth(40);
        Button btnScience = new Button(null, scienceimage);

        ImageView monitorimage = new ImageView("/fxui/icons/appbar.magnify.png");
        monitorimage.setFitHeight(40);
        monitorimage.setFitWidth(40);
        Button btnMonitor = new Button(null, monitorimage);

        ImageView globeimage = new ImageView("/fxui/icons/appbar.globe.wire.png");
        globeimage.setFitHeight(40);
        globeimage.setFitWidth(40);
        Button btnGlobe = new Button(null, globeimage);

        ImageView questionimage = new ImageView("/fxui/icons/appbar.question.png");
        questionimage.setFitHeight(40);
        questionimage.setFitWidth(40);
        Button btnQuestion = new Button(null, questionimage);

        getItems().addAll(
                btnNew,
                btnOpen,
                btnSave,
                btnExit,
                new Separator(),
                btnGlobe,
                // Search
                btnTime,
                // Resupply Crate
                btnMonitor,
                btnMission,
                btnMap,
                btnScience,
                new Separator(),
                btnQuestion
            );
		
	}
	
}
