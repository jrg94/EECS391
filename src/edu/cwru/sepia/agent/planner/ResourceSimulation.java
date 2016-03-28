package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;

public class ResourceSimulation {

	private int resourceId;
	private Position position;
	private int resourceRemaining;
	private ResourceNode.Type resourceType;
	
	public ResourceSimulation(ResourceView res){
		this.position = new Position(res.getXPosition(), res.getYPosition());
		this.resourceRemaining = res.getAmountRemaining();
		this.resourceType = res.getType();
		this.resourceId = res.getID();
	}
	
	public ResourceSimulation(Position position, int resourceRemaining, ResourceNode.Type resourceType, int resourceId){
		this.position = position;
		this.resourceRemaining = resourceRemaining;
		this.resourceType = resourceType;
		this.resourceId = resourceId;
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

	/**
	 * @return the resourceId
	 */
	public int getResourceId() {
		return resourceId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + resourceId;
		result = prime * result + resourceRemaining;
		result = prime * result + ((resourceType == null) ? 0 : resourceType.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceSimulation other = (ResourceSimulation) obj;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (resourceId != other.resourceId)
			return false;
		if (resourceRemaining != other.resourceRemaining)
			return false;
		if (resourceType != other.resourceType)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ResourceSimulation [resourceId=" + resourceId + ", position=" + position + ", resourceRemaining="
				+ resourceRemaining + ", resourceType=" + resourceType + "]";
	}	
	
}
