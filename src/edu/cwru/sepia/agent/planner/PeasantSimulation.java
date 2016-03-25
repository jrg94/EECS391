package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class PeasantSimulation {

	private int x;
	private int y;
	private int cargo;
	private ResourceType cargoType;
	
	public PeasantSimulation(UnitView unit){
		x = unit.getXPosition();
		y = unit.getYPosition();
		cargo = unit.getCargoAmount();
		cargoType = unit.getCargoType();
	}
	
	public PeasantSimulation(int x, int y, int cargo, ResourceType cargoType){
		this.x = x;
		this.y = y;
		this.cargo = cargo;
		this.cargoType = cargoType;
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
