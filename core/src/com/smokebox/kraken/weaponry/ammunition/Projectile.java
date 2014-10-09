/**
 * 
 */
package com.smokebox.kraken.weaponry.ammunition;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.smokebox.kraken.Game;
import com.smokebox.kraken.ability.ProjectileHittable;
import com.smokebox.lib.utils.Vector2;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public interface Projectile {
	
	public Game.Team getTeam();
	
	public void integrate(float delta);

	public void draw(ShapeRenderer sr);

	public void addForce(Vector2 force);

	public Vector2 getPos();
	public Vector2 getVel();
	public boolean isAlive();
	public float getInverseMass();

	public void handleHitting(ProjectileHittable character);
	
	public float getBoundingRadius();
	public void handleWallIntersection(Vector2 penetration);
	public void handleOutsideWalls(Vector2 closestDistance);
}
