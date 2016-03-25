package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;

public class ResourceSimulation {



	private int x;
	private int y;
	private int resourceRemaining;
	private ResourceNode.Type resourceType;
	
	public ResourceSimulation(ResourceView res){
		this.x = res.getXPosition();
		this.y = res.getYPosition();
		this.resourceRemaining = res.getAmountRemaining();
		this.resourceType = res.getType();
	}
	
	public ResourceSimulation(int x, int y, int resourceRemaining, ResourceNode.Type resourceType){
		this.x = x;
		this.y = y;
		this.resourceRemaining = resourceRemaining;
		this.resourceType = resourceType;
	}
	
	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the resourceRemaining
	 */
	public int getResourceRemaining() {
		return resourceRemaining;
	}

	/**
	 * @return the resourceType
	 */
	public ResourceNode.Type getResourceType() {
		return resourceType;
	}	
}
