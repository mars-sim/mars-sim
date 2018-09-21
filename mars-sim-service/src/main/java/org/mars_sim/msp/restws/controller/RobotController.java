package org.mars_sim.msp.restws.controller;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.restws.mapper.RobotDetailsMapper;
import org.mars_sim.msp.restws.mapper.RobotSummaryMapper;
import org.mars_sim.msp.restws.model.PagedList;
import org.mars_sim.msp.restws.model.RobotDetails;
import org.mars_sim.msp.restws.model.RobotSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController()
public class RobotController extends BaseController {
	/** initialized logger for this class. */
	private static Log log = LogFactory.getLog(RobotController.class);
	
	@Autowired
    private UnitManager robotManager;

	@Autowired
	private RobotSummaryMapper summaryMapper;

	@Autowired
	private RobotDetailsMapper detailMapper;
	
	/**
	 * Find a Robot entity by the unique identifer
	 * @param robotId
	 * @return
	 */
	private Robot getRobot(int robotId) {
		// Need a better way to find unit
		Iterator<Robot> it = robotManager.getRobots().iterator();
		while (it.hasNext()) {
			Robot unit = it.next();
			if (unit.getIdentifier() == robotId) {
				return unit;
			}
		}
		log.error("Can not find " + robotId);
		throw new NotFoundException("Robot", robotId);
	}
	
	@ApiOperation(value = "get Robot by Id", nickname = "getRobot")
	@RequestMapping(method = RequestMethod.GET, path="/robots/{id}", produces = "application/json")
    public RobotDetails getRobotDetails(@PathVariable(value="id") int robotId) {
		
        return detailMapper.robotToRobotDetails(getRobot(robotId));
    }
	
	@ApiOperation(value = "get All Robots", nickname = "getRobots")
    @RequestMapping(method=RequestMethod.GET, path="/robots", produces = "application/json")
    public PagedList<RobotSummary> robots(@RequestParam(value="page", defaultValue="1") int page,
    								   @RequestParam(value="size", defaultValue="10") int pageSize) {
    	Collection<Robot> allRobots = robotManager.getRobots();
		List<Robot> filtered = filter(allRobots, page, pageSize);
		
		return new PagedList<RobotSummary>(summaryMapper.robotsToRobotSummarys(filtered),
											page, pageSize, allRobots.size());
    }

}
