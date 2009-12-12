/**
 * Mars Simulation Project
 * ScienceUtil.java
 * @version 2.87 2009-11-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.science;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobManager;

/**
 * Utility class for science fields.
 */
public class ScienceUtil {

    // Data members
    private static List<Science> sciences;
    
    /**
     * Private constructor for static utility class.
     */
    private ScienceUtil() {}
    
    /**
     * Load available sciences.
     */
    private static void loadSciences() {
        // Load available sciences in list.
        sciences = new ArrayList<Science>();
        sciences.add(new Science(Science.AREOLOGY, 
                new Job[] { JobManager.getJob("Areologist") }));
        sciences.add(new Science(Science.ASTRONOMY, 
                new Job[] { JobManager.getJob("Astronomer") }));
        sciences.add(new Science(Science.BIOLOGY, 
                new Job[] { JobManager.getJob("Biologist") }));
        sciences.add(new Science(Science.BOTANY, 
                new Job[] { JobManager.getJob("Botanist") }));
        sciences.add(new Science(Science.CHEMISTRY, 
                new Job[] { JobManager.getJob("Chemist") }));
        sciences.add(new Science(Science.MATHEMATICS, 
                new Job[] { JobManager.getJob("Mathematician") }));
        sciences.add(new Science(Science.MEDICINE, 
                new Job[] { JobManager.getJob("Doctor") }));
        sciences.add(new Science(Science.METEOROLOGY, 
                new Job[] { JobManager.getJob("Meteorologist") }));
        sciences.add(new Science(Science.PHYSICS, 
                new Job[] { JobManager.getJob("Physicist") }));
        
        // Configure collaborative sciences.
        configureCollaborativeSciences();
    }
    
    /**
     * Configure sciences that can collaborate on research.
     */
    private static void configureCollaborativeSciences() {
        
        Science areology = getScience(Science.AREOLOGY);
        Science astronomy = getScience(Science.ASTRONOMY);
        Science biology = getScience(Science.BIOLOGY);
        Science botany = getScience(Science.BOTANY);
        Science chemistry = getScience(Science.CHEMISTRY);
        Science mathematics = getScience(Science.MATHEMATICS);
        Science medicine = getScience(Science.MEDICINE);
        Science meteorology = getScience(Science.METEOROLOGY);
        Science physics = getScience(Science.PHYSICS);
        
        areology.setCollaborativeSciences(new Science[] { biology, chemistry, 
                mathematics, meteorology });
        
        astronomy.setCollaborativeSciences(new Science[] { biology, chemistry, 
                mathematics, physics });
        
        biology.setCollaborativeSciences(new Science[] { botany, chemistry, 
                mathematics });
        
        botany.setCollaborativeSciences(new Science[] { biology, chemistry, 
                mathematics });
        
        chemistry.setCollaborativeSciences(new Science[] { mathematics });
        
        mathematics.setCollaborativeSciences(new Science[] {});
        
        medicine.setCollaborativeSciences(new Science[] { biology, botany, 
                chemistry, mathematics });
        
        meteorology.setCollaborativeSciences(new Science[] { chemistry, 
                mathematics, physics });
        
        physics.setCollaborativeSciences(new Science[] { astronomy, mathematics });
    }
    
    /**
     * Gets a list of all sciences.
     * @return list of all sciences.
     */
    public static List<Science> getAllSciences() {
        return Collections.unmodifiableList(sciences);
    }
    
    /**
     * Gets a science based on its name.
     * @param name the science name (case insensitive).
     * @return science or null if none found matching name.
     */
    public static Science getScience(String name) {
        if (sciences == null) loadSciences();
        
        Science result = null;
        
        Iterator<Science> i = sciences.iterator();
        while (i.hasNext()) {
            Science science = i.next();
            if (science.getName().equalsIgnoreCase(name)) result = science;
        }
        
        return result;
    }
    
    /**
     * Gets the skill associated with the science.
     * @param science the science.
     * @return skill name.
     */
    public static String getAssociatedSkill(Science science) {
        if (sciences == null) loadSciences();
        
        return science.getName();
    }
    
    /**
     * Gets all jobs related to a scientific field.
     * @param science the scientific field.
     * @return array of jobs (empty array if none).
     */
    public static Job[] getAssociatedJobs(Science science) {
        if (sciences == null) loadSciences();
        
        Job[] jobs = science.getJobs();
        return jobs.clone();
    }
    
    /**
     * Checks if a job is associated with any science.
     * @param job the job.
     * @return true if job is associated with a science.
     */
    public static boolean isScienceJob(Job job) {
        if (sciences == null) loadSciences();
        
        boolean result = false;
        
        Iterator<Science> i = sciences.iterator();
        while (i.hasNext()) {
            Science science = i.next();
            Job[] jobs = science.getJobs();
            for (int x = 0; x < jobs.length; x++) {
                if (jobs[x].equals(job)) result = true;
            }
        }
        
        return result;
    }
    
    /**
     * Gets a science associated with a given job if any.
     * @param job the job.
     * @return associated science or null if none.
     */
    public static Science getAssociatedScience(Job job) {
        if (sciences == null) loadSciences();
        
        Science result = null;
        
        Iterator<Science> i = sciences.iterator();
        while (i.hasNext()) {
            Science science = i.next();
            Job[] jobs = science.getJobs();
            for (int x = 0; x < jobs.length; x++) {
                if (jobs[x].equals(job)) result = science;
            }
        }
        
        return result;
    }
    
    /**
     * Checks if a science is collaborative to a primary science.
     * @param primaryScience the primary science.
     * @param science the science to check.
     * @return true if science is collaborative to primary science.
     */
    public static boolean isCollaborativeScience(Science primaryScience, Science science) {
        if (sciences == null) loadSciences();
        
        boolean result = false;
        
        Science[] collaborators = primaryScience.getCollaborativeSciences();
        for (int x = 0; x < collaborators.length; x++) {
            if (collaborators[x].equals(science)) result = true;
        }
        
        return result;
    }
}