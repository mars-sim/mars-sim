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
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.image.Image;


/**
 *
 * @author hansolo
 */
public class Section {
    private double                _start;
    private DoubleProperty        start;
    private double                _stop;
    private DoubleProperty        stop;
    private String                _text;
    private StringProperty        text;
    private ObjectProperty<Image> icon;
    private String                _styleClass;
    private StringProperty        styleClass;


    // ******************** Constructors **************************************
    public Section() {
        this(-1, -1, "");
    }
    public Section(final double START, final double STOP) {
        this(START, STOP, "");
    }
    public Section(final double START, final double STOP, final String TEXT) {
        this(START, STOP, TEXT, null);
    }
    public Section(final double START, final double STOP, final String TEXT, final Image ICON) {
        this(START, STOP, TEXT, ICON, "");
    }
    public Section(final double START, final double STOP, final String TEXT, final Image ICON, final String STYLE_CLASS) {
        _start      = START;
        _stop       = STOP;
        _text       = TEXT;
        icon        = new SimpleObjectProperty<>(this, "icon", ICON);
        _styleClass = STYLE_CLASS;
    }


    // ******************** Methods *******************************************
    public final double getStart() {
        return null == start ? _start : start.get();
    }
    public final void setStart(final double START) {
        if (null == start) {
            _start = START;
        } else {
            start.set(START);
        }
    }
    public final DoubleProperty startProperty() {
        if (null == start) {
            start = new SimpleDoubleProperty(this, "start", _start);
        }
        return start;
    }

    public final double getStop() {
        return null == stop ? _stop : stop.get();
    }
    public final void setStop(final double STOP) {
        if (null == stop) {
            _stop = STOP;
        } else {
            stop.set(STOP);
        }
    }
    public final DoubleProperty stopProperty() {
        if (null == stop) {
            stop = new SimpleDoubleProperty(this, "stop", _stop);
        }
        return stop;
    }

    public final String getText() {
        return null == text ? _text : text.get();
    }
    public final void setText(final String TEXT) {
        if (null == text) {
            _text = TEXT;
        } else {
            text.set(TEXT);
        }
    }
    public final StringProperty textProperty() {
        if (null == text) {
            text = new SimpleStringProperty(this, "text", _text);
        }
        return text;
    }

    public final Image getImage() {
        return icon.get();
    }
    public final void setIcon(final Image IMAGE) {
        icon.set(IMAGE);
    }
    public final ObjectProperty<Image> iconProperty() {
        return icon;
    }

    public final String getStyleClass() {
        return null == styleClass ? _styleClass : styleClass.get();
    }
    public final void setStyleClass(final String STYLE_CLASS) {
        if (null == styleClass) {
            _styleClass = STYLE_CLASS;
        } else {
            styleClass.set(STYLE_CLASS);
        }
    }
    public final StringProperty styleClassProperty() {
        if (null == styleClass) {
            styleClass = new SimpleStringProperty(this, "styleClass", _styleClass);
        }
        return styleClass;
    }    
    
    public boolean contains(final double VALUE) {
        return ((Double.compare(VALUE, getStart()) >= 0 && Double.compare(VALUE, getStop()) <= 0));
    }


    // ******************** Event handling ************************************
    public final ObjectProperty<EventHandler<SectionEvent>> onEnteringSectionProperty() { return onEnteringSection; }
    public final void setOnEnteringSection(EventHandler<SectionEvent> value) { onEnteringSectionProperty().set(value); }
    public final EventHandler<SectionEvent> getOnEnteringSection() { return onEnteringSectionProperty().get(); }
    private ObjectProperty<EventHandler<SectionEvent>> onEnteringSection = new ObjectPropertyBase<EventHandler<SectionEvent>>() {
        @Override public Object getBean() { return this; }
        @Override public String getName() { return "onEnteringSection";}
    };

    public final ObjectProperty<EventHandler<SectionEvent>> onLeavingSectionProperty() { return onLeavingSection; }
    public final void setOnLeavingSection(EventHandler<SectionEvent> value) { onLeavingSectionProperty().set(value); }
    public final EventHandler<SectionEvent> getOnLeavingSection() { return onLeavingSectionProperty().get(); }
    private ObjectProperty<EventHandler<SectionEvent>> onLeavingSection = new ObjectPropertyBase<EventHandler<SectionEvent>>() {
        @Override public Object getBean() { return this; }
        @Override public String getName() { return "onLeavingSection";}
    };

    public void fireSectionEvent(final SectionEvent EVENT) {
        final EventHandler<SectionEvent> HANDLER;
        final EventType TYPE = EVENT.getEventType();
        if (SectionEvent.ENTERING_SECTION == TYPE) {
            HANDLER = getOnEnteringSection();
        } else if (SectionEvent.LEAVING_SECTION == TYPE) {
            HANDLER = getOnLeavingSection();
        } else {
            HANDLER = null;
        }

        if (null == HANDLER) return;

        HANDLER.handle(EVENT);
    }

    public boolean equals(final Section SECTION) {
        return (Double.compare(SECTION.getStart(), getStart()) == 0 &&
                Double.compare(SECTION.getStop(), getStop()) == 0 &&
                SECTION.getText().equals(getText()));
    }

    @Override public String toString() {
        final StringBuilder NAME = new StringBuilder();
        NAME.append("Section: ").append("\n");
        NAME.append("text      : ").append(getText()).append("\n");
        NAME.append("startValue: ").append(getStart()).append("\n");
        NAME.append("stopValue : ").append(getStop()).append("\n");
        return NAME.toString();
    }


    // ******************** Inner Classes *************************************
    public static class SectionEvent extends Event {
        public static final EventType<SectionEvent> ENTERING_SECTION = new EventType(ANY, "enteringSection");
        public static final EventType<SectionEvent> LEAVING_SECTION  = new EventType(ANY, "leavingSection");

        // ******************** Constructors **************************************
        public SectionEvent(final Object SOURCE, final EventTarget TARGET, EventType<SectionEvent> TYPE) {
            super(SOURCE, TARGET, TYPE);
        }
    }
}
