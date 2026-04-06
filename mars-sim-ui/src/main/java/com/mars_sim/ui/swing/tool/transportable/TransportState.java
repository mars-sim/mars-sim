/*
 * Mars Simulation Project
 * TransportState.java
 * @date 2026-03-03
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.transportable;

import com.mars_sim.core.interplanetary.transport.resupply.ResupplyManifest;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

/**
 * Shared state for the TransportableWizard.
 */
class TransportState {
	private TransportableType type;

	private String name;
	private MarsTime arrivalDate;

	private int arrivingPopulation = 4;
	private int arrivingRobots = 2;
	private Settlement landingSettlement;
    private ResupplyManifest manifest;

    // Arriving settlement attributes
    private String arrivingTemplate;
	private String arrivingSponsor;
    private Coordinates landingSite;

    public void setLandingSettlement(Settlement settlement) {
        this.landingSettlement = settlement;
    }

    public void setType(TransportableType selectedItem) {
        type = selectedItem;
    }

    public void setName(String string) {
        name = string;
    }

    public void setArrivalDate(MarsTime marsTime) {
        arrivalDate = marsTime;
    }

    public void setArrivingSponsor(String selectedItem) {
        this.arrivingSponsor = selectedItem;
    }

    public void setArrivingTemplate(String selectedItem) {
        this.arrivingTemplate = selectedItem;
    }

    public void setPopulation(Integer value) {
        this.arrivingPopulation = value;    
    }

    public void setRobots(Integer value) {
        this.arrivingRobots = value;
    }

    public void setLandingSite(Coordinates coordinates) {
        this.landingSite = coordinates;
    }

    public void setManifest(ResupplyManifest selectedItem) {
        this.manifest = selectedItem;
    }

    public TransportableType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public MarsTime getArrivalDate() {
        return arrivalDate;
    }

    public Settlement getLandingSettlement() {
        return landingSettlement;
    }

    public ResupplyManifest getManifest() {
        return manifest;
    }

    public int getArrivingRobots() {
        return arrivingRobots;
    }

    public int getArrivingPopulation() {
        return arrivingPopulation;
    }

    public Coordinates getLandingSite() {
        return landingSite;
    }

    public String getArrivingSponsor() {
        return arrivingSponsor;
    }

    public String getArrivingTemplate() {
        return arrivingTemplate;
    }
}
