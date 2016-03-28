package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;

public class ResourceSimulation {

	private Position position;
	private int resourceRemaining;
	private ResourceNode.Type resourceType;
	
	public ResourceSimulation(ResourceView res){
		this.position = new Position(res.getXPosition(), res.getYPosition());
		this.resourceRemaining = res.getAmountRemaining();
		this.resourceType = res.getType();
	}
	
	public ResourceSimulation(Position position, int resourceRemaining, ResourceNode.Type resourceType){
		this.position = position;
		this.resourceRemaining = resourceRemaining;
		this.resourceType = resourceType;
	}

	/**
	 * @return the position
	 */
	public Position getPosition() {
		return position;
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
