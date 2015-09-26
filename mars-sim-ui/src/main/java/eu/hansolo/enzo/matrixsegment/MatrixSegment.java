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

package eu.hansolo.enzo.matrixsegment;

import eu.hansolo.enzo.matrixsegment.skin.MatrixSegmentSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MatrixSegment extends Control {
    public static enum Dot {
        D11, D21, D31, D41, D51,
        D12, D22, D32, D42, D52,
        D13, D23, D33, D43, D53,
        D14, D24, D34, D44, D54,
        D15, D25, D35, D45, D55,
        D16, D26, D36, D46, D56,
        D17, D27, D37, D47, D57
    }
    private Color                                   _color = Color.RED;
    private ObjectProperty<Color>                   color;
    private String                                  _character = " ";
    private StringProperty                          character;
    private Map<Integer, List<Dot>>                 mapping;
    private ObjectProperty<Map<Integer, List<Dot>>> customDotMapping;
    private boolean                                 _backgroundVisible = true;
    private BooleanProperty                         backgroundVisible;
    private boolean                                 _highlightsVisible = true;
    private BooleanProperty                         highlightsVisible;
    private boolean                                 _glowEnabled = true;
    private BooleanProperty                         glowEnabled;


    // ******************** Constructors **************************************
    public MatrixSegment() {
        this(" ", Color.RED);
    }
    public MatrixSegment(final String CHARACTER) {
        this(CHARACTER, Color.RED);
    }
    public MatrixSegment(final Color COLOR) {
        this(" ", COLOR);
    }
    public MatrixSegment(final String CHARACTER, final Color COLOR) {
        getStyleClass().add("matrix-segment");
        _color     = COLOR;
        _character = CHARACTER;
        mapping    = new HashMap<>(72);

        initMapping();
    }


    // ******************** Initialization ************************************
    private void initMapping() {
        // Space
        mapping.put(20, Arrays.asList(new Dot[] {}));
        // * + , - . / : ; = \ _ < > #
        mapping.put(42, Arrays.asList(new Dot[]{Dot.D32, Dot.D13, Dot.D33, Dot.D53, Dot.D24, Dot.D34, Dot.D44, Dot.D15, Dot.D35, Dot.D55, Dot.D36}));
        mapping.put(43, Arrays.asList(new Dot[]{Dot.D32, Dot.D33, Dot.D14, Dot.D24, Dot.D34, Dot.D44, Dot.D54, Dot.D35, Dot.D36}));
        mapping.put(44, Arrays.asList(new Dot[]{Dot.D25, Dot.D35, Dot.D36, Dot.D27}));
        mapping.put(45, Arrays.asList(new Dot[]{Dot.D14, Dot.D24, Dot.D34, Dot.D44, Dot.D54}));
        mapping.put(46, Arrays.asList(new Dot[]{Dot.D35, Dot.D36, Dot.D45, Dot.D46}));
        mapping.put(47, Arrays.asList(new Dot[]{Dot.D52, Dot.D43, Dot.D34, Dot.D25, Dot.D16}));
        mapping.put(58, Arrays.asList(new Dot[]{Dot.D22, Dot.D32, Dot.D23, Dot.D33, Dot.D25, Dot.D35, Dot.D26, Dot.D36}));
        mapping.put(59, Arrays.asList(new Dot[]{Dot.D22, Dot.D32, Dot.D23, Dot.D33, Dot.D25, Dot.D35, Dot.D36, Dot.D27}));
        mapping.put(61, Arrays.asList(new Dot[]{Dot.D13, Dot.D23, Dot.D33, Dot.D43, Dot.D53, Dot.D15, Dot.D25, Dot.D35, Dot.D45, Dot.D55}));
        mapping.put(92, Arrays.asList(new Dot[]{Dot.D12, Dot.D23, Dot.D34, Dot.D45, Dot.D56}));
        mapping.put(95, Arrays.asList(new Dot[]{Dot.D17, Dot.D27, Dot.D37, Dot.D47, Dot.D57}));
        mapping.put(60, Arrays.asList(new Dot[]{Dot.D41, Dot.D32, Dot.D23, Dot.D14, Dot.D25, Dot.D36, Dot.D47}));
        mapping.put(62, Arrays.asList(new Dot[]{Dot.D21, Dot.D32, Dot.D43, Dot.D54, Dot.D45, Dot.D36, Dot.D27}));
        mapping.put(35, Arrays.asList(new Dot[]{Dot.D21, Dot.D41, Dot.D22, Dot.D42, Dot.D13, Dot.D23, Dot.D33, Dot.D43, Dot.D53, Dot.D24, Dot.D44, Dot.D15, Dot.D25, Dot.D35, Dot.D45, Dot.D55, Dot.D26, Dot.D46, Dot.D27, Dot.D47}));
        mapping.put(34, Arrays.asList(new Dot[]{Dot.D21, Dot.D41, Dot.D22, Dot.D42, Dot.D23, Dot.D43}));
        // 0 - 9
        mapping.put(48, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D33, Dot.D53, Dot.D14, Dot.D34, Dot.D54, Dot.D15, Dot.D35, Dot.D55, Dot.D16, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(49, Arrays.asList(new Dot[]{Dot.D31, Dot.D22, Dot.D32, Dot.D33, Dot.D34, Dot.D35, Dot.D36, Dot.D17, Dot.D27, Dot.D37, Dot.D47, Dot.D57}));
        mapping.put(50, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D53, Dot.D44, Dot.D35, Dot.D26, Dot.D17, Dot.D27, Dot.D37, Dot.D47, Dot.D57}));
        mapping.put(51, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D53, Dot.D34, Dot.D44, Dot.D55, Dot.D16, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(52, Arrays.asList(new Dot[]{Dot.D14, Dot.D32, Dot.D42, Dot.D23, Dot.D41, Dot.D43, Dot.D44, Dot.D15, Dot.D25, Dot.D35, Dot.D45, Dot.D55, Dot.D46, Dot.D47}));
        mapping.put(53, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D31, Dot.D41, Dot.D51, Dot.D12, Dot.D13, Dot.D23, Dot.D33, Dot.D43, Dot.D54, Dot.D55, Dot.D16, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(54, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D14, Dot.D24, Dot.D34, Dot.D44, Dot.D15, Dot.D55, Dot.D16, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(55, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D31, Dot.D41, Dot.D51, Dot.D52, Dot.D43, Dot.D34, Dot.D35, Dot.D36, Dot.D37}));
        mapping.put(56, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D24, Dot.D34, Dot.D44, Dot.D15, Dot.D55, Dot.D16, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(57, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D24, Dot.D34, Dot.D44, Dot.D54, Dot.D55, Dot.D16, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        // ? ! % $ [ ] ( ) { }
        mapping.put(63, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D53, Dot.D34, Dot.D44, Dot.D35, Dot.D37}));
        mapping.put(33, Arrays.asList(new Dot[]{Dot.D31, Dot.D32, Dot.D33, Dot.D34, Dot.D35, Dot.D37}));
        mapping.put(37, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D12, Dot.D22, Dot.D52, Dot.D43, Dot.D34, Dot.D25, Dot.D16, Dot.D46, Dot.D56, Dot.D47, Dot.D57}));
        mapping.put(36, Arrays.asList(new Dot[]{Dot.D31, Dot.D22, Dot.D32, Dot.D42, Dot.D52, Dot.D13, Dot.D33, Dot.D24, Dot.D34, Dot.D44, Dot.D35, Dot.D55, Dot.D16, Dot.D26, Dot.D36, Dot.D46, Dot.D37}));
        mapping.put(91, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D42, Dot.D43, Dot.D44, Dot.D45, Dot.D46, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(93, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D22, Dot.D23, Dot.D24, Dot.D25, Dot.D26, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(40, Arrays.asList(new Dot[]{Dot.D41, Dot.D32, Dot.D23, Dot.D24, Dot.D25, Dot.D36, Dot.D47}));
        mapping.put(41, Arrays.asList(new Dot[]{Dot.D21, Dot.D32, Dot.D43, Dot.D44, Dot.D45, Dot.D36, Dot.D27}));
        mapping.put(123, Arrays.asList(new Dot[]{Dot.D31, Dot.D41, Dot.D22, Dot.D23, Dot.D14, Dot.D25, Dot.D26, Dot.D37, Dot.D47}));
        mapping.put(125, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D42, Dot.D43, Dot.D54, Dot.D45, Dot.D46, Dot.D27, Dot.D37}));
        // A - Z
        mapping.put(65, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D14, Dot.D54, Dot.D15, Dot.D25, Dot.D35, Dot.D45, Dot.D15, Dot.D55, Dot.D16, Dot.D56, Dot.D17, Dot.D57}));
        mapping.put(66, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D31, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D14, Dot.D24, Dot.D34, Dot.D44, Dot.D15, Dot.D55, Dot.D16, Dot.D56, Dot.D17, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(67, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D51, Dot.D12, Dot.D13, Dot.D14, Dot.D15, Dot.D16, Dot.D27, Dot.D37, Dot.D47, Dot.D57}));
        mapping.put(68, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D14, Dot.D54, Dot.D15, Dot.D55, Dot.D16, Dot.D56, Dot.D17, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(69, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D31, Dot.D41, Dot.D51, Dot.D12, Dot.D13, Dot.D14, Dot.D24, Dot.D34, Dot.D44, Dot.D15, Dot.D16, Dot.D17, Dot.D27, Dot.D37, Dot.D47, Dot.D57}));
        mapping.put(70, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D31, Dot.D41, Dot.D51, Dot.D12, Dot.D13, Dot.D14, Dot.D24, Dot.D34, Dot.D44, Dot.D15, Dot.D16, Dot.D17}));
        mapping.put(71, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D51, Dot.D12, Dot.D13, Dot.D14, Dot.D34, Dot.D44, Dot.D54, Dot.D15, Dot.D55, Dot.D16, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(72, Arrays.asList(new Dot[]{Dot.D11, Dot.D51, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D14, Dot.D24, Dot.D34, Dot.D44, Dot.D54, Dot.D15, Dot.D55, Dot.D16, Dot.D56, Dot.D17, Dot.D57}));
        mapping.put(73, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D31, Dot.D41, Dot.D51, Dot.D32, Dot.D33, Dot.D34, Dot.D35, Dot.D36, Dot.D17, Dot.D27, Dot.D37, Dot.D47, Dot.D57}));
        mapping.put(74, Arrays.asList(new Dot[]{Dot.D51, Dot.D52, Dot.D53, Dot.D54, Dot.D15, Dot.D55, Dot.D16, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(75, Arrays.asList(new Dot[]{Dot.D11, Dot.D51, Dot.D12, Dot.D42, Dot.D13, Dot.D33, Dot.D14, Dot.D24, Dot.D15, Dot.D35, Dot.D16, Dot.D46, Dot.D17, Dot.D57}));
        mapping.put(76, Arrays.asList(new Dot[]{Dot.D11, Dot.D12, Dot.D13, Dot.D14, Dot.D15, Dot.D16, Dot.D17, Dot.D27, Dot.D37, Dot.D47, Dot.D57}));
        mapping.put(77, Arrays.asList(new Dot[]{Dot.D11, Dot.D51, Dot.D12, Dot.D22, Dot.D42, Dot.D52, Dot.D13, Dot.D33, Dot.D53, Dot.D14, Dot.D34, Dot.D54, Dot.D15, Dot.D55, Dot.D16, Dot.D56, Dot.D17, Dot.D57}));
        mapping.put(78, Arrays.asList(new Dot[]{Dot.D11, Dot.D51, Dot.D12, Dot.D52, Dot.D13, Dot.D23, Dot.D53, Dot.D14, Dot.D34, Dot.D54, Dot.D15, Dot.D45, Dot.D55, Dot.D16, Dot.D56, Dot.D17, Dot.D57}));
        mapping.put(79, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D14, Dot.D54, Dot.D15, Dot.D55, Dot.D16, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(80, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D14, Dot.D24, Dot.D34, Dot.D44, Dot.D15, Dot.D16, Dot.D17}));
        mapping.put(81, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D14, Dot.D54, Dot.D15, Dot.D35, Dot.D55, Dot.D16, Dot.D46, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(82, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D14, Dot.D24, Dot.D34, Dot.D44, Dot.D15, Dot.D35, Dot.D16, Dot.D46, Dot.D17, Dot.D57}));
        mapping.put(83, Arrays.asList(new Dot[]{Dot.D21, Dot.D31, Dot.D41, Dot.D12, Dot.D52, Dot.D13, Dot.D24, Dot.D34, Dot.D44, Dot.D55, Dot.D16, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(84, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D31, Dot.D41, Dot.D51, Dot.D32, Dot.D33, Dot.D34, Dot.D35, Dot.D36, Dot.D37}));
        mapping.put(85, Arrays.asList(new Dot[]{Dot.D11, Dot.D51, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D14, Dot.D54, Dot.D15, Dot.D55, Dot.D16, Dot.D56, Dot.D27, Dot.D37, Dot.D47}));
        mapping.put(86, Arrays.asList(new Dot[]{Dot.D11, Dot.D51, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D14, Dot.D54, Dot.D15, Dot.D55, Dot.D26, Dot.D46, Dot.D37}));
        mapping.put(87, Arrays.asList(new Dot[]{Dot.D11, Dot.D51, Dot.D12, Dot.D52, Dot.D13, Dot.D53, Dot.D14, Dot.D34, Dot.D54, Dot.D15, Dot.D35, Dot.D55, Dot.D16, Dot.D26, Dot.D46, Dot.D56, Dot.D17, Dot.D57}));
        mapping.put(88, Arrays.asList(new Dot[]{Dot.D11, Dot.D51, Dot.D12, Dot.D52, Dot.D23, Dot.D43, Dot.D34, Dot.D25, Dot.D45, Dot.D16, Dot.D56, Dot.D17, Dot.D57}));
        mapping.put(89, Arrays.asList(new Dot[]{Dot.D11, Dot.D51, Dot.D12, Dot.D52, Dot.D23, Dot.D43, Dot.D34, Dot.D35, Dot.D36, Dot.D37}));
        mapping.put(90, Arrays.asList(new Dot[]{Dot.D11, Dot.D21, Dot.D31, Dot.D41, Dot.D51, Dot.D52, Dot.D43, Dot.D34, Dot.D25, Dot.D16, Dot.D17, Dot.D27, Dot.D37, Dot.D47, Dot.D57}));
    }


    // ******************** Methods *******************************************
    @Override public boolean isResizable() {
        return true;
    }

    public final Color getColor() {
        return null == color ? _color : color.get();
    }
    public final void setColor(final Color COLOR) {
        if (null == color) {
            _color = COLOR;
        } else {
            color.set(COLOR);
        }
    }
    public final ObjectProperty<Color> colorProperty() {
        if (null == color) {
            color = new SimpleObjectProperty<>(this, "ledColor", _color);
        }
        return color;
    }

    public final String getCharacter() {
        return null == character ? _character : character.get();
    }
    public final void setCharacter(final String CHARACTER) {
        if (null == character) {
            _character = CHARACTER;
        } else {
            character.set(CHARACTER);
        }
    }
    public final void setCharacter(final Character CHARACTER) {
        if (null == character) {
            _character = String.valueOf(CHARACTER);
        } else {
            character.set(String.valueOf(CHARACTER));
        }
    }
    public final StringProperty characterProperty() {
        if (null == character) {
            character = new SimpleStringProperty(this, "character", _character);
        }
        return character;
    }

    public final Map<Integer, List<Dot>> getCustomDotMapping() {
        if (customDotMapping == null) {
            customDotMapping = new SimpleObjectProperty<Map<Integer, List<Dot>>>(new HashMap<>());
        }
        return customDotMapping.get();
    }
    public final void setCustomDotMapping(final Map<Integer, List<Dot>> CUSTOM_DOT_MAPPING) {
        if (customDotMapping == null) {
            customDotMapping = new SimpleObjectProperty<Map<Integer, List<Dot>>>(new HashMap<>());
        }
        customDotMapping.get().clear();
        for (int key : CUSTOM_DOT_MAPPING.keySet()) {
            customDotMapping.get().put(key, CUSTOM_DOT_MAPPING.get(key));
        }
    }
    public final ObjectProperty<Map<Integer, List<Dot>>> customDotMappingProperty() {
        if (customDotMapping == null) {
            customDotMapping = new SimpleObjectProperty<Map<Integer, List<Dot>>>(new HashMap<>());
        }
        return customDotMapping;
    }

    /**
     * Returns a Map that contains the default mapping from ascii integers to lcd segments.
     * The segments are defined as follows:
     *
     *        D11 D21 D31 D41 D51
     *        D12 D22 D32 D42 D52
     *        D13 D23 D33 D43 D53
     *        D14 D24 D34 D44 D54
     *        D15 D25 D35 D45 D55
     *        D16 D26 D36 D46 D56
     *        D17 D27 D37 D47 D57
     *
     * If you would like to add a $ sign (ASCII: 36) for example you should add the following code to
     * your custom dot map.
     *
     * customDotMapping.put(36, Arrays.asList(new DotMatrixSegment.Dot[] {
     *     DotMatrixSegment.Dot.D11,
     *     DotMatrixSegment.Dot.A2,
     *     DotMatrixSegment.Dot.F,
     *     DotMatrixSegment.Dot.P,
     *     DotMatrixSegment.Dot.K,
     *     DotMatrixSegment.Dot.C,
     *     DotMatrixSegment.Dot.D2,
     *     DotMatrixSegment.Dot.D1,
     *     DotMatrixSegment.Dot.H,
     *     DotMatrixSegment.Dot.M
     * }));
     *
     * @return a Map that contains the default mapping from ascii integers to segments
     */
    public final Map<Integer, List<Dot>> getDotMapping() {
        HashMap<Integer, List<Dot>> dotMapping = new HashMap<Integer, List<Dot>>(42);
        for (int key : mapping.keySet()) {
            dotMapping.put(key, mapping.get(key));
        }
        return dotMapping;
    }

    public final boolean isBackgroundVisible() {
        return null == backgroundVisible ? _backgroundVisible : backgroundVisible.get();
    }
    public final void setBackgroundVisible(final boolean BACKGROUND_VISIBLE) {
        if (null == backgroundVisible) {
            _backgroundVisible = BACKGROUND_VISIBLE;
        } else {
            backgroundVisible.set(BACKGROUND_VISIBLE);
        }
    }
    public final BooleanProperty backgroundVisibleProperty() {
        if (null == backgroundVisible) {
            backgroundVisible = new SimpleBooleanProperty(this, "backgroundVisible", _backgroundVisible);
        }
        return backgroundVisible;
    }

    public final boolean isHighlightsVisible() {
        return null == highlightsVisible ? _highlightsVisible : highlightsVisible.get();
    }
    public final void setHighlightsVisible(final boolean HIGHLIGHTS_VISIBLE) {
        if (null == highlightsVisible) {
            _highlightsVisible = HIGHLIGHTS_VISIBLE;
        } else {
            highlightsVisible.set(HIGHLIGHTS_VISIBLE);
        }
    }
    public final BooleanProperty highlightsVisibleProperty() {
        if (null == highlightsVisible) {
            highlightsVisible = new SimpleBooleanProperty(this, "highlightsVisible", _highlightsVisible);
        }
        return highlightsVisible;
    }

    public final boolean isGlowEnabled() {
        return null == glowEnabled ? _glowEnabled : glowEnabled.get();
    }
    public final void setGlowEnabled(final boolean GLOW_ENABLED) {
        if (null == glowEnabled) {
            _glowEnabled = GLOW_ENABLED;
        } else {
            glowEnabled.set(GLOW_ENABLED);
        }
    }
    public final BooleanProperty glowEnabledProperty() {
        if (null == glowEnabled) {
            glowEnabled = new SimpleBooleanProperty(this, "glowEnabled", _glowEnabled);
        }
        return glowEnabled;
    }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        return new MatrixSegmentSkin(this);
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("/eu/hansolo/enzo/matrixsegment/" + getClass().getSimpleName().toLowerCase() + ".css").toExternalForm();
    }
}

