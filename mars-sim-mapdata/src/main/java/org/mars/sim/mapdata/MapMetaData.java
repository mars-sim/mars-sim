/*
 * Mars Simulation Project
 * MapMetaData.java
 * @date 2023-08-02
 * @author Barry Evans
 */

package org.mars.sim.mapdata;

import java.util.HashMap;
import java.util.Map;

import org.mars.sim.mapdata.common.FileLocator;

public class MapMetaData {
	
    private boolean colourful;
    
    /** The selected resolution of the map file. */
	private int res = 0;
	
	private String mapString;
    private String name;
    private String hiResFilename;
    private String midResFilename;
    private String loResFilename;
    
    private Map<String, Boolean> locallyAvailableMap = new HashMap<>();
    
    public MapMetaData(String mapString, String name, boolean colourful,
                        String hiResFilename, String midResFilename, String loResFilename) {
        this.mapString = mapString;
        this.name = name;
        this.colourful = colourful;
        this.hiResFilename = hiResFilename;
        this.midResFilename = midResFilename;
        this.loResFilename = loResFilename;
        
        checkMapLocalAvailability();
    }

    /**
     * Checks if the maps are available locally.
     */
    public void checkMapLocalAvailability() {
		boolean loaded0 = FileLocator.isLocallyAvailable(hiResFilename);
		boolean loaded1 = FileLocator.isLocallyAvailable(midResFilename);
		boolean loaded2 = FileLocator.isLocallyAvailable(loResFilename);
		
        locallyAvailableMap.put(hiResFilename, loaded0);
        locallyAvailableMap.put(midResFilename, loaded1);
        locallyAvailableMap.put(loResFilename, loaded2);
        
		// Sync up with the resolution data member
		if (loaded0)
			setResolution(0);
		else if (loaded1)
			setResolution(1);
		else if (loaded2)
			setResolution(2);
    }
    
    public String getMapString() {
        return mapString;
    }

    public String getName() {
        return name;
    }

    /**
     * Sets if the file is locally available.
     * 
     * @param newValue
     */
    void setLocallyAvailable(boolean newValue) {
    	String fileName = getFile();
        locallyAvailableMap.put(fileName, newValue);
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
     * Is the map file locally available.
     * 
     * @param resolution
     * @return
     */
    public boolean isLocallyAvailable(int resolution) {
    	String fileName = getFile(resolution);
        return locallyAvailableMap.get(fileName);
    }

    /**
     * Is the map file locally available.
     * 
     * @return
     */
    public boolean isLocallyAvailable() {
        return locallyAvailableMap.get(getFile());
    }
    
    public boolean isColourful() {
        return colourful;
    }

    /**
     * Gets the filename.
     * 
     * @param res
     * @return
     */
    public String getFile(int res) {
    	if (res == 0) {
    		return getLoResFile();
    	}
    	else if (res == 1) {
    		return getMidResFile();
    	}

    	return getHiResFile();
    }
    
    /**
     * Gets the filename.
     * 
     * @return
     */
    public String getFile() {
    	return getFile(res);
    }
    
    String getHiResFile() {
        return hiResFilename;
    }

    String getMidResFile() {
        return midResFilename;
    }
    
    public String getLoResFile() {
        return loResFilename;
    }

	public void destroy() {
		locallyAvailableMap.clear();
		locallyAvailableMap = null;
	}
}
