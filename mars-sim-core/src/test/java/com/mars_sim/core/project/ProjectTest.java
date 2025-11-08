package com.mars_sim.core.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.person.ai.task.util.Worker;

public class ProjectTest {
    @SuppressWarnings("serial")
	static final class TestStep extends ProjectStep {

        int expectedCount = 0;
        int startCount = 0;
        int endCount = 0;

        TestStep(Stage stage, int expectedCount, String name) {
            super(stage, name);
            this.expectedCount = expectedCount;
        }
        
        @Override
        protected boolean execute(Worker worker) {
            expectedCount--;

            if (expectedCount <= 0) {
                complete();
            }

            return true;
        }

        @Override
        protected void start() {
            startCount++;
            super.start();
        }

        @Override
        protected void complete() {
            endCount++;
            super.complete();
        }
    }

    @Test
    public void testExecuteOneStep() {
        Project p = new Project("Test");

        assertEquals(Stage.WAITING, p.getStage(), "Waiting with no steps");

        TestStep step1 = new TestStep(Stage.ACTIVE, 2, "Step 1");
        p.addStep(step1);
        assertEquals(Stage.WAITING, p.getStage(), "Waiting before execution");

        // Execute once
        Worker worker = null;
        p.execute(worker);
        assertEquals(Stage.ACTIVE, p.getStage(), "Stage is Active");

        // Xecute second time
        p.execute(worker);
        assertEquals(Stage.DONE, p.getStage(), "Stage is DONE");
        assertEquals(1, step1.startCount, "Step started once");
        assertEquals(1, step1.endCount, "Step ended once");
        assertEquals(0, step1.expectedCount, "Step fully expected");

    }

    @Test
    public void testExecuteTwoStep() {
        Project p = new Project("Test");

        assertEquals(Stage.WAITING, p.getStage(), "Waiting with no steps");

        TestStep step1 = new TestStep(Stage.ACTIVE, 2, "Step 1");
        TestStep step2 = new TestStep(Stage.CLOSEDOWN, 3, "Step 2");

        p.addStep(step1);
        p.addStep(step2);

        assertEquals(Stage.WAITING, p.getStage(), "Waiting before execution");
        assertEquals(2, p.getRemainingSteps().size(), "Number of step before starting");


        // Execute once
        Worker worker = null;
        p.execute(worker);
        assertEquals(Stage.ACTIVE, p.getStage(), "Stage is Active");
        assertEquals(p.getStep().getDescription(), "Step 1", "Project 1st step");
        assertEquals(2, p.getRemainingSteps().size(), "Number of step after starting");


        // Xecute second time
        p.execute(worker);
        assertEquals(Stage.CLOSEDOWN, p.getStage(), "Step1 stage is DONE");
        assertEquals(p.getStep().getDescription(), "Step 2", "Project 2nd step");
        assertEquals(1, step1.startCount, "Step1 started once");
        assertEquals(1, step1.endCount, "Step1 ended once");
        assertEquals(0, step1.expectedCount, "Step1 fully expected");
        assertEquals(1, p.getRemainingSteps().size(), "Number of step after 1st step");


        // Step 2, execute 1
        p.execute(worker);
        assertEquals(Stage.CLOSEDOWN, p.getStage(), "Stage is Active");

        // Step 2, execute 2
        p.execute(worker);
        assertEquals(Stage.CLOSEDOWN, p.getStage(), "Stage is Active");

        // Xecute second time
        p.execute(worker);
        assertEquals(Stage.DONE, p.getStage(), "Step2 stage is DONE");
        assertEquals(1, step1.startCount, "Step2 started once");
        assertEquals(1, step1.endCount, "Step2 ended once");
        assertEquals(0, step1.expectedCount, "Step2 fully expected");
        assertEquals(0, p.getRemainingSteps().size(), "Number of step after all steps");

    }

    @Test
    public void testAbort() {
        Project p = new Project("Test");

        TestStep step1 = new TestStep(Stage.ACTIVE, 2, "Step 1");
        TestStep step2 = new TestStep(Stage.CLOSEDOWN, 1, "Step 2");

        p.addStep(step1);
        p.addStep(step2);

        Worker worker = null;
        // Execute Step 1
        p.execute(worker);
        p.abort("Test");

        assertEquals(Stage.ABORTED, p.getStage(), "Stage is aborted");
        assertTrue(p.isFinished(), "Project is done");

        // Step 1 only executed one
        assertEquals(1, step1.startCount, "Step1 started once");
        assertEquals(1, step1.endCount, "Step1 ended once");
        assertEquals(1, step1.expectedCount, "Step1 execution 1 short");

        // Step 2 never used
        assertEquals(0, step2.startCount, "Step2 never started");
        assertEquals(0, step2.endCount, "Step2 never ended");
        assertEquals(1, step2.expectedCount, "Step2 fully expected");
    }


    @Test
    public void testRemoveStep() {
        Project p = new Project("Test");

        TestStep step1 = new TestStep(Stage.PREPARATION, 2, "Step 1");
        TestStep step2 = new TestStep(Stage.ACTIVE, 1, "Old Step");
        TestStep step3 = new TestStep(Stage.CLOSEDOWN, 1, "New Step");


        p.addStep(step1);
        p.addStep(step2);

        // Execute once
        Worker worker = null;
        p.execute(worker);
        assertEquals(Stage.PREPARATION, p.getStage(), "Stage is Active");
        assertEquals(p.getStep().getDescription(), "Step 1", "Project step");


        // Swap last step
        p.removeStep(step2);
        p.addStep(step3);

        // Xecute end of first step, stage is new last step
        p.execute(worker);
        assertEquals(Stage.CLOSEDOWN, p.getStage(), "Process stage is Closedown");
        assertEquals(p.getStep().getDescription(), "New Step", "Project step");

        // Last step executed
        p.execute(worker);
        assertEquals(0, step2.startCount, "Remove step not started");
        assertEquals(0, step2.endCount, "Removed step not stopped");
        assertEquals(1, step2.expectedCount, "Remove step not executed");

        // Xecute second time
        assertEquals(Stage.DONE, p.getStage(), "Step3 stage is DONE");
        assertEquals(1, step3.startCount, "Step3 started once");
        assertEquals(1, step3.endCount, "Step3 ended once");
        assertEquals(0, step3.expectedCount, "Step3 fully expected");
    }



    @Test
    public void testCreateBadStep() {

        // Add a step with Active stage
        assertThrows(IllegalArgumentException.class, () ->  {
                        new TestStep(Stage.WAITING, 0, null);
                    }, "Create illegal WAITING step");
        
        assertThrows(IllegalArgumentException.class, () ->  {
                        new TestStep(Stage.DONE, 0, null);
                    }, "Create illegal DONE step");
    }

    @Test
    public void testAddingBadStage() {
        Project p = new Project("Bad");
        p.addStep(new TestStep(Stage.CLOSEDOWN, 0, null));

        // Add a step with Active stage
        assertThrows(IllegalArgumentException.class, () ->  {
                        p.addStep(new TestStep(Stage.ACTIVE, 0, null));
                    }, "Add illegal ACTIVE step");
        
        assertThrows(IllegalArgumentException.class, () ->  {
                        p.addStep(new TestStep(Stage.PREPARATION, 0, null));
                    }, "Add illegal PREPARATION step");
    }
}
