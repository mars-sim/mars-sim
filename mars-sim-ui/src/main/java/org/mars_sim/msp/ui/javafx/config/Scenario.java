package org.mars_sim.msp.ui.javafx.config;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author cdea
 */
public class Scenario {
    private StringProperty chapter;
    private IntegerProperty sortOrder;
    private StringProperty name;
    private StringProperty fullClassName;
    private StringProperty className;
    private StringProperty description;

    public Scenario (String name, int sortOrder, String chapter, String fullClassName, String className, String description) {
        this.sortOrder = new SimpleIntegerProperty(sortOrder);
        this.name = new SimpleStringProperty(name);
        this.chapter = new SimpleStringProperty(chapter);
        this.fullClassName = new SimpleStringProperty(fullClassName);
        this.className = new SimpleStringProperty(className);
        this.description = new SimpleStringProperty(description);
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public IntegerProperty sortOrderProperty() {
        return sortOrder;
    }
    
    public StringProperty chapterProperty() {
        return chapter;
    }
    
    public StringProperty fullClassNameProperty() {
        return fullClassName;
    }
    
    public StringProperty classNameProperty() {
        return className;
    }

    public StringProperty descriptionProperty() {
        return description;
    }
    
    public String getName() {
        return nameProperty().getValue();
    }
    
    public int getSortOrderProperty() {
        return sortOrderProperty().getValue();
    }
    
    public String getChapter() {
        return chapterProperty().getValue();
    }
    
    public String getFullClassName() {
        return fullClassNameProperty().getValue();
    }
    
    public String getClassName() {
        return classNameProperty().getValue();
    }

    public String getDescription() {
        return descriptionProperty().getValue();
    }

    @Override
    public String toString() {
        return getName() + ": " + getSortOrderProperty() + ", " + getChapter() + ", "+ getFullClassName() + ", " + getClassName() + ", " + getDescription();
    }    
}
