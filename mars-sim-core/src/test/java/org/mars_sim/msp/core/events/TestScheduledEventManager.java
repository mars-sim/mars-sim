package org.mars_sim.msp.core.events;

import java.util.Collection;

import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.events.ScheduledEventManager.ScheduledEvent;
import org.mars_sim.msp.core.time.MarsClock;

public class TestScheduledEventManager extends AbstractMarsSimUnitTest  {

	private static class TestHandler implements ScheduledEventHandler {

		private String description;
		private int counter = 0;
		private int repeatDuration;

		

		public TestHandler(String description, int repeatDuration) {
			this.description = description;
			this.repeatDuration = repeatDuration;
		}

		@Override
		public String getEventDescription() {
			return description;
		}

		@Override
		public int execute() {
			counter++;
			return repeatDuration;
		}

	}

	private MarsClock clock;
	private ScheduledEventManager mgr;
	
    @Override
    public void setUp() {
		super.setUp();
		clock = sim.getMasterClock().getMarsClock();

		mgr = new ScheduledEventManager(clock);
    }
    

    public void testAddingEvents() {
		int [] durations = {100, 300, 500, 800};

		// Add handler in different not time order
		mgr.addEvent(durations[3], new TestHandler("Handler 3", 0));
		mgr.addEvent(durations[0], new TestHandler("Handler 0", 0));
		mgr.addEvent(durations[2], new TestHandler("Handler 2", 0));
		mgr.addEvent(durations[1], new TestHandler("Handler 1", 0));

		Collection<ScheduledEvent> events = mgr.getEvents();
		assertEquals("All events queued", durations.length, events.size());
    	
		// Check order
		int eventCounter = 0;
		for(ScheduledEvent event : events) {
			assertEquals("Event #" + eventCounter, "Handler " + eventCounter, event.getDescription());
			MarsClock eventWhen = new MarsClock(clock);
			eventWhen.addTime(durations[eventCounter]);
			assertEquals("When Event #" + eventCounter, eventWhen, event.getWhen());
			eventCounter++;
		}
    }

	public void testSameTimeEvents() {

		// Add handler in different not time order
		TestHandler handler = new TestHandler("Handler", 0);
		mgr.addEvent(100, handler);
		mgr.addEvent(100, handler);

		Collection<ScheduledEvent> events = mgr.getEvents();
		assertEquals("All events queued", 2, events.size());
    	
		// Check order
		int eventCounter = 0;
		for(ScheduledEvent event : events) {
			MarsClock eventWhen = new MarsClock(clock);
			eventWhen.addTime(100);
			assertEquals("When Event #" + eventCounter, eventWhen, event.getWhen());
			eventCounter++;
		}

		mgr.timePassing(createPulse(100));
		assertEquals("Execution count of concurrent events", 2, handler.counter);
		assertEquals("All events executed", 0, events.size());
    }


    public void testOneOffEvent() {
		int duration = 100;

		// No events
		mgr.timePassing(createPulse(1));

		// Add handler in different not time order
		TestHandler handler = new TestHandler("Handler 0", 0);
		mgr.addEvent(duration, handler);

		// Move clock forard but not past event
		mgr.timePassing(createPulse(duration/2));
		Collection<ScheduledEvent> events = mgr.getEvents();
		assertEquals("Events still queued", 1, events.size());
    	
		// Move clock forard but  past event
		mgr.timePassing(createPulse(duration));
		events = mgr.getEvents();
		assertTrue("All event queue empty", events.isEmpty());
		assertEquals("Handler executuon count", 1, handler.counter);
    }

	public void testRepeatingEvent() {
		int duration = 100;

		// Add handler in different not time order
		TestHandler handler = new TestHandler("Handler 0", duration);
		mgr.addEvent(duration, handler);

		// Move clock forard but not past event
		clock.addTime(duration/2);
		mgr.timePassing(createPulse(clock, false));
		Collection<ScheduledEvent> events = mgr.getEvents();
		assertEquals("Events still queued", 1, events.size());
    	
		// Move clock forward but past event
		for(int count = 1; count < 4; count++) {
			clock.addTime(duration);
			mgr.timePassing(createPulse(clock, false));
			events = mgr.getEvents();
			assertEquals("Repeat event queued", 1, events.size());
			assertEquals("Handler executuon count", count, handler.counter);
		}
    }
}
   