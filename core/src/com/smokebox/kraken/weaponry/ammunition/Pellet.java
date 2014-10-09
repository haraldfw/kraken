/**
 * 
 */
package com.smokebox.kraken.weaponry.ammunition;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.smokebox.kraken.Game;
import com.smokebox.kraken.ability.ProjectileHittable;
import com.smokebox.kraken.effect.ParticleBurst_ProjectileShatter;
import com.smokebox.lib.utils.Vector2;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public class Pellet implements Projectile {
	
	private Vector2 pos;
	private Vector2 vel;
	private Vector2 acc = new Vector2();
	private Vector2 forceAcm = new Vector2();
	
	private float inverseMass;
	
	private float radius;
	
	private boolean isAlive = true;
	
	private float timeAlive = 0;
	private float maxLife;
	
	Game game;
	
	private int bounces = 0;
	private int maxBounces = 2;
	
	private Game.Team team;
	
	private float speedScale;
	
	public Pellet(Vector2 pos, Vector2 vel, float inverseMass, float radius, 
			float range, float speedScale, Game.Team team, Game game) {
		this.pos = new Vector2(pos);
		this.vel = new Vector2(vel);
		
		this.inverseMass = inverseMass;
		this.radius = radius;
		maxLife = range;
		
		this.game = game;
		this.team = team;
		
		this.speedScale = speedScale;
	}
	
	@Override
	public void integrate(float delta) {
		timeAlive += delta;
		if(timeAlive > maxLife) {
			game.addAnimation(new ParticleBurst_ProjectileShatter(pos, vel, 2, radius));
			isAlive = false;
		}
		vel.scl(speedScale*(1 - delta));
		vel.addScaledVector(acc, delta);
		pos.addScaledVector(vel, delta);
		acc.clear();
		
		if(inverseMass <= 0) return; // If mass is infinite, do not continue
		
		forceAcm.scl(inverseMass);
		acc.add(forceAcm);
		forceAcm.clear();
	}

	@Override
	public void draw(ShapeRenderer sr) {
		sr.circle(pos.x, pos.y, radius, 9);
	}

	@Override
	public float getBoundingRadius() {
		return radius;
	}

	@Override
	public void handleWallIntersection(Vector2 penetration) {
		pos.add(penetration);
		if(penetration.x == 0) {
			vel.flipY();
			bounces++;
		} else if(penetration.y == 0) {
			vel.flipX();
			bounces++;
		}
		if(bounces > maxBounces) {
			isAlive = false;
		}
		game.addAnimation(
			new ParticleBurst_ProjectileShatter(
				new Vector2(pos).add(penetration), 
				penetration.nor().scl(Math.random()), 
				1,
				radius
			)
		);
	}

	@Override
	public void handleOutsideWalls(Vector2 moveBy) {
		pos.add(moveBy);
	}

	@Override
	public void addForce(Vector2 force) {
		forceAcm.add(force);
	}

	@Override
	public Vector2 getPos() {
		return pos;
	}

	@Override
	public Vector2 getVel() {
		return vel;
	}
	
	@Override
	public boolean isAlive() {
		return isAlive;
	}
	
	@Override
	public float getInverseMass() {
		return inverseMass;
	}

	@Override
	public void handleHitting(ProjectileHittable character) {
		// Relative velocity
		Vector2 rv =  new Vector2(character.getVel()).sub(vel);
		Vector2 normal = new Vector2(character.getPos()).sub(this.pos).nor();
		
		float velAlongNormal = rv.getDotProduct(normal);
		if(velAlongNormal > 0) return;
		
		float j = -2*velAlongNormal / (inverseMass + character.getInverseMass());
		
		Vector2 impulse = normal.scl(j);
		vel.addScaledVector(impulse, -inverseMass);
		character.getVel().addScaledVector(impulse, character.getInverseMass());
		bounces++;
	}
	
	@Override
	public Game.Team getTeam() {
		return team;
	}
}
