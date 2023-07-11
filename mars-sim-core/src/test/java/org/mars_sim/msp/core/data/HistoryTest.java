package org.mars_sim.msp.core.data;

import java.util.List;

import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.data.History.HistoryItem;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MasterClock;

public class HistoryTest extends AbstractMarsSimUnitTest {
    
    public void testAdd() {
        History<Integer> h = new History<>();
        MasterClock master = sim.getMasterClock();
        
        MarsTime firstTime = master.getMarsTime();
        h.add(1);

        MarsTime secondTime = firstTime.addTime(10);
        master.setMarsTime(secondTime);
        h.add(2);

        List<HistoryItem<Integer>> changes = h.getChanges();
        assertEquals("Histoy size", 2, changes.size());
        assertEquals("1st time", firstTime, changes.get(0).getWhen());
        assertEquals("1st value", Integer.valueOf(1), changes.get(0).getWhat());

        assertEquals("2nd time", secondTime, changes.get(1).getWhen());
        assertEquals("2nd value", Integer.valueOf(2), changes.get(1).getWhat());

    }

    public void testAddSameTime() {
        History<Integer> h = new History<>();
        MasterClock master = sim.getMasterClock();
        
        MarsTime firstTime = master.getMarsTime();
        h.add(1);
        h.add(2);

        List<HistoryItem<Integer>> changes = h.getChanges();
        assertEquals("History size", 1, changes.size());
        assertEquals("Last time", firstTime, changes.get(0).getWhen());
        assertEquals("Last value", Integer.valueOf(2), changes.get(0).getWhat());
    }

        public void testAddSameValue() {
        History<Integer> h = new History<>();
        MasterClock master = sim.getMasterClock();
        
        MarsTime firstTime = master.getMarsTime();
        h.add(1);

        MarsTime secondTime = firstTime.addTime(10);
        master.setMarsTime(secondTime);
        h.add(1);

        List<HistoryItem<Integer>> changes = h.getChanges();
        assertEquals("History size", 1, changes.size());
        assertEquals("Last time", firstTime, changes.get(0).getWhen());
        assertEquals("Last value", Integer.valueOf(1), changes.get(0).getWhat());
    }
}
