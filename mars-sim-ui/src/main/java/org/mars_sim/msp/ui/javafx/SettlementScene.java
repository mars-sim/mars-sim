/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-01-28
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class SettlementScene {

    public Scene createSettlementScene() {
        Group  root  =  new  Group();
        Scene  settlementScene  =  new  Scene(root);
        settlementScene.getStylesheets().addAll("/fxui/css/settlementskin.css");		
        root.getChildren().add(new Button("Press me to go to Menu Scene"));

        return (settlementScene);
    }
	
	
}
