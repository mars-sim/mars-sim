package com.mars_sim.core.data;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.data.History.HistoryItem;
import com.mars_sim.core.time.MarsDate;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;

public class HistoryTest extends MarsSimUnitTest {
    
    @Test
    public void testAdd() {
        History<Integer> h = new History<>();
        MasterClock master = getSim().getMasterClock();
        
        MarsTime firstTime = master.getMarsTime();
        h.add(1);

        MarsTime secondTime = firstTime.addTime(10);
        master.setMarsTime(secondTime);
        h.add(2);

        List<HistoryItem<Integer>> changes = h.getChanges();
        assertEquals(2, changes.size(), "Histoy size");
        assertEquals(firstTime, changes.get(0).getWhen(), "1st time");
        assertEquals(Integer.valueOf(1), changes.get(0).getWhat(), "1st value");

        assertEquals(secondTime, changes.get(1).getWhen(), "2nd time");
        assertEquals(Integer.valueOf(2), changes.get(1).getWhat(), "2nd value");

    }

    @Test
    public void testRange() {
        History<Integer> h = new History<>();
        MasterClock master = getSim().getMasterClock();
        
        MarsDate firstDate = master.getMarsTime().getDate();
        h.add(1);
        master.setMarsTime(master.getMarsTime().addTime(10));
        h.add(2);
        master.setMarsTime(master.getMarsTime().addTime(10));
        h.add(3);

        master.setMarsTime(master.getMarsTime().addTime(1000D));
        MarsDate middleDate = master.getMarsTime().getDate();
        h.add(4);

        master.setMarsTime(master.getMarsTime().addTime(2000D));
        h.add(5);

        master.setMarsTime(master.getMarsTime().addTime(2D));
        MarsDate lastDate = master.getMarsTime().getDate();
        h.add(6);
        

        List<MarsDate> range = h.getRange();
        assertEquals(3, range.size(), "Range size");
        assertEquals(firstDate, range.get(0), "1st date");
        assertEquals(middleDate, range.get(1), "2nd date");
        assertEquals(lastDate, range.get(2), "3rd date");
    }

    @Test
    public void testAddSameTime() {
        History<Integer> h = new History<>();
        MasterClock master = getSim().getMasterClock();
        
        MarsTime firstTime = master.getMarsTime();
        h.add(1);
        h.add(2);

        List<HistoryItem<Integer>> changes = h.getChanges();
        assertEquals(1, changes.size(), "History size");
        assertEquals(firstTime, changes.get(0).getWhen(), "Last time");
        assertEquals(Integer.valueOf(2), changes.get(0).getWhat(), "Last value");
    }

    @Test
    public void testAddSameValue() {
        History<Integer> h = new History<>();
        MasterClock master = getSim().getMasterClock();
        
        MarsTime firstTime = master.getMarsTime();
        h.add(1);

        MarsTime secondTime = firstTime.addTime(10);
        master.setMarsTime(secondTime);
        h.add(1);

        List<HistoryItem<Integer>> changes = h.getChanges();
        assertEquals(1, changes.size(), "History size");
        assertEquals(firstTime, changes.get(0).getWhen(), "Last time");
        assertEquals(Integer.valueOf(1), changes.get(0).getWhat(), "Last value");
    }
}
