/**
 * 
 */
package com.smokebox.kraken.ability;

import com.smokebox.kraken.Game;
import com.smokebox.kraken.weaponry.ammunition.Projectile;
import com.smokebox.lib.utils.Vector2;


/**
 * @author Harald Floor Wilhelmsen
 *
 */
public interface ProjectileHittable {

	public Game.Team getTeam();
	public void handleGettingHit(Projectile ammo);
	public float getBoundingRadius();
	public float getInverseMass();
	
	public Vector2 getPos();
	public Vector2 getVel();
	public void setVel(Vector2 newVel);
	public void addForce(Vector2 force);
}
