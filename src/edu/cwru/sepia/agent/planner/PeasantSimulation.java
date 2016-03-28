package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class PeasantSimulation {

	private int unitId;
	private Position position;
	private int cargo;
	private ResourceType cargoType;
	
	private boolean isCarrying;
	
	public PeasantSimulation(UnitView unit){
		unitId = unit.getID();
		position = new Position(unit.getXPosition(), unit.getYPosition());
		cargo = unit.getCargoAmount();
		cargoType = unit.getCargoType();
		if (cargoType == null){
			isCarrying = true;
		}
		else{
			isCarrying = false;
		}
	}
	
	public PeasantSimulation(Position pos, int cargo, ResourceType cargoType, int unitId){
		position = new Position(pos);
		this.cargo = cargo;
		this.cargoType = cargoType;
		this.unitId = unitId;
		if (cargoType == null){
			isCarrying = true;
		}
		else{
			isCarrying = false;
		}
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

	public boolean isCarrying() {
		return isCarrying;
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
		PeasantSimulation other = (PeasantSimulation) obj;
		if (cargo != other.cargo)
			return false;
		if (cargoType != other.cargoType)
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (unitId != other.unitId)
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cargo;
		result = prime * result + ((cargoType == null) ? 0 : cargoType.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + unitId;
		return result;
	}
	
	@Override
	public String toString(){
		return String.format("PeasantSimulation:{id=%d, position=%s, cargoType=%s, cargo=%d}", unitId, position.toString(), cargoType.toString(), cargo);
	}
}
