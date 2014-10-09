/**
 * 
 */
package com.smokebox.kraken.character;

import com.smokebox.kraken.Game;
import com.smokebox.kraken.weaponry.Weapon;
import com.smokebox.lib.utils.Vector2;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public interface Wielding {

	public Weapon getWeapon();
	public Game.Team getTeam();
	public Vector2 getPos();
	public void addForce(Vector2 force);
	
	public float getActiveInverseMass();
	public float getActiveDamage();
	public float getActiveRange();
	public float getActiveMaxHealth();
	public float getActiveWalkStrength();
	public float getActiveAccuracy();
	public float getActiveInverseRoundsPerSecond();
	public float getActiveProjectileSpeed();
	public float getActiveAimInfluence();
	public int getActiveBurstLength();
	public boolean getBurstStatus();
	public float getActiveBurstRpsScale();
	public float getActiveBurstDelay();
}