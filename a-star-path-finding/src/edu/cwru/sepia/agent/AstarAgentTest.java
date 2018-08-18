package edu.cwru.sepia.agent;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

import edu.cwru.sepia.agent.AstarAgent.MapLocation;

public class AstarAgentTest {

	@Test
	public void testIsValidMapLocation() {
		AstarAgent a = new AstarAgent(1);
		
		boolean testInsideGridNotBlocked = a.isValidMapLocation(new AstarAgent.MapLocation(1,1, null, 0), new AstarAgent.MapLocation(1,2, null, 0), 10, 10, new HashSet<MapLocation>());
		assertTrue(testInsideGridNotBlocked);
		
		boolean testOutsideGrid = a.isValidMapLocation(new AstarAgent.MapLocation(0,0, null, 0), new AstarAgent.MapLocation(-1,0, null, 0), 10, 10, new HashSet<MapLocation>());
		assertFalse(testOutsideGrid);
		
		Set<MapLocation> resources = new HashSet<MapLocation>();
		resources.add(new AstarAgent.MapLocation(1,1,null,0));
		boolean testInsideGridBlocked = a.isValidMapLocation(new AstarAgent.MapLocation(0,0, null, 0), new AstarAgent.MapLocation(1,1, null, 0), 10, 10, resources);
		assertFalse(testInsideGridBlocked);
	}

}
