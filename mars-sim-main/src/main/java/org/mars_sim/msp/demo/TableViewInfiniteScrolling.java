package org.mars_sim.msp.demo;

import java.util.Random;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

//import org.apache.commons.lang.RandomStringUtils;

public class TableViewInfiniteScrolling extends Application {
    
    ObservableList<Person> items = FXCollections.observableArrayList();
    TableView<Person> table = new TableView<Person>();

    public TableViewInfiniteScrolling() {
        addPersons();
    }

    
    
    private void addPersons() {
        for (int i = 0; i < 50; i++) {
            Person p = new Person();
            p.setFirstName(items.size() + " " + new Random(9));
            p.setLastName("L"+ new Random(9));
            items.add(p);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        table.setItems(items);
        TableColumn<Person, String> firstNameCol = new TableColumn<Person, String>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("firstName"));
        TableColumn<Person, String> lastNameCol = new TableColumn<Person, String>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("lastName"));        
        table.getColumns().setAll(firstNameCol, lastNameCol);        
        
        Scene scene = new Scene(table, 400, 400);
        primaryStage.setScene(scene);        
        primaryStage.show();
        ScrollBar bar = getVerticalScrollbar(table);
        bar.valueProperty().addListener(this::scrolled);
    }
    
    private ScrollBar getVerticalScrollbar(TableView<?> table) {
        ScrollBar result = null;
        for (Node n : table.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    result = bar;
                }
            }
        }        
        return result;
    }
        
    void scrolled(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        double value = newValue.doubleValue();
        System.out.println("Scrolled to " + value);
        ScrollBar bar = getVerticalScrollbar(table);
        if (value == bar.getMax()) {
            System.out.println("Adding new persons.");
            double targetValue = value * items.size();
            addPersons();
            bar.setValue(targetValue / items.size());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public class Person {
        private StringProperty firstName;
        public void setFirstName(String value) { firstNameProperty().set(value); }
        public String getFirstName() { return firstNameProperty().get(); }
        public StringProperty firstNameProperty() { 
            if (firstName == null) firstName = new SimpleStringProperty(this, "firstName");
            return firstName; 
        }
    
        private StringProperty lastName;
        public void setLastName(String value) { lastNameProperty().set(value); }
        public String getLastName() { return lastNameProperty().get(); }
        public StringProperty lastNameProperty() { 
            if (lastName == null) lastName = new SimpleStringProperty(this, "lastName");
            return lastName; 
        } 
    }    
    
}