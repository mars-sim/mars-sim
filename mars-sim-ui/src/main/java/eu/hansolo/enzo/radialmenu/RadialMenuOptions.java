/*
 * Copyright (c) 2015 by Gerrit Grunwald
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

package eu.hansolo.enzo.radialmenu;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;


/**
 * Created with IntelliJ IDEA.
 * User: hansolo
 * Date: 21.09.12
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */
public class RadialMenuOptions {
    private double                _degrees;
    private DoubleProperty        degrees;
    private double                _offset;
    private DoubleProperty        offset;
    private double                _radius;
    private DoubleProperty        radius;
    private double                _buttonSize;
    private DoubleProperty        buttonSize;
    private Color                 _buttonFillColor;
    private ObjectProperty<Color> buttonFillColor;
    private Color                 _buttonStrokeColor;
    private ObjectProperty<Color> buttonStrokeColor;
    private Color                 _buttonForegroundColor;
    private ObjectProperty<Color> buttonForegroundColor;
    private double                _buttonAlpha;
    private DoubleProperty        buttonAlpha;
    private boolean               _buttonVisible;
    private BooleanProperty       buttonVisible;
    private boolean               _buttonHideOnSelect;
    private BooleanProperty       buttonHideOnSelect;
    private boolean               _buttonHideOnClose;
    private BooleanProperty       buttonHideOnClose;
    private boolean               _tooltipsEnabled;
    private BooleanProperty       tooltipsEnabled;
    private boolean               _simpleMode;
    private BooleanProperty       simpleMode;
    private boolean               _strokeVisible;
    private BooleanProperty       strokeVisible;


    // ******************** Constructors **************************************
    public RadialMenuOptions() {
        this(360, -90, 100);
    }
    public RadialMenuOptions(final double DEGREES, final double OFFSET, final double RADIUS) {
        this(DEGREES, OFFSET, RADIUS, 44, Color.RED, Color.WHITE, Color.WHITE, true, false, false, 0.5, true);
    }
    public RadialMenuOptions(final double DEGREES, final double OFFSET, final double RADIUS, final double BUTTON_SIZE, final Color BUTTON_INNER_COLOR, final Color BUTTON_FRAME_COLOR, final Color BUTTON_FOREGROUND_COLOR, final boolean BUTTON_HIDE_ON_SELECT, final boolean HIDE_ON_CLOSE, final boolean TOOLTIPS_ENABLED, final double BUTTON_ALPHA, final boolean BUTTON_VISIBLE) {
        _degrees               = DEGREES;
        _offset                = OFFSET;
        _radius                = RADIUS;
        _buttonSize            = BUTTON_SIZE;
        _buttonFillColor       = BUTTON_INNER_COLOR;
        _buttonStrokeColor     = BUTTON_FRAME_COLOR;
        _buttonForegroundColor = BUTTON_FOREGROUND_COLOR;
        _buttonAlpha           = BUTTON_ALPHA;
        _buttonHideOnSelect    = BUTTON_HIDE_ON_SELECT;
        _buttonHideOnClose     = HIDE_ON_CLOSE;
        _tooltipsEnabled       = TOOLTIPS_ENABLED;
        _buttonVisible         = BUTTON_VISIBLE;
        _simpleMode            = false;
        _strokeVisible         = true;
    }


    // ******************** Methods *******************************************
    public double getDegrees() {
        return null == degrees ? _degrees : degrees.get();
    }
    public void setDegrees(final double DEGREES) {
        if (null == degrees) {
            _degrees = DEGREES;
        } else {
            degrees.set(DEGREES);
        }
    }
    public DoubleProperty degreesProperty() {
        if (null == degrees) {
            degrees = new SimpleDoubleProperty(this, "degrees", _degrees);
        }
        return degrees;
    }

    public double getOffset() {
        return null == offset ? _offset : offset.get();
    }
    public void setOffset(final double OFFSET) {
        if (null == offset) {
            _offset = OFFSET;
        } else {
            offset.set(OFFSET);
        }
    }
    public DoubleProperty offsetProperty() {
        if (null == offset) {
            offset = new SimpleDoubleProperty(this, "offset", _offset);
        }
        return offset;
    }

    public double getRadius() {
        return null == radius ? _radius : radius.get();
    }
    public void setRadius(final double RADIUS) {
        if (null == radius) {
            _radius = RADIUS;
        } else {
            radius.set(RADIUS);
        }
    }
    public DoubleProperty radiusProperty() {
        if (null == radius) {
            radius = new SimpleDoubleProperty(this, "radius", _radius);
        }
        return radius;
    }

    public double getButtonSize() {
        return null == buttonSize ? _buttonSize : buttonSize.get();
    }
    public void setButtonSize(final double BUTTON_SIZE) {
        if (null == buttonSize) {
            _buttonSize = BUTTON_SIZE;
        } else {
            buttonSize.set(BUTTON_SIZE);
        }
    }
    public DoubleProperty buttonSizeProperty() {
        if (null == buttonSize) {
            buttonSize = new SimpleDoubleProperty(this, "buttonSize", _buttonSize);
        }
        return buttonSize;
    }

    public Color getButtonFillColor() {
        return null == buttonFillColor ? _buttonFillColor : buttonFillColor.get();
    }
    public void setButtonFillColor(final Color BUTTON_FILL_COLOR) {
        if (null == buttonFillColor) {
            _buttonFillColor = BUTTON_FILL_COLOR;
        } else {
            buttonFillColor.set(BUTTON_FILL_COLOR);
        }
    }
    public ObjectProperty<Color> buttonFillColorProperty() {
        if (null == buttonFillColor) {
            buttonFillColor = new SimpleObjectProperty<>(this, "buttonFillColor", _buttonFillColor);
        }
        return buttonFillColor;
    }

    public Color getButtonStrokeColor() {
        return null == buttonStrokeColor ? _buttonStrokeColor : buttonStrokeColor.get();
    }
    public void setButtonStrokeColor(final Color BUTTON_STROKE_COLOR) {
        if (null == buttonStrokeColor) {
            _buttonStrokeColor = BUTTON_STROKE_COLOR;
        } else {
            buttonStrokeColor.set(BUTTON_STROKE_COLOR);
        }
    }
    public ObjectProperty<Color> buttonStrokeColorProperty() {
        if (null == buttonStrokeColor) {
            buttonStrokeColor = new SimpleObjectProperty<>(this, "buttonStrokeColor", _buttonStrokeColor);
        }
        return buttonStrokeColor;
    }

    public Color getButtonForegroundColor() {
        return null == buttonForegroundColor ? _buttonForegroundColor : buttonForegroundColor.get();
    }
    public void setButtonForegroundColor(final Color BUTTON_FOREGROUND_COLOR) {
        if (null == buttonForegroundColor) {
            _buttonForegroundColor = BUTTON_FOREGROUND_COLOR;
        } else {
            buttonForegroundColor.set(BUTTON_FOREGROUND_COLOR);
        }
    }
    public ObjectProperty<Color> buttonForegroundColorProperty() {
        if (null == buttonForegroundColor) {
            buttonForegroundColor = new SimpleObjectProperty<>(this, "buttonForegroundColor", _buttonForegroundColor);
        }
        return buttonForegroundColor;
    }

    public double getButtonAlpha() {
        return null == buttonAlpha ? _buttonAlpha : buttonAlpha.get();
    }
    public void setButtonAlpha(final double BUTTON_ALPHA) {
        double alpha = BUTTON_ALPHA < 0 ? 0 : (BUTTON_ALPHA > 1 ? 1.0 : BUTTON_ALPHA);
        if (null == buttonAlpha) {
            _buttonAlpha = alpha;
        } else {
            buttonAlpha.set(alpha);
        }
    }
    public DoubleProperty buttonAlphaProperty() {
        if (null == buttonAlpha) {
            buttonAlpha = new SimpleDoubleProperty(this, "buttonAlpha", _buttonAlpha);
        }
        return buttonAlpha;
    }

    public boolean isButtonHideOnSelect() {
        return null == buttonHideOnSelect ? _buttonHideOnSelect : buttonHideOnSelect.get();
    }
    public void setButtonHideOnSelect(final boolean BUTTON_HIDE_ON_SELECT) {
        if (null == buttonHideOnSelect) {
            _buttonHideOnSelect = BUTTON_HIDE_ON_SELECT;
        } else {
            buttonHideOnSelect.set(BUTTON_HIDE_ON_SELECT);
        }
    }
    public BooleanProperty buttonHideOnSelectProperty() {
        if (null == buttonHideOnSelect) {
            buttonHideOnSelect = new SimpleBooleanProperty(this, "buttonHideOnSelect", _buttonHideOnSelect);
        }
        return buttonHideOnSelect;
    }

    public boolean isButtonHideOnClose() {
        return null == buttonHideOnClose ? _buttonHideOnClose : buttonHideOnClose.get();
    }
    public void setButtonHideOnClose(final boolean BUTTON_HIDE_ON_CLOSE) {
        if (null == buttonHideOnClose) {
            _buttonHideOnClose = BUTTON_HIDE_ON_CLOSE;
        } else {
            buttonHideOnClose.set(BUTTON_HIDE_ON_CLOSE);
        }
    }
    public BooleanProperty buttonHideOnCloseProperty() {
        if (null == buttonHideOnClose) {
            buttonHideOnClose = new SimpleBooleanProperty(this, "buttonHideOnClose", _buttonHideOnClose);
        }
        return buttonHideOnClose;
    }

    public boolean isTooltipsEnabled() {
        return null == tooltipsEnabled ? _tooltipsEnabled : tooltipsEnabled.get();
    }
    public void setTooltipsEnabled(final boolean TOOLTIPS_ENABLED) {
        if (null == tooltipsEnabled) {
            _tooltipsEnabled = TOOLTIPS_ENABLED;
        } else {
            tooltipsEnabled.set(TOOLTIPS_ENABLED);
        }
    }
    public BooleanProperty tooltipsEnabledProperty() {
        if (null == tooltipsEnabled) {
            tooltipsEnabled = new SimpleBooleanProperty(this, "tooltipsEnabled", _tooltipsEnabled);
        }
        return tooltipsEnabled;
    }

    public boolean isButtonVisible() {
        return null == buttonVisible ? _buttonVisible : buttonVisible.get();
    }
    public void setButtonVisible(final boolean BUTTON_VISIBLE) {
        if (null == buttonVisible) {
            _buttonVisible = BUTTON_VISIBLE;
        } else {
            buttonVisible.set(BUTTON_VISIBLE);
        }
    }
    public BooleanProperty buttonVisibleProperty() {
        if (null == buttonVisible) {
            buttonVisible = new SimpleBooleanProperty(this, "buttonVisible", _buttonVisible);
        }
        return buttonVisible;
    }

    public boolean isSimpleMode() {
        return null == simpleMode ? _simpleMode : simpleMode.get();
    }
    public void setSimpleMode(final boolean SIMPLE_MODE) {
        if (null == simpleMode) {
            _simpleMode = SIMPLE_MODE;
        } else {
            simpleMode.set(SIMPLE_MODE);
        }
    }
    public BooleanProperty simpleModeProperty() {
        if (null == simpleMode) {
            simpleMode = new SimpleBooleanProperty(this, "simpleMode", _simpleMode);
        }
        return simpleMode;
    }

    public boolean isStrokeVisible() {
        return null == strokeVisible ? _strokeVisible : strokeVisible.get();
    }
    public void setStrokeVisible(final boolean STROKE_VISIBLE) {
        if (null == strokeVisible) {
            _strokeVisible = STROKE_VISIBLE;
        } else {
            strokeVisible.set(STROKE_VISIBLE);
        }
    }
    public BooleanProperty strokeVisibleProperty() {
        if (null == strokeVisible) {
            strokeVisible = new SimpleBooleanProperty(this, "strokeVisible", _strokeVisible);
        }
        return strokeVisible;
    }
}
