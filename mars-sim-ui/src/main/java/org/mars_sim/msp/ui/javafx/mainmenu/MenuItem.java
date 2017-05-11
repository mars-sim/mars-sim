package org.mars_sim.msp.ui.javafx.mainmenu;

import javafx.beans.binding.Bindings;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
@SuppressWarnings("restriction")
public class MenuItem extends Pane {
    private Text text;

    private Effect shadow = new DropShadow(5, Color.ORANGE);//.DARKRED);//.DARKGOLDENROD);//.ANTIQUEWHITE);//.CORAL);
    private Effect blur = new BoxBlur(1, 1, 3);

    public MenuItem(String name) {
    	int n = name.length();
        Polygon bg = new Polygon(
                0, 0,
                220, 0,
                245, 25,
                220, 50,
                0, 50
        );
        bg.setStroke(Color.color(1, 1, 1, 0.75));
        bg.setEffect(new GaussianBlur());

        bg.fillProperty().bind(
                Bindings.when(pressedProperty())
                        .then(Color.color(139D/255D, 69D/255D, 19D/255D, 0.75))
                        .otherwise(Color.color(139D/255D, 69D/255D, 19D/255D, 0.25))
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