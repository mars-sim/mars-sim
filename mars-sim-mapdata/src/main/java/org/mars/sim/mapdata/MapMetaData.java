/*
 * Mars Simulation Project
 * MapMetaData.java
 * @date 2023-07-26
 * @author Barry Evans
 */

package org.mars.sim.mapdata;

import java.util.HashMap;
import java.util.Map;

import org.mars.sim.mapdata.common.FileLocator;

public class MapMetaData {
	
//    private boolean locallyAvailable;
    private boolean colourful;
    
	private int selectedResolution = 2;
	
	private String id;
    private String name;
    private String hiResFilename;
    private String midResFilename;
    private String loResFilename;
    
    private Map<String, Boolean> locallyAvailableMap = new HashMap<>();
    
    public MapMetaData(String id, String name, boolean colourful,
                        String hiResFilename, String midResFilename, String loResFilename) {
        this.id = id;
        this.name = name;
        this.colourful = colourful;
        this.hiResFilename = hiResFilename;
        this.midResFilename = midResFilename;
        this.loResFilename = loResFilename;
        
        checkMapLocalAvailability();
    }

    public void checkMapLocalAvailability() {
        locallyAvailableMap.put(hiResFilename, FileLocator.isLocallyAvailable(hiResFilename));
        locallyAvailableMap.put(midResFilename, FileLocator.isLocallyAvailable(midResFilename));
        locallyAvailableMap.put(loResFilename, FileLocator.isLocallyAvailable(loResFilename));
    }
    
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    void setLocallyAvailable(boolean newValue) {
    	String fileName = getFile();
        locallyAvailableMap.put(fileName, newValue);
    }

    public void setResolution(int selected) {
    	selectedResolution = selected;
    }
    
    public int getResolution() {
    	return selectedResolution;
    }
    
    public boolean isLocallyAvailable(int resolution) {
    	String fileName = getFile(resolution);
        return locallyAvailableMap.get(fileName);
    }

    public boolean isLocallyAvailable() {
        return locallyAvailableMap.get(getFile());
    }
    
    public boolean isColourful() {
        return colourful;
    }

    public String getFile(int selectedResolution) {
    	if (selectedResolution == 0) {
    		return getHiResFile();
    	}
    	else if (selectedResolution == 1) {
    		return getMidResFile();
    	}
//    	else if (selectedResolution == 2) {
//    		return getLoResFile();
//    	}
    	
    	return getLoResFile();
    }
    
    public String getFile() {
    	if (selectedResolution == 0) {
    		return getHiResFile();
    	}
    	else if (selectedResolution == 1) {
    		return getMidResFile();
    	}
  
    	return getLoResFile();
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

}
