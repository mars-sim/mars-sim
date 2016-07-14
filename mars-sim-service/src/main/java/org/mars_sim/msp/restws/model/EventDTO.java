package org.mars_sim.msp.restws.model;

public class EventDTO {

	private int id;
	private String name;
	private String description;
	private String timestamp;
	private EntityReference source;
	private String sourceType;
	
	public EventDTO(int id, String name, String description, String timestamp, EntityReference source, String sourceType) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.timestamp = timestamp;
		this.source = source;
		this.sourceType = sourceType;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public EntityReference getSource() {
		return source;
	}

	public String getSourceType() {
		return sourceType;
	}

	public int getId() {
		return id;
	}
}
