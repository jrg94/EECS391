package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.environment.model.state.Unit;

public class UnitSimulation {
	private int xPosition;
	private int yPosition;
	private int unitID;
	private int currentHP;
	private int maxHP;
	
	public UnitSimulation(Unit.UnitView unit){
		xPosition=unit.getXPosition();
		yPosition=unit.getYPosition();
		unitID=unit.getID();
		currentHP=unit.getHP();
		maxHP=unit.getTemplateView().getBaseHealth();
	}
	

	/**
	 * @return the xPosition
	 */
	public int getXPosition() {
		return xPosition;
	}


	/**
	 * @param xPosition the xPosition to set
	 */
	public void setXPosition(int xPosition) {
		this.xPosition = xPosition;
	}


	/**
	 * @return the yPosition
	 */
	public int getYPosition() {
		return yPosition;
	}


	/**
	 * @param yPosition the yPosition to set
	 */
	public void setYPosition(int yPosition) {
		this.yPosition = yPosition;
	}


	/**
	 * @return the unitID
	 */
	public int getUnitID() {
		return unitID;
	}

	/**
	 * @return the currentHP
	 */
	public int getCurrentHP() {
		return currentHP;
	}

	/**
	 * @return the maxHP
	 */
	public int getMaxHP() {
		return maxHP;
	}

}
