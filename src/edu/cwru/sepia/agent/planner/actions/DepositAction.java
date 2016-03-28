package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantSimulation;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class DepositAction implements StripsAction {

	private PeasantSimulation peasant;
	
	public DepositAction(PeasantSimulation peasant) {
		this.peasant = peasant;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return peasant.getPosition().isAdjacent(state.getTownHall().getPosition())
				&& peasant.isCarrying();
	}

	@Override
	public GameState apply(GameState state) {
		GameState nextGameState = new GameState(state, this);
		
		ResourceType holdingType = peasant.getCargoType();
		int holdingAmount = peasant.getCargo();
		
		PeasantSimulation peasantClone = new PeasantSimulation(peasant.getPosition(), 0, null, peasant.getUnitId());
		nextGameState.getPeasantMap().put(peasantClone.getUnitId(), peasantClone);
		
		// if i get null pointer error here, it's cuz isCarrying doesn't work
		switch(holdingType){
		case GOLD:
			nextGameState.setRequiredGold(nextGameState.getRequiredGold() - holdingAmount);
			break;
		case WOOD:
			nextGameState.setRequiredWood(nextGameState.getRequiredWood() - holdingAmount);
			break;
		}
		return nextGameState;
	}

}
