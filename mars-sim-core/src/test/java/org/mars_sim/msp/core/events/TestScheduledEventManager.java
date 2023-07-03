package org.mars_sim.msp.core.events;

import java.util.Collection;

import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.events.ScheduledEventManager.ScheduledEvent;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MasterClock;

public class TestScheduledEventManager extends AbstractMarsSimUnitTest  {

	private static class TestHandler implements ScheduledEventHandler {

		private static final long serialVersionUID = 1L;
		
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
		public int execute(MarsTime now) {
			counter++;
			return repeatDuration;
		}

	}

	private MasterClock clock;
	private ScheduledEventManager mgr;
	
    @Override
    public void setUp() {
		super.setUp();
		clock = sim.getMasterClock();

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
		MarsTime base = clock.getMarsTime();
		for(ScheduledEvent event : events) {
			assertEquals("Event #" + eventCounter, "Handler " + eventCounter, event.getDescription());
			MarsTime eventTime = base.addTime(durations[eventCounter]);
			assertEquals("When Event #" + eventCounter, eventTime, event.getWhen());
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
		MarsTime eventWhen = clock.getMarsTime().addTime(100);
		for(ScheduledEvent event : events) {
			assertEquals("When Event #" + eventCounter, eventWhen, event.getWhen());
			eventCounter++;
		}

		mgr.timePassing(createPulse(eventWhen, false));
		assertEquals("Execution count of concurrent events", 2, handler.counter);
		assertEquals("All events executed", 0, events.size());
    }


    public void testOneOffEvent() {
		int duration = 100;

		// No events
		MarsTime now = sim.getMasterClock().getMarsTime();
		mgr.timePassing(createPulse(now, false));

		// Add handler in different not time order
		TestHandler handler = new TestHandler("Handler 0", 0);
		mgr.addEvent(duration, handler);

		// Move clock forard but not past event
		now = now.addTime(duration/2);
		mgr.timePassing(createPulse(now, false));
		Collection<ScheduledEvent> events = mgr.getEvents();
		assertEquals("Events still queued", 1, events.size());
    	
		// Move clock forard but  past event
		now = now.addTime(duration);
		mgr.timePassing(createPulse(now, false));
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
		MarsTime eventTime = clock.getMarsTime().addTime(duration/2);
		mgr.timePassing(createPulse(eventTime, false));
		Collection<ScheduledEvent> events = mgr.getEvents();
		assertEquals("Events still queued", 1, events.size());
    	
		// Move clock forward but past event
		for(int count = 1; count < 4; count++) {
			eventTime = eventTime.addTime(duration);
			mgr.timePassing(createPulse(eventTime, false));
			events = mgr.getEvents();
			assertEquals("Repeat event queued", 1, events.size());
			assertEquals("Handler executuon count", count, handler.counter);
		}
    }
}
   