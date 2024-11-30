/*
 * Mars Simulation Project
 * ResupplyManifest.java
 * @date 2024-12-01
 * @author Barry Evans
 */
package com.mars_sim.core.interplanetary.transport.resupply;

import java.io.Serializable;

import com.mars_sim.core.structure.SettlementSupplies;

/**
 * Definition of a Supply Manifest used in a resupply mission.
 */
public class ResupplyManifest implements Serializable  {
    private String name;
    private int people;
    private SettlementSupplies supplies;

    public ResupplyManifest(String name, int people, SettlementSupplies supplies) {
        this.name = name;
        this.people = people;
        this.supplies = supplies;
    }

    public String getName() {
        return name;
    }

    public int getPeople() {
        return people;
    }

    public SettlementSupplies getSupplies() {
        return supplies;
    }
}