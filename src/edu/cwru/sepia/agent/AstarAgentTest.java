package edu.cwru.sepia.agent;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

import edu.cwru.sepia.agent.AstarAgent.MapLocation;

public class AstarAgentTest {

	@Test
	public void testIsValidMapLocation() {
		AstarAgent a = new AstarAgent(1);
		boolean test = a.isValidMapLocation(new AstarAgent.MapLocation(1,1, null, 0), new AstarAgent.MapLocation(1,2, null, 0), 10, 10, new HashSet<MapLocation>());
		assertTrue(test);
	}

}
