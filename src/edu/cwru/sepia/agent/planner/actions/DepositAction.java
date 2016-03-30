package edu.cwru.sepia.agent.planner.actions;

import java.util.HashMap;
import java.util.Map;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantSimulation;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class DepositAction implements StripsAction {

	private static final int PEASANT_DEPOSIT_DURATION = 25;
	
	@Override
	public boolean preconditionsMet(GameState state) {
		boolean condition = true;
		for (PeasantSimulation peasant : state.getPeasantMap().values()){
			condition = condition && peasant.getPosition().isAdjacent(state.getTownHall().getPosition())
					&& peasant.isCarrying();
		}
		return condition;
	}

	@Override
	public GameState apply(GameState state) {
		GameState nextGameState = new GameState(state, this);
		
		ResourceType holdingType = nextGameState.getPeasantMap().get(0).getCargoType();
		int holdingAmount = 0;
		for (PeasantSimulation peasant : nextGameState.getPeasantMap().values()){
			holdingAmount += peasant.getCargo();
			PeasantSimulation peasantClone = new PeasantSimulation(peasant.getPosition(), 0, null, peasant.getUnitId());
			nextGameState.getPeasantMap().put(peasantClone.getUnitId(), peasantClone);
		}
		
		// if i get null pointer error here, it's cuz isCarrying doesn't work
		switch(holdingType){
		case GOLD:
			nextGameState.setRequiredGold(nextGameState.getRequiredGold() - holdingAmount);
			nextGameState.setCurrentGold(nextGameState.getCurrentGold()+holdingAmount);
			break;
		case WOOD:
			nextGameState.setRequiredWood(nextGameState.getRequiredWood() - holdingAmount);
			break;
		}
		return nextGameState;
	}

	/**
	 * deposit only takes 1 turn
	 */
	@Override
	public int cost() {
		return 1;
	}

}
