package com.mars_sim.core.resource;

import java.util.HashMap;
import java.util.Map;

public class ResourceManager {

    private static final int RESOURCE_UPDATE_INTERVAL = 60; // Update every 60 game ticks (1 minute of in-game time)
    private Map<String, Resource> resources = new HashMap<>();
    private int tickCounter = 0;

    public ResourceManager() {
        // Initialize resources
        resources.put("Oxygen", new Resource("Oxygen", 1000, 0));  // Example: 1000 units of Oxygen at 0 consumption rate
        resources.put("Water", new Resource("Water", 1000, 0));
        resources.put("Food", new Resource("Food", 500, 0));
    }

    public void update() {
        tickCounter++;

        // Only update resources every RESOURCE_UPDATE_INTERVAL ticks
        if (tickCounter >= RESOURCE_UPDATE_INTERVAL) {
            tickCounter = 0;  // Reset counter after update

            // Iterate over resources and update only if necessary
            for (Resource resource : resources.values()) {
                if (resource.isConsumptionThresholdExceeded()) {
                    resource.updateResourceState();
                }
            }
        }
    }

    // Method to update resource consumption based on time intervals and threshold checks
    public void addResource(String resourceName, double amount) {
        Resource resource = resources.get(resourceName);
        if (resource != null) {
            resource.add(amount);
        }
    }

    public void setResourceConsumptionRate(String resourceName, double rate) {
        Resource resource = resources.get(resourceName);
        if (resource != null) {
            resource.setConsumptionRate(rate);
        }
    }

    // Resource class encapsulating individual resource data
    public static class Resource {
        private String name;
        private double currentAmount;
        private double consumptionRate;
        private double threshold;

        public Resource(String name, double initialAmount, double consumptionRate) {
            this.name = name;
            this.currentAmount = initialAmount;
            this.consumptionRate = consumptionRate;
            this.threshold = 0.1 * initialAmount; // Default threshold is 10% of initial amount
        }

        public void updateResourceState() {
            // Check if resource needs to be consumed
            if (currentAmount > 0) {
                currentAmount -= consumptionRate;
            }
            if (currentAmount < 0) {
                currentAmount = 0;
            }
            System.out.println(name + " updated: " + currentAmount);
        }

        public void add(double amount) {
            currentAmount += amount;
            if (currentAmount > 1000) { // Cap max resource amount (for example, 1000 units max)
                currentAmount = 1000;
            }
        }

        public void setConsumptionRate(double rate) {
            this.consumptionRate = rate;
        }

        // Only update when consumption exceeds the threshold
        public boolean isConsumptionThresholdExceeded() {
            return currentAmount < threshold;
        }
    }
}
