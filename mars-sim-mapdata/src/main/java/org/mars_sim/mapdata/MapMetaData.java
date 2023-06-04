/*
 * Mars Simulation Project
 * MapMetaData.java
 * @date 2023-06-03
 * @author Barry Evans
 */

package org.mars_sim.mapdata;

public class MapMetaData {
	private String id;
    private String name;
    private boolean locallyAvailable;
    private boolean colourful;
    private String hiResFilename;
    private String loResFilename;
    
    public MapMetaData(String id, String name, boolean locallyAvailable, boolean colourful,
                        String hiResFilename, String loResFilename) {
        this.id = id;
        this.name = name;
        this.locallyAvailable = locallyAvailable;
        this.colourful = colourful;
        this.hiResFilename = hiResFilename;
        this.loResFilename = loResFilename;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    void setLocallyAvailable(boolean newValue) {
        locallyAvailable = newValue;
    }

    public boolean isLocallyAvailable() {
        return locallyAvailable;
    }

    public boolean isColourful() {
        return colourful;
    }

    String getHiResFile() {
        return hiResFilename;
    }

    public String getLoResFile() {
        return loResFilename;
    }

}
