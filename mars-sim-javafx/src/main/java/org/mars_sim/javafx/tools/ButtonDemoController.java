package org.mars_sim.javafx.tools;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Jens Deters
 */
public class ButtonDemoController {
    @FXML
    private ToggleGroup g2;

    @FXML
    private ToggleGroup g3;

    public void initialize() {
        JavaFXUtil.get().addAlwaysOneSelectedSupport(g2);
        JavaFXUtil.get().addAlwaysOneSelectedSupport(g3);
    }

}
