/**
 * Mars Simulation Project
 * MenuTitle.java
 * @version 3.1.0 2017-05-12
 * @author Manny KUng
 */
package org.mars_sim.msp.ui.javafx.mainmenu;

//import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class MenuTitle extends Pane {
    private Text text;

    public MenuTitle(String name, int size, Color color, boolean useSpread) {
        String spread = "";
        if (useSpread) {
	        for (char c : name.toCharArray()) {
	            spread += c + " ";
	        }
	        text = new Text(spread);
	        text.setFont(Font.loadFont(MenuApp.class.getResource("/fonts/Penumbra-HalfSerif-Std_35114.ttf").toExternalForm(), size));
	        text.setEffect(new DropShadow(50, Color.BLACK));
        }
        else {
            text = new Text(name);
            text.setFont(Font.font(null, FontWeight.LIGHT, size));
        }

        text.setFill(color);
        text.setTextAlignment(TextAlignment.CENTER);
        
        getChildren().addAll(text);
    }

    public double getTitleWidth() {
        return text.getLayoutBounds().getWidth();
    }

    public double getTitleHeight() {
        return text.getLayoutBounds().getHeight();
    }
}
