/**
 * Mars Simulation Project
 * TemperatureGauge.java
 * @version 3.08 2015-05-04
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx;

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

import eu.hansolo.enzo.common.Marker;
import eu.hansolo.enzo.common.Section;
import eu.hansolo.enzo.gauge.Gauge;
import eu.hansolo.enzo.gauge.GaugeBuilder;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;


public class TemperatureGauge {
    private Gauge               control;
    private Marker              marker0;



    public TemperatureGauge() {
        control = GaugeBuilder.create()
                              .prefSize(300, 300)
                              //.animated(true)
                              //.startAngle(330)
                              //.angleRange(300)
                              .minValue(-120)
                              .maxValue(10)
                              .sectionsVisible(true)
                              .sections(new Section(-120, -50),
                                        new Section(-50, -20),
                                        new Section(-20, 10))
                              .areas(new Section(60, 60))
                              .majorTickSpace(20)
                              .plainValue(false)
                              .tickLabelOrientation(Gauge.TickLabelOrientation.HORIZONTAL)
                              .threshold(-40)
                              .thresholdVisible(true)
                              .minMeasuredValueVisible(true)
                              .maxMeasuredValueVisible(true)
                              .title("Weather")
                              .unit("\u00B0C")
                              .build();

        control.setStyle("-tick-label-fill: blue;");
        control.setStyle("-section-fill-0: red;"
        		+ "-section-fill-1: blue;"
        		+ "-section-fill-1: green;");
        control.setMinorTickSpace(2);
        control.setHistogramEnabled(true);

        //control.setOnThresholdExceeded(observable -> System.out.println("Threshold exceeded") );
        //control.setOnThresholdUnderrun(observable -> System.out.println("Threshold underrun"));

        marker0 = new Marker(-10);
        //marker0.setOnMarkerExceeded(observable -> System.out.println("Marker exceeded"));
        //marker0.setOnMarkerUnderrun(observable -> System.out.println("Marker underrun"));
        control.addMarker(marker0);

    }

    public Pane toDraw() {
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(5, 5, 5, 5));
        pane.getChildren().add(control);

        return pane;
    }

    public void stop() {}

}
