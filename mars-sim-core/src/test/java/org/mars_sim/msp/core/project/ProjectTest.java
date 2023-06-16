package org.mars_sim.msp.core.project;

import static org.junit.Assert.assertThrows;

import org.mars_sim.msp.core.person.ai.task.util.Worker;

import junit.framework.TestCase;


public class ProjectTest extends TestCase {
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

    public void testExecuteOneStep() {
        Project p = new Project("Test");

        assertEquals("Waiting with no steps", Stage.WAITING, p.getStage());

        TestStep step1 = new TestStep(Stage.ACTIVE, 2, "Step 1");
        p.addStep(step1);
        assertEquals("Waiting before execution", Stage.WAITING, p.getStage());

        // Execute once
        Worker worker = null;
        p.execute(worker);
        assertEquals("Stage is Active", Stage.ACTIVE, p.getStage());

        // Xecute second time
        p.execute(worker);
        assertEquals("Stage is DONE", Stage.DONE, p.getStage());
        assertEquals("Step started once", 1, step1.startCount);
        assertEquals("Step ended once", 1, step1.endCount);
        assertEquals("Step fully expected", 0, step1.expectedCount);

    }

    public void testExecuteTwoStep() {
        Project p = new Project("Test");

        assertEquals("Waiting with no steps", Stage.WAITING, p.getStage());

        TestStep step1 = new TestStep(Stage.ACTIVE, 2, "Step 1");
        TestStep step2 = new TestStep(Stage.CLOSEDOWN, 3, "Step 2");

        p.addStep(step1);
        p.addStep(step2);

        assertEquals("Waiting before execution", Stage.WAITING, p.getStage());
        assertEquals("Number of step before starting", 2, p.getRemainingSteps().size());


        // Execute once
        Worker worker = null;
        p.execute(worker);
        assertEquals("Stage is Active", Stage.ACTIVE, p.getStage());
        assertEquals("Project 1st step", "Step 1", p.getStep().getDescription());
        assertEquals("Number of step after starting", 2, p.getRemainingSteps().size());


        // Xecute second time
        p.execute(worker);
        assertEquals("Step1 stage is DONE", Stage.CLOSEDOWN, p.getStage());
        assertEquals("Project 2nd step", "Step 2", p.getStep().getDescription());
        assertEquals("Step1 started once", 1, step1.startCount);
        assertEquals("Step1 ended once", 1, step1.endCount);
        assertEquals("Step1 fully expected", 0, step1.expectedCount);
        assertEquals("Number of step after 1st step", 1, p.getRemainingSteps().size());


        // Step 2, execute 1
        p.execute(worker);
        assertEquals("Stage is Active", Stage.CLOSEDOWN, p.getStage());

        // Step 2, execute 2
        p.execute(worker);
        assertEquals("Stage is Active", Stage.CLOSEDOWN, p.getStage());

        // Xecute second time
        p.execute(worker);
        assertEquals("Step2 stage is DONE", Stage.DONE, p.getStage());
        assertEquals("Step2 started once", 1, step1.startCount);
        assertEquals("Step2 ended once", 1, step1.endCount);
        assertEquals("Step2 fully expected", 0, step1.expectedCount);
        assertEquals("Number of step after all steps", 0, p.getRemainingSteps().size());

    }

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

        assertEquals("Stage is aborted", Stage.ABORTED, p.getStage());
        assertTrue("Project is done", p.isFinished());

        // Step 1 only executed one
        assertEquals("Step1 started once", 1, step1.startCount);
        assertEquals("Step1 ended once", 1, step1.endCount);
        assertEquals("Step1 execution 1 short", 1, step1.expectedCount);

        // Step 2 never used
        assertEquals("Step2 never started", 0, step2.startCount);
        assertEquals("Step2 never ended", 0, step2.endCount);
        assertEquals("Step2 fully expected", 1, step2.expectedCount);
    }


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
        assertEquals("Stage is Active", Stage.PREPARATION, p.getStage());
        assertEquals("Project step", "Step 1", p.getStep().getDescription());


        // Swap last step
        p.removeStep(step2);
        p.addStep(step3);

        // Xecute end of first step, stage is new last step
        p.execute(worker);
        assertEquals("Process stage is Closedown", Stage.CLOSEDOWN, p.getStage());
        assertEquals("Project step", "New Step", p.getStep().getDescription());

        // Last step executed
        p.execute(worker);
        assertEquals("Remove step not started", 0, step2.startCount);
        assertEquals("Removed step not stopped", 0, step2.endCount);
        assertEquals("Remove step not executed", 1, step2.expectedCount);

        // Xecute second time
        assertEquals("Step3 stage is DONE", Stage.DONE, p.getStage());
        assertEquals("Step3 started once", 1, step3.startCount);
        assertEquals("Step3 ended once", 1, step3.endCount);
        assertEquals("Step3 fully expected", 0, step3.expectedCount);
    }



    public void testCreateBadStep() {

        // Add a step with Active stage
        assertThrows("Create illegal WAITING step", IllegalArgumentException.class, () ->  {
                        new TestStep(Stage.WAITING, 0, null);
                    });
        
        assertThrows("Create illegal DONE step", IllegalArgumentException.class, () ->  {
                        new TestStep(Stage.DONE, 0, null);
                    });
    }

    public void testAddingBadStage() {
        Project p = new Project("Bad");
        p.addStep(new TestStep(Stage.CLOSEDOWN, 0, null));

        // Add a step with Active stage
        assertThrows("Add illegal ACTIVE step", IllegalArgumentException.class, () ->  {
                        p.addStep(new TestStep(Stage.ACTIVE, 0, null));
                    });
        
        assertThrows("Add illegal PREPARATION step", IllegalArgumentException.class, () ->  {
                        p.addStep(new TestStep(Stage.PREPARATION, 0, null));
                    });
    }
}
