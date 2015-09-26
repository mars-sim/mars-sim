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

package eu.hansolo.enzo.common;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

import java.util.HashMap;


/**
 * Created by
 * User: hansolo
 * Date: 26.08.13
 * Time: 16:13
 */
public class SectionBuilder {
    private HashMap<String, Property> properties = new HashMap<>();

    // ******************** Constructors **************************************
    protected SectionBuilder() {}

    // ******************** Methods *******************************************
    public static final SectionBuilder create() {
        return new SectionBuilder();
    }

    public final SectionBuilder start(final double START) {
        properties.put("start", new SimpleDoubleProperty(START));
        return this;
    }

    public final SectionBuilder stop(final double STOP) {
        properties.put("stop", new SimpleDoubleProperty(STOP));
        return this;
    }

    public final SectionBuilder text(final String TEXT) {
        properties.put("text", new SimpleStringProperty(TEXT));
        return this;
    }

    public final SectionBuilder icon(final Image IMAGE) {
        properties.put("icon", new SimpleObjectProperty<>(IMAGE));
        return this;
    }
    
    public final SectionBuilder styleClass(final String STYLE_CLASS) {
        properties.put("styleClass", new SimpleStringProperty(STYLE_CLASS));
        return this;
    }

    public final Section build() {
        final Section SECTION = new Section();
        for (String key : properties.keySet()) {
            if ("start".equals(key)) {
                SECTION.setStart(((DoubleProperty) properties.get(key)).get());
            } else if("stop".equals(key)) {
                SECTION.setStop(((DoubleProperty) properties.get(key)).get());
            } else if("text".equals(key)) {
                SECTION.setText(((StringProperty) properties.get(key)).get());
            } else if("icon".equals(key)) {
                SECTION.setIcon(((ObjectProperty<Image>) properties.get(key)).get());
            } else if ("styleClass".equals(key)) {
                SECTION.setStyleClass(((StringProperty) properties.get(key)).get());
            }
        }
        return SECTION;
    }
}
