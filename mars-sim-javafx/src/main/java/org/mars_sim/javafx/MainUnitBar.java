/**
 * Mars Simulation Project
 * MainUnitBar.java
 * @version 3.1.0 2019-09-20
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.javafx;

import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;

public class MainUnitBar extends ToolBar{

	public MainUnitBar () {
		super();
		setOrientation(Orientation.HORIZONTAL);
        Button btnNew = new Button("New");
        Button btnPause = new Button("Pause");
        Button btnQuit = new Button("Quit");
        getItems().addAll(
                btnNew,
                btnPause,
                btnQuit
                
            );
		
	}
	
}
