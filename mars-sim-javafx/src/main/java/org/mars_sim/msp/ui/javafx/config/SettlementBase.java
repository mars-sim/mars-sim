/**
 * Mars Simulation Project
 * SettlementBase.java
 * @version 3.1.0 2018-11-04
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.config;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SettlementBase {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty template = new SimpleStringProperty();
    private final StringProperty settler = new SimpleStringProperty();
    private final StringProperty bot = new SimpleStringProperty();
    private final StringProperty sponsor = new SimpleStringProperty();
    private final StringProperty latitude = new SimpleStringProperty();
    private final StringProperty longitude = new SimpleStringProperty();
    
    public SettlementBase() {};
    
    public SettlementBase(String name, String template, String settler, 
    		String bot, String sponsor, String latitude, String longitude) {
        setName(name);
        setTemplate(template);
        setSettler(settler);
        setBot(bot);
        setSponsor(sponsor);
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public final StringProperty nameProperty() {
        return this.name;
    }

    public final String getName() {
        return this.nameProperty().get();
    }

    public final void setName(final String name) {
        this.nameProperty().set(name);
    }

    public final StringProperty templateProperty() {
        return this.template;
    }

    public final String getTemplate() {
        return this.templateProperty().get();
    }

    public final void setTemplate(final String category) {
        this.templateProperty().set(category);
    }

    public final StringProperty sponsorProperty() {
        return this.sponsor;
    }

    public final String getSponsor() {
        return this.sponsorProperty().get();
    }

    public final void setSponsor(final String sponsor) {
        this.sponsorProperty().set(sponsor);
    }

    public StringProperty botProperty() {
    	return this.bot;
    }
   
    public final String getBot() {
        return this.botProperty().get();
    }

    public final void setBot(final String bot) {
        this.botProperty().set(bot);
    }
    
    public StringProperty settlerProperty() {
    	return this.settler;
    }
   
    public final String getSettler() {
        return this.settlerProperty().get();
    }

    public final void setSettler(final String settler) {
        this.settlerProperty().set(settler);
    }
    
    public StringProperty latitudeProperty() {
    	return this.latitude;
    }
   
    public final String getLatitude() {
        return this.latitudeProperty().get();
    }

    public final void setLatitude(final String latitude) {
        this.latitudeProperty().set(latitude);
    }
    
    public StringProperty longitudeProperty() {
    	return this.longitude;
    }
   
    public final String getLongitude() {
        return this.longitudeProperty().get();
    }

    public final void setLongitude(final String longitude) {
        this.longitudeProperty().set(longitude);
    }
    
}