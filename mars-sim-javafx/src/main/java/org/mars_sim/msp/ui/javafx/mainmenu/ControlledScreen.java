/**
 * Mars Simulation Project
 * ControlledScreen.java
 * @version 3.1.0 2017-05-08
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.mainmenu;


public interface ControlledScreen {

    //This method will allow the injection of the Parent ScreenPane
    public void setScreenParent(ScreensSwitcher screenPage);
}
