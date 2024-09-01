/*
 * Mars Simulation Project
 * ComputingJob.java
 * @date 2024-05-04
 * @author Barry Evans
 */
package com.mars_sim.core.computing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.Unit;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.Computation;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class represents a job that uses computing resources to deliver an outcome.
 */
public class ComputingJob implements Serializable {
	
    private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(ComputingJob.class.getName());

    private int lastMSol = 1;
    
    private double initDemand;
    private double remainingNeed;
    private double cuPerMSol;
    private double duration;
    
    private Computation singleNode;
    
    private Map<Computation, Double> nodeLoads;
    
    private String purpose;

    private Settlement host;
    
    /**
     * Constructor. Creates a computing job to run at a Settlement for a maximum duration.
     * 
     * @param host Where the computing job will run
     * @param loadType the type of computing load
     * @param now the current integer millisols
     * @param duration Duration the computing job runs
     * @param purpose The purpose of the job
     */
    public ComputingJob(Settlement host, ComputingLoadType loadType, int now, double duration, String purpose) {
        this.host = host;
        this.duration = duration;
        double factor = 0;
        
        if (ComputingLoadType.LOW == loadType) {
        	factor = .5;
        } else if (ComputingLoadType.MID == loadType) {
        	factor = 1;
        } else if (ComputingLoadType.HIGH == loadType) {
        	factor = 1.5;
        } else if (ComputingLoadType.HEAVY == loadType) {
        	factor = 3;
        } 
        
        this.lastMSol = now;
        
        this.cuPerMSol = factor * .5 * (.5 + RandomUtil.getRandomDouble(.5));
        
        this.initDemand = duration * cuPerMSol;
        
        this.remainingNeed = initDemand;
        
        this.purpose = purpose;
        		
        this.nodeLoads = new HashMap<>();
        
		logger.log(host, Level.INFO, 30_000, "Requested " 
		 		+ Math.round(initDemand * 100.0)/100.0 + " CUs for a duration of "
		 		+ Math.round(duration * 10.0)/10.0 + " msols on '"
		 		+ purpose + "'.");
    }

    public Set<Computation> getNodes() {
    	return nodeLoads.keySet();
    }
    
    public Computation getSingleNode() {
    	return singleNode;
    }
    
    /**
     * Picks from a single node for processing service.
     * 
     * @param timeCompleted
     * @param now the msol at this moment
     * @return
     */
    public Computation pickSingleNode(double timeCompleted, int now) {
    	Computation center = null;
    	
		int nowMSol = now;
//		logger.info(host, 30_000, "1. nowMSol: " + nowMSol + ".");
        int startMSol = nowMSol + 1;
        int endMSol = (int) (startMSol + duration - timeCompleted);
        
	    center = host.getBuildingManager()
		    .getMostFreeComputingNode(initDemand, startMSol, endMSol);
	    if (center == null) {
		    logger.info(host, 30_000, "No server nodes found to have enough resources for " 
		    	+ purpose + ".");
		    return null;
        }

        if (!center.scheduleTask(initDemand, startMSol, endMSol)) {
		    logger.info(center.getBuilding(), 30_000, "Not enough CUs in " 
		    	+ center.getBuilding().getName() + " for '" 
		    	+ purpose + "'.");
		    return null;
        }

        setRecordMSol(nowMSol);
        
        this.singleNode = center;
        
        return center;
    }
    
    /**
     * Picks from multiple nodes for processing service.
     * 
     * @param timeCompleted
     * @param now the msol at this moment
     * @return
     */
    public boolean pickMultipleNodes(double timeCompleted, int now) {
    	boolean canWork = false;
    	
		List<Building> nodes = new ArrayList<>(host.getBuildingManager().getBuildingSet(FunctionType.COMPUTATION));

		double remainingDemand = initDemand;
		
        int startMSol = now + 1;
        int endMSol = (int) (startMSol + duration - timeCompleted);
        
		// Perform the first round of spreading the load
		boolean complete = spreadLoad(nodes, remainingDemand, startMSol, endMSol);
		
		if (!complete) {
			// Perform the second round of spreading the load
			complete = spreadLoad(nodes, remainingDemand, startMSol, endMSol);
		}
		
		if (!complete) {
			// Perform the third round of spreading the load
			complete = spreadLoad(nodes, remainingDemand, startMSol, endMSol);
		}
		
	    if (!canWork || !complete) {
		    logger.info(host, 30_000, "Servers unable to handle the computational request for " 
		    	+ purpose + ".");
        }

        setRecordMSol(now);
        
        return canWork;
    }
    
    /**
     * Spreads the server loads.
     * 
     * @param nodes
     * @param remainingDemand
     * @param startMSol
     * @param endMSol
     * @return
     */
    private boolean spreadLoad(List<Building> nodes, double remainingDemand, int startMSol, int endMSol) {
    	boolean complete = false;
    	int numNodes = nodes.size();
    	for (int i = 0; i < numNodes; i++) {
			Building b = nodes.get(i);
			Computation node = b.getComputation();
			double portionToHandle = remainingDemand / numNodes;
			complete = node.scheduleTask(portionToHandle, startMSol, endMSol);
			
			if (complete) {
				remainingDemand -= portionToHandle;
				this.nodeLoads.put(node, portionToHandle);
			}
			
			if (remainingDemand == 0)
				break;
		}
    	
    	return complete;
    }
    
    /**
     * Processes the request.
     * 
     * @param timeCompleted
     * @param nowMSol
     */
    public void process(double timeCompleted, int nowMSol) {
    	Computation node = singleNode;
    	Unit unit = null;
    	
    	if (node != null) {
    		unit = node.getBuilding();
    	}
    	else {
    		unit = host;
    	}
    	
//    	if (lastMSol != nowMSol) logger.info(unit, 30_000, "2. lastMSol: " + lastMSol + "  nowMSol: " + nowMSol); 
	
        int interval = nowMSol - lastMSol;
        if (interval == 0)
        	return;
        
        if (interval < 0)
        	interval += 1000;

        double consumed = interval * cuPerMSol;

        double newNeed = remainingNeed - consumed; 
        
        if (newNeed < 0) {
        	consumed = remainingNeed;
        	remainingNeed = 0;
        }
        
        // If scheduled then reduce computing
//        logger.info(unit, 30_000, "Consumed " + Math.round(consumed * 1000.0)/1000.0 + " CUs.  "
//        		+ "Remaining: " + Math.round(remainingNeed * 100.0)/100.0 + " -> " + Math.round(newNeed * 100.0)/100.0 + " CUs.");
       
        if (node == null) {
	        int num = nodeLoads.size();
	        
	        for (Computation center: nodeLoads.keySet()) {
	            double existingLoad = nodeLoads.get(center);
	            double newLoad = existingLoad - consumed/num;
	            if (newLoad < 0) {
	            	newLoad = 0;
	            }
	            
	            nodeLoads.put(center, newLoad);     
	    		// Increase the entropy
	            center.increaseEntropy(consumed * Computation.ENTROPY_FACTOR);
	        }
        }
        else {
        	// Increase the entropy
            node.increaseEntropy(consumed * Computation.ENTROPY_FACTOR);
        }
    
        setRecordMSol(nowMSol);
    }
    
    /**
	 * Accesses the computing nodes. This will reduce the computing needed by a factor
     * related to the time.
	 * 
	 * @param node the server node that provides the resources
	 * @param timeCompleted  the time already spent on a task
	 * @param nowMSol the msol at this moment
     * @return Computing process is running
	 */
	public Computation consumeProcessing(Computation node, double timeCompleted, int nowMSol) {
		Computation center = node;

		if (center == null) {
			// Pick one node
			center = pickSingleNode(timeCompleted, nowMSol);
        }
		
		else {
	        // Submit request for computing resources
			process(timeCompleted, nowMSol);
		}
		
        return center;
	}

	public void setRecordMSol(int nowMSol) {
		lastMSol = nowMSol;
	}
	
	public int getLastMSol() {
		return lastMSol;
	}
	
    /**
     * Gets how much remaining CUs still in need.
     * 
     * @return
     */
    public double getRemainingNeed() {
        return remainingNeed;
    }

    /**
     * Has all the computing demand been completed.
     * 
     * @return
     */
    public boolean isCompleted() {
        return remainingNeed <= 0;
    }

    /**
     * Gets how many CU consumed per mSol by this job.
     * 
     * @return
     */
    public double getCUPerMSol() {
        return cuPerMSol;
    }

    /**
     * Gets how much has been consumed so far.
     * 
     * @return
     */
    public double getConsumed() {
        return initDemand - remainingNeed;
    }
}
