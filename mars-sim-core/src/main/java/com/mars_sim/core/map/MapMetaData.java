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
    
    /** The selected resolution of the map file. */
	private int res = 0;
    /** The available number of resolution level for this map type. */
	private int numLevel = 1;
	
	private String mapString;
    private String mapType;
    
    private List<Resolution> listOfMaps;
    
    public MapMetaData(String mapString, String mapType, boolean colourful, List<String> array) {

        this.mapString = mapString;
        this.mapType = mapType;
        this.colourful = colourful;
        this.listOfMaps = new ArrayList<>();
        for(var a : array) {
            var locally = FileLocator.isLocallyAvailable(a);
            listOfMaps.add(new Resolution(a, locally));
        }
  
        numLevel = listOfMaps.size();
        setResolution(0);
    }
    
    public String getMapString() {
        return mapString;
    }

    public String getMapType() {
        return mapType;
    }

    /**
     * Sets if the file is locally available.
     * 
     * @param newValue
     */
    void setLocallyAvailable() {
        listOfMaps.get(res).setLocal();
    }

    /**
     * Sets the resolution of the map file.
     * 
     * @param selected
     */
    public void setResolution(int selected) {
    	res = selected;
    }
    
    /**
     * Gets the resolution of the map file.
     * 
     * @return
     */
    public int getResolution() {
    	return res;
    }
    
    /**
     * Gets the available number of level of resolution.
     * 
     * @return
     */
    public int getNumLevel() {
    	return numLevel;
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
     * Is the resolution loaded locally
     * @param newRes
     * @return
     */
    public boolean isLocal(int newRes) {
        if (newRes >= listOfMaps.size()) {
            newRes = listOfMaps.size()-1;
        }

        return listOfMaps.get(newRes).isLocal();
    }

    /**
     * Gets the filename.
     * 
     * @param newRes
     * @return
     */
    public String getFile(int newRes) {
        if (newRes >= listOfMaps.size()) {
            newRes = listOfMaps.size()-1;
        }
        this.res = newRes;
        return listOfMaps.get(res).getFilename();
    }
    
    /**
     * Gets the filename.
     * 
     * @return
     */
    public String getFile() {
    	return getFile(res);
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
