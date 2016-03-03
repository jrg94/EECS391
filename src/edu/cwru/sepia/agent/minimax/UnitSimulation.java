package edu.cwru.sepia.agent.minimax;


import java.util.Random;

import edu.cwru.sepia.environment.model.state.Unit;

public class UnitSimulation {
	private int xPosition;
	private int yPosition;
	private int id;
	private int currentHP;
	private int maxHP;
	private String name;
	private int range;
	
	private int basicDamage;
	// "Does anyone else miss chaos damage?"
	private int piercingDamage; 
	private int armor;
	
	



	public UnitSimulation(Unit.UnitView unit){
		xPosition=unit.getXPosition();
		yPosition=unit.getYPosition();
		id=unit.getID();
		currentHP=unit.getHP();
		maxHP=unit.getTemplateView().getBaseHealth();
		name=unit.getTemplateView().getName().toLowerCase();
		range=unit.getTemplateView().getRange();
		basicDamage=unit.getTemplateView().getBasicAttack();
		piercingDamage=unit.getTemplateView().getPiercingAttack();
		armor=unit.getTemplateView().getArmor();
	}
	
	/**
	 * constructor for deep cloning
	 * @param unit
	 */
	public UnitSimulation (UnitSimulation unit){
		xPosition=unit.getXPosition();
		yPosition=unit.getYPosition();
		id=unit.getID();
		currentHP=unit.getCurrentHP();
		maxHP=unit.getMaxHP();
		name=unit.getName();
		range=unit.getRange();
		basicDamage=unit.basicDamage;
		piercingDamage=unit.piercingDamage;
		armor=unit.armor;
	}
	

	/**
	 * @return the armor
	 */
	public int getArmor() {
		return armor;
	}


	/**
	 * @return the basicDamage
	 */
	public int getBasicDamage() {
		return basicDamage;
	}


	/**
	 * @return the piercingDamage
	 */
	public int getPiercingDamage() {
		return piercingDamage;
	}


	/**
	 * @return the xPosition
	 */
	public int getXPosition() {
		return xPosition;
	}


	/**
	 * @param x the x to move horizontally by
	 */
	public void moveXBy(int x){
		this.xPosition += x;
	}


	/**
	 * @return the yPosition
	 */
	public int getYPosition() {
		return yPosition;
	}


	/**
	 * @param y the y to move vertically by
	 */
	public void moveYBy(int y) {
		this.yPosition += y;
	}


	/**
	 * @return the unitID
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return the currentHP
	 */
	public int getCurrentHP() {
		return currentHP;
	}
	
	/**
	 * decrement the current hp by the damage inflicted by an attack
	 * @param attackDamage
	 */
	public void decrementHP(int attackDamage){
		currentHP -= attackDamage;
	}

	/**
	 * @return the maxHP
	 */
	public int getMaxHP() {
		return maxHP;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the range
	 */
	public int getRange() {
		return range;
	}
	
	/**
	 * The Equation
	When one unit attacks another, the formula used to determine damage is:


	(Basic Damage - Target's Armor) + Piercing Damage = Maximum damage inflicted
	The attacker does a random amount of damage from 50%-100% of this total each attack.
	Source: http://classic.battle.net/war2/basic/combat.shtml
	 */
	
	/**
	 * The damage calculation without the rng 
	 * @param enemyArmor
	 * @return
	 */
	public int damageCalculation(int enemyArmor){
		return Math.max(basicDamage - enemyArmor, 0) + piercingDamage;
	}
	
	/**
	 * The damage calculation with the rng
	 * @param enemyArmor
	 * @return
	 */
	public int randomDamageCalculation (int enemyArmor){
		Random r = new Random();
		double rngPercent = .5 + .5*r.nextDouble();
		return (int)(rngPercent*damageCalculation(enemyArmor));
	}
	
	/**
	 * The damage calculation with expected value for rng (average)
	 * @param enemyArmor
	 * @return
	 */
	public int expectedDamageCalculation (int enemyArmor){
		return (int) (.75*damageCalculation(enemyArmor));
	}
}
