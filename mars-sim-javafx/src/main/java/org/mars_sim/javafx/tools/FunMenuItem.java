/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mars_sim.javafx.tools;
/*
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;


**
 * User: hansolo
 * Date: 14.08.16
 * Time: 11:24
 *
@DefaultProperty("children")
public class FunMenuItem extends StackPane {
    private static final double                                PREFERRED_WIDTH  = 48;
    private static final double                                PREFERRED_HEIGHT = 48;
    private static final double                                MINIMUM_WIDTH    = 12;
    private static final double                                MINIMUM_HEIGHT   = 12;
    private static final double                                MAXIMUM_WIDTH    = 1024;
    private static final double                                MAXIMUM_HEIGHT   = 1024;
    private static final StyleablePropertyFactory<FunMenuItem> FACTORY          = new StyleablePropertyFactory<>(Region.getClassCssMetaData());
    private static final CssMetaData<FunMenuItem, Color>       ICON_COLOR       = FACTORY.createColorCssMetaData("-icon-color", s -> s.iconColor, Color.WHITE, false);
    private        final StyleableProperty<Color>              iconColor;
    private              double                                size;
    private              FontIcon                              icon;
    private              Ikon                                  iconCode;


    // ******************** Constructors **************************************
    public FunMenuItem() {
        this(null);
    }
    public FunMenuItem(final Ikon CODE) {
        getStylesheets().add(FunMenuItem.class.getResource("/css/funmenu.css").toExternalForm());
        iconColor = new SimpleStyleableObjectProperty<>(ICON_COLOR, this, "iconColor");
        iconCode  = CODE;
        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getMinWidth(), 0.0) <= 0 || Double.compare(getMinHeight(), 0.0) <= 0) {
            setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getMaxWidth(), 0.0) <= 0 || Double.compare(getMaxHeight(), 0.0) <= 0) {
            setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        getStyleClass().add("menu-item");

        icon = null == iconCode ? new FontIcon() : new FontIcon(iconCode);
        icon.setTextOrigin(VPos.CENTER);
        icon.getStyleClass().add("menu-item-icon");

        getChildren().setAll(icon);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        iconColorProperty().addListener(o -> icon.setIconColor(getIconColor()));
    }


    // ******************** Methods *******************************************
    public FontIcon getIcon() { return icon; }

    public Ikon getIconCode() { return icon.getIconCode(); }
    public void setIconCode(final Ikon CODE) { icon.setIconCode(CODE); }

    public Color getIconColor() { return iconColor.getValue(); }
    public void setIconColor(final Color COLOR) { iconColor.setValue(COLOR); }
    public ObjectProperty<Color> iconColorProperty() { return (ObjectProperty<Color>) iconColor; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }


    // ******************** Style related *************************************
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() { return FACTORY.getCssMetaData(); }
    @Override public List<CssMetaData<? extends Styleable, ?>> getCssMetaData(){ return FACTORY.getCssMetaData(); }


    // ******************** Resizing ******************************************
    private void resize() {
        double width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        double height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size = width < height ? width : height;

        if (width > 0 && height > 0) { icon.setIconSize((int) (size * 0.75)); }
    }
}
*/