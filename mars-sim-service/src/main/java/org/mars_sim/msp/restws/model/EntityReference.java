package org.mars_sim.msp.restws.model;

/**
 * Reference to an entity of the simulation.
 */
public class EntityReference {
	private int id;
	private String name;

	public EntityReference() {
		
	}
	
	public EntityReference(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
