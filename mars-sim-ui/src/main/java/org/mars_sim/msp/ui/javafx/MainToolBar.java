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
        Button btnNew = new Button(null, new ImageView("/fxui/icons/appbar.page.new.png"));
        Button btnOpen = new Button(null, new ImageView("/fxui/icons/appbar.folder.open.png"));
        Button btnSave = new Button(null, new ImageView("/fxui/icons/appbar.save.png"));
        Button btnExit = new Button(null, new ImageView("/fxui/icons/appbar.door.leave.png"));
        Button btnMap = new Button(null, new ImageView("/fxui/icons/appbar.map.folds.png"));
        Button btnMission = new Button(null, new ImageView("/fxui/icons/appbar.flag.wavy.png"));
        Button btnTime = new Button(null, new ImageView("/fxui/icons/appbar.hourglass.png"));
        Button btnScience = new Button(null, new ImageView("/fxui/icons/appbar.potion.png"));
        Button btnMonitor = new Button(null, new ImageView("/fxui/icons/appbar.magnify.png"));
        Button btnGlobe = new Button(null, new ImageView("/fxui/icons/appbar.globe.wire.png"));
        Button btnQuestion = new Button(null, new ImageView("/fxui/icons/appbar.question.png"));

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
