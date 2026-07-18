package com.mars_sim.core.person.ai.task.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityIdentifier;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

class SettlementTaskTest extends MarsSimUnitTest {

	private static class TestMetaTask extends MetaTask implements SettlementMetaTask {

		private TestMetaTask(String name) {
			super(name, WorkerType.BOTH, TaskScope.ANY_HOUR);
		}

		@Override
		public List<SettlementTask> getSettlementTasks(Settlement settlement) {
			return Collections.emptyList();
		}
	}

	private static class TestEntity implements Entity {

		private static final long serialVersionUID = 1L;

		private final String name;
		private final EntityIdentifier id;

		private TestEntity(String name, String id) {
			this.name = name;
			this.id = new EntityIdentifier("test", id);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getContext() {
			return "test";
		}

		@Override
		public EntityIdentifier getEntityIdentifier() {
			return id;
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof TestEntity other))
				return false;
			return id.equals(other.id);
		}
	}

	@Test
	void testEqualsTrueWithSameKeyAttributesAndDifferentScoreAndDemand() {
		Settlement owner = buildSettlement("owner");
		TestMetaTask meta = new TestMetaTask("Meta");
		TestEntity focus = new TestEntity("Focus", "focus-1");

		MockSettlementTask first = new MockSettlementTask(meta, owner, "Task A", focus, new RatingScore(10));
		first.updateDemand(1);

		MockSettlementTask second = new MockSettlementTask(meta, owner, "Task B", focus, new RatingScore(500));
		second.updateDemand(7);

		assertEquals(first, second, "Tasks should be equal when owner, meta task and focus are the same");
		assertEquals(second, first, "Equality should be symmetric");
	}

	@Test
	void testEqualsFalseWhenOwnerDifferent() {
		Settlement owner1 = buildSettlement("owner1");
		Settlement owner2 = buildSettlement("owner2");
		TestMetaTask meta = new TestMetaTask("Meta");
		TestEntity focus = new TestEntity("Focus", "focus-1");

		SettlementTask first = new MockSettlementTask(meta, owner1, "Task", focus, new RatingScore(10));
		SettlementTask second = new MockSettlementTask(meta, owner2, "Task", focus, new RatingScore(10));

		assertNotEquals(first, second, "Tasks should not be equal when owners are different");
	}
	
	@Test
	void testEqualsFalseWhenFocusDifferent() {
		Settlement owner = buildSettlement("owner");
		TestMetaTask meta = new TestMetaTask("Meta");
		TestEntity focus1 = new TestEntity("Focus", "focus-1");
		TestEntity focus2 = null; // Different focus (null vs non-null)

		SettlementTask first = new MockSettlementTask(meta, owner, "Task", focus1, new RatingScore(10));
		SettlementTask second = new MockSettlementTask(meta, owner, "Task", focus2, new RatingScore(10));

		assertNotEquals(first, second, "Tasks should not be equal when focus is different");
	}

    
	@Test
	void testEqualsNoFocus() {
		Settlement owner = buildSettlement("owner");
		TestMetaTask meta = new TestMetaTask("Meta");

		SettlementTask first = new MockSettlementTask(meta, owner, "Task", null, new RatingScore(10));
		SettlementTask second = new MockSettlementTask(meta, owner, "Task", null, new RatingScore(10));

		assertEquals(first, second, "Tasks should be equal when focus is null for both");
	}
}
