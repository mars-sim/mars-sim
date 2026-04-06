/*
 * Mars Simulation Project
 * MapMetaData.java
 * @date 2023-08-02
 * @author Barry Evans
 */

package com.mars_sim.core.map;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mars_sim.core.map.common.FileLocator;

/**
 * Represents the meta data associated with a type of map. It contains a "stack" of different 
 * resolution images.
 */
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
	private String id;
    private String description;
    
    private List<Resolution> listOfMaps;
    
    MapMetaData(String id, String description, boolean colourful, List<String> array) {

        this.id = id;
        this.description = description;
        this.colourful = colourful;
        this.listOfMaps = new ArrayList<>();
        for(var a : array) {
            var locally = FileLocator.isLocallyAvailable(MapDataFactory.MAPS_FOLDER + a);
            listOfMaps.add(new Resolution(a, locally));
        }
    }
    
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
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
     * Gets the map data associate with a particular resolution. This may load async.
     * 
     * @param newRes the resolution level for the map data on thos "family"
     * @param callback the callback to notify when the map data is loaded
     * @return
     */
    public MapData getData(int newRes, Consumer<MapData> callback) {
        var filename = listOfMaps.get(newRes).getFilename();

        return MapDataFactory.loadMapData(this, newRes, filename, callback);
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
		return this.id == ((MapMetaData)obj).getId();
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		return description.hashCode() * id.hashCode();
	}
}
