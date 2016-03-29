package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantSimulation;
import edu.cwru.sepia.agent.planner.ResourceSimulation;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class HarvestAction implements StripsAction{

	private PeasantSimulation peasant;
	private ResourceSimulation resource;
	
	public HarvestAction(PeasantSimulation peasant, ResourceSimulation resource) {
		this.peasant = peasant;
		this.resource = resource;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return resource!=null
				&&peasant.getPosition().isAdjacent(resource.getPosition()) 
				&& !peasant.isCarrying() 
				&& resource.getResourceRemaining()>0;
	}

	@Override
	public GameState apply(GameState state) {
		GameState nextGameState = new GameState(state, this);

		ResourceType resType = resourceNodeTypeToResourceType(resource.getResourceType());
		int cargoAmount = (resource.getResourceRemaining()>=100) ? 100 : resource.getResourceRemaining();
		
		PeasantSimulation peasantClone = new PeasantSimulation(peasant.getPosition(), cargoAmount, resType, peasant.getUnitId());
		ResourceSimulation resourceClone = new ResourceSimulation(resource.getPosition(), resource.getResourceRemaining()-cargoAmount, resource.getResourceType(), resource.getResourceId());
		nextGameState.getPeasantMap().put(peasantClone.getUnitId(), peasantClone);
		nextGameState.getResourceMap().put(resourceClone.getResourceId(), resourceClone);
		return nextGameState;
	}
	
	private ResourceType resourceNodeTypeToResourceType(ResourceNode.Type type){
		switch(type){
		case GOLD_MINE:
			return ResourceType.GOLD;
		case TREE:
			return ResourceType.WOOD;
		}
		return null;
	}

	/**
	 * harvest costs 1 turn
	 */
	@Override
	public int cost() {
		return 1;
	}

}
