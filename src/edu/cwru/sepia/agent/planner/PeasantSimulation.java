package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class PeasantSimulation {

	private int unitId;
	private Position position;
	private int cargo;
	private ResourceType cargoType;
	
	public PeasantSimulation(UnitView unit){
		unitId = unit.getID();
		position = new Position(unit.getXPosition(), unit.getYPosition());
		cargo = unit.getCargoAmount();
		cargoType = unit.getCargoType();
	}
	
	public PeasantSimulation(Position pos, int cargo, ResourceType cargoType, int unitId){
		position = new Position(pos);
		this.cargo = cargo;
		this.cargoType = cargoType;
		this.unitId = unitId;
	}
	
	/**
	 * @return the unitId
	 */
	public int getUnitId() {
		return unitId;
	}

	/**
	 * @return the position
	 */
	public Position getPosition() {
		return position;
	}
	
	/**
	 * @return the cargo
	 */
	public int getCargo() {
		return cargo;
	}
	
	/**
	 * @return the cargoType
	 */
	public ResourceType getCargoType() {
		return cargoType;
	}
	
}
