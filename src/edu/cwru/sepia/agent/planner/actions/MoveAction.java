package edu.cwru.sepia.agent.planner.actions;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantSimulation;
import edu.cwru.sepia.agent.planner.Position;

public class MoveAction implements StripsAction{

	private Position destinationPosition;
	private Map<Integer, Position> destinationPositionMap;
	private int cost;
	
	private static final int PEASANT_MOVE_DURATION = 16;
	
	public MoveAction(Position destinationPosition){
		this.destinationPosition = destinationPosition;
		destinationPositionMap = new HashMap<Integer, Position>();
		cost = 0;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		boolean condition = true;
		for (PeasantSimulation peasant : state.getPeasantMap().values()){
			condition &= !peasant.getPosition().equals(destinationPosition);
		}
		return condition;
		//TODO add another condition that checks if there's already a peasant in the way
	}

	@Override
	public GameState apply(GameState state) {
		GameState nextGameState = new GameState(state, this);

		/**
		 * Sorting approach takes NlogN time compared to 
		 * trying to find the closest adjacent position for each peasant N^2
		 */
		List<Position> closestPositions = destinationPosition.getAdjacentPositions();
		closestPositions.sort(new Comparator<Position>(){
			//this sorts the list from smallest distance to greatest
			@Override
			public int compare(Position arg0, Position arg1) {
				return state.getTownHall().getPosition().chebyshevDistance(arg0) - state.getTownHall().getPosition().chebyshevDistance(arg1);
			}
		});
		int i = 0;
		for (PeasantSimulation peasant : state.getPeasantMap().values()){
			Position gatherPosition = new Position(closestPositions.get(i));
			PeasantSimulation peasantClone = new PeasantSimulation(gatherPosition, peasant.getCargo(), peasant.getCargoType(), peasant.getUnitId());
			nextGameState.getPeasantMap().put(peasantClone.getUnitId(), peasantClone);
			i++;
			
			cost += peasant.getPosition().chebyshevDistance(gatherPosition);
		}
		return nextGameState;
	}

	/**
	 * cost of moving is more than 1
	 * returns the average chebyshev distance between peasant and the destination
	 */
	@Override
	public int cost() {
		return cost/destinationPositionMap.size();
	}

	/**
	 * @return the destinationPosition
	 */
	public Position getDestinationPosition() {
		return destinationPosition;
	}

	/**
	 * @return the destinationPositionMap
	 */
	public Map<Integer, Position> getDestinationPositionMap() {
		return destinationPositionMap;
	}

}
