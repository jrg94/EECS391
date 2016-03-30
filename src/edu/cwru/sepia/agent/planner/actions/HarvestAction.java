package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantSimulation;
import edu.cwru.sepia.agent.planner.ResourceSimulation;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class HarvestAction implements StripsAction{

	private ResourceSimulation resource;
	
	private static final int PEASANT_GOLD_HARVEST_DURATION = 400;
	private static final int PEASANT_WOOD_HARVEST_DURATION = 1000;
	
	public HarvestAction(ResourceSimulation resource) {
		this.resource = resource;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		if (resource == null){
			return false;
		}
		boolean hasEnough = state.hasEnough(resource.getResourceType());
		boolean condition = true;
		for (PeasantSimulation peasant : state.getPeasantMap().values()){
			condition = condition && peasant.getPosition().isAdjacent(resource.getPosition()) 
					&& !peasant.isCarrying() 
					&& resource.getResourceRemaining()>0;
		}
		return !hasEnough && condition;
	}

	@Override
	public GameState apply(GameState state) {
		GameState nextGameState = new GameState(state, this);

		ResourceType resType = resourceNodeTypeToResourceType(resource.getResourceType());
		int totalGatheredAmount = 0;
		
		for (PeasantSimulation peasant : nextGameState.getPeasantMap().values()){
			
			int cargoAmount = (resource.getResourceRemaining()>=100) ? 100 : resource.getResourceRemaining();
			totalGatheredAmount += cargoAmount;
			PeasantSimulation peasantClone = new PeasantSimulation(peasant.getPosition(), cargoAmount, resType, peasant.getUnitId());
			nextGameState.getPeasantMap().put(peasantClone.getUnitId(), peasantClone);
			
		}
		
		ResourceSimulation resourceClone = new ResourceSimulation(resource.getPosition(), resource.getResourceRemaining()-totalGatheredAmount, resource.getResourceType());
		nextGameState.getResourceMap().put(resourceClone.getPosition(), resourceClone);
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

	/**
	 * @return the resource
	 */
	public ResourceSimulation getResource() {
		return resource;
	}

}
