package com.mars_sim.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestEntityListener implements EntityListener {

    private Set<String> eventFilter;
    private int eventsReceived = 0;
    private EntityEvent lastEvent;

    public TestEntityListener(String... eventFilter) {
        this.eventFilter = new HashSet<>(Arrays.asList(eventFilter));
    }

    public int getEventsReceived() {
        return eventsReceived;
    }

    public Entity getLastSource() {
        return lastEvent != null ? lastEvent.getSource() : null;
    }

    public Object getLastTarget() {
        return lastEvent != null ? lastEvent.getTarget() : null;
    }

    public String getLastType() {
        return lastEvent != null ? lastEvent.getType() : null;
    }

    @Override
    public void entityUpdate(EntityEvent event) {
        if (eventFilter.isEmpty() || eventFilter.contains(event.getType())) {
            eventsReceived++;
            lastEvent = event;
        }
    }
}
