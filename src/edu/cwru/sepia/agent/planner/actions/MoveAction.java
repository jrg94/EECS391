package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantSimulation;
import edu.cwru.sepia.agent.planner.Position;

public class MoveAction implements StripsAction{

	private PeasantSimulation peasant;
	private Position destinationPosition;
	
	public MoveAction(PeasantSimulation peasant, Position destinationPosition){
		this.peasant = peasant;
		this.destinationPosition = destinationPosition;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		
		return !peasant.getPosition().equals(destinationPosition);
		//TODO add another condition that checks if there's already a peasant in the way
	}

	@Override
	public GameState apply(GameState state) {
		PeasantSimulation peasantClone = new PeasantSimulation(destinationPosition, peasant.getCargo(), peasant.getCargoType(), peasant.getUnitId());
		GameState nextGameState = new GameState(state, this);
		nextGameState.getPeasantMap().put(peasantClone.getUnitId(), peasantClone);
		return nextGameState;
	}

}
