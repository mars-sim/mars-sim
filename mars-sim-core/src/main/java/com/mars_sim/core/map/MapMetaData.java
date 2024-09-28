/*
 * Mars Simulation Project
 * MapMetaData.java
 * @date 2023-08-02
 * @author Barry Evans
 */

package com.mars_sim.core.map;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.map.common.FileLocator;

public class MapMetaData {
    private class Resolution{
        private boolean locallyAvailable;
        private String filename;

        Resolution(String filename, boolean locallyAvailable) {
            this.filename = filename;
            this.locallyAvailable = locallyAvailable;
        }

        void setLocal() {
            locallyAvailable = true;
        }

        String getFilename() {
            return filename;
        }

        boolean isLocal() {
            return locallyAvailable;
        }
    }

    private boolean colourful = true;
	private String mapString;
    private String mapType;
    
    private List<Resolution> listOfMaps;
    
    public MapMetaData(String mapString, String mapType, boolean colourful, List<String> array) {

        this.mapString = mapString;
        this.mapType = mapType;
        this.colourful = colourful;
        this.listOfMaps = new ArrayList<>();
        for(var a : array) {
            var locally = FileLocator.isLocallyAvailable(MapDataFactory.MAPS_FOLDER + a);
            listOfMaps.add(new Resolution(a, locally));
        }
    }
    
    public String getMapString() {
        return mapString;
    }

    public String getMapType() {
        return mapType;
    }

    /**
     * Gets the available number of level of resolution.
     * 
     * @return
     */
    public int getNumLevel() {
    	return listOfMaps.size();
    }
    
    /**
     * Is this a color map ? 
     * 
     * @return
     */
    public boolean isColourful() {
        return colourful;
    }

    /**
     * Callback from the MapData that is is not loaded locally
     * @param res
     */
    void setLocallyAvailable(int res) {
        listOfMaps.get(res).setLocal();
    }

    /**
     * Is the resolution loaded locally
     * @param newRes
     * @return
     */
    public boolean isLocal(int newRes) {
        return listOfMaps.get(newRes).isLocal();
    }

    /**
     * Gets the filename that represents a resolution layer of this map
     * 
     * @param newRes
     * @return
     */
    public String getFile(int newRes) {
        return listOfMaps.get(newRes).getFilename();
    }
    
	/**
	 * Compares if an object is the same as this unit
	 *
	 * @param obj
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		return this.mapString == ((MapMetaData)obj).getMapString();
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		return mapType.hashCode() * mapString.hashCode();
	}
}
