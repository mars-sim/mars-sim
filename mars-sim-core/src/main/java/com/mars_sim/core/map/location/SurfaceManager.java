/*
 * Mars Simulation Project
 * SurfaceManager.java
 * @date 2024-10-02
 * @author Barry Evans
 */
package com.mars_sim.core.map.location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class manages a collection of SurfaceFeatures on the surface of Mars.
 * These are held in a tiled internal structure based on the Coordinates
 * giving it optimised lookup on Coordinate.
 */
public class SurfaceManager<T extends SurfacePOI> implements Serializable {

    private static final int LATITUDE_SLICES = 6;
    private static final double LATITUDE_RANGE = Math.PI/LATITUDE_SLICES;

    @SuppressWarnings("unchecked")
    private List<T>[] slices = new List[LATITUDE_SLICES];

    public SurfaceManager() {
        Arrays.setAll(slices, element -> new ArrayList<>());
    }

    public void addFeature(T newFeature) {
        // select the best collection 
        int subId = getSlice(newFeature.getLocation().getPhi());
        slices[subId].add(newFeature);
    }

    /**
     * Select the best slicer based on the Coordinate. This is based on the Latitude (phi)
     * @param phi Lantitude phi vlaue
     * @return
     */
    private int getSlice(double phi) {
        int idx = (int)(phi/LATITUDE_RANGE);
        if (idx < 0) {
            return 0;
        }
        else if (idx >= slices.length) {
            return slices.length-1;
        }
        return idx;
    }

    /**
     * Get a feature at a specific location.
     * @param newLocation
     * @return
     */
    public T getFeature(Coordinates newLocation) {
        var top = getSlice(newLocation.getPhi());
        return slices[top].stream()
                .filter(c -> c.getLocation().equals(newLocation))
                .findFirst().orElse(null);
    }

    /**
     * Find all features within a range from a center location.
     * @param center Center point
     * @param arcAngle 
     * @return
     */
    public List<T> getFeatures(Coordinates center, double arcAngle) {
        // Find the source the sublists based on the 4 points
        var top = getSlice(center.getPhi() + arcAngle);
        var bottom = getSlice(center.getPhi() - arcAngle);

        List<T> result = new ArrayList<>();
        for(int i = bottom; i <= top; i++) {
            result.addAll(
                slices[i].stream()
                    .filter(c -> center.getAngle(c.getLocation()) <= arcAngle)
                    .toList()
            );
        }

        return result;
    }

    /**
     * Get the distribution of details per slice. 
     * @return Array of the number of items in each slice
     */
    public int[] getStats() {
        int[] result = new int[slices.length];
        for(int i = 0; i < slices.length; i++) {
            result[i] = slices[i].size();
        }
        return result;
    }
}
