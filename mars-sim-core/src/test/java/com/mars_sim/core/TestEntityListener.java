package com.mars_sim.core;

public class TestEntityListener implements EntityListener {

    private String eventFilter;
    private Entity lastSource;
    private int eventsReceived = 0;

    public TestEntityListener(String eventFilter) {
        this.eventFilter = eventFilter;
    }

    public int getEventsReceived() {
        return eventsReceived;
    }

    public Entity getLastSource() {
        return lastSource;
    }

    @Override
    public void entityUpdate(EntityEvent event) {
        if ((eventFilter == null) || event.getType().equals(eventFilter)) {
            eventsReceived++;
            lastSource = event.getSource();
        }
    }
}
