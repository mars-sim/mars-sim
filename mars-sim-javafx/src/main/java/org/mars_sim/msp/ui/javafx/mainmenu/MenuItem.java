/**
 * Mars Simulation Project
 * MenuItem.java
 * @version 3.1.0 2017-05-12
 * @author Manny KUng
 */

package org.mars_sim.msp.ui.javafx.mainmenu;

import javafx.beans.binding.Bindings;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
//import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
//import javafx.scene.paint.CycleMethod;
//import javafx.scene.paint.LinearGradient;
//import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class MenuItem extends Pane {
    private Text text;

    private Effect shadow = new DropShadow(20, Color.DARKGOLDENROD);//WHITESMOKE);//.ORANGE);//.TRANSPARENT);//.LIGHTGOLDENRODYELLOW);//.ORANGE);//.DARKRED);//.DARKGOLDENROD);//.ANTIQUEWHITE);//.CORAL);
    private Effect blur = new BoxBlur(1, 1, 1);

//    private LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop[] {
//            new Stop(0, Color.BLACK),
//            new Stop(0.2, Color.DARKGREY)
//    });
    
    public MenuItem(String name, int width) {
    	int n = name.length();
//        Polygon bg = new Polygon(
//                0, 0,
//                220, 0,
//                245, 25,
//                220, 50,
//                0, 50
//        );
        Polygon bg = new Polygon(
                0, 0,
                width - 20 , 0,
                width, 25,
                width - 20, 50,
                0, 50
        );

        bg.setStroke(Color.color(1, 1, 1, 0.1));//75));
        //bg.setEffect(new GaussianBlur());
        bg.fillProperty().bind(
                Bindings.when(pressedProperty())
                        .then(Color.color(1, 1, 1, 0.35))//Color.color(139D/255D, 69D/255D, 19D/255D, 0.35))
                        .otherwise(Color.color(1, 1, 1, 0.15))
        );

        text = new Text(name);
        text.setTranslateX((18 - n) * 6.5);
        text.setTranslateY(34);
        text.setFont(Font.loadFont(MenuApp.class.getResource("/fonts/Penumbra-HalfSerif-Std_35114.ttf").toExternalForm(), 22));
        text.setFill(Color.LIGHTGOLDENRODYELLOW);//.CORAL);//.WHITE);
        text.effectProperty().bind(
                Bindings.when(hoverProperty())
                        .then(shadow)
                        .otherwise(blur)
        );

        getChildren().addAll(bg, text);
    }

    public void setOnAction(Runnable action) {
        setOnMouseClicked(e -> action.run());
    }
}