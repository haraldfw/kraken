/**
 * 
 */
package com.smokebox.kraken.weaponry.ammunition;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.smokebox.kraken.Game;
import com.smokebox.kraken.ability.ProjectileHittable;
import com.smokebox.kraken.effect.ParticleBurst_Explosion;
import com.smokebox.kraken.effect.ParticleBurst_ProjectileShatter;
import com.smokebox.lib.utils.Vector2;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public class Grenade implements Projectile {

	private Vector2 pos;
	private Vector2 vel;
	private Vector2 acc = new Vector2();
	private Vector2 forceAcm = new Vector2();
	
	private float inverseMass;
	
	private float radius;
	
	private boolean isAlive = true;
	
	private float timeAlive = 0;
	private float maxLife;
	
	private float explRadius;
	private float explStrength;
	private float explDamage;
	
	Game game;
	
	private Game.Team team;
	
	public Grenade(Vector2 pos, Vector2 vel, float inverseMass, float radius, 
			float range, Game.Team team, Game game, float explRadius,
			float explStrength, float explDamage) {
		this.pos = new Vector2(pos);
		this.vel = new Vector2(vel);
		
		this.inverseMass = inverseMass;
		this.radius = radius;
		maxLife = range;
		
		this.explRadius = explRadius;
		this.explStrength = explStrength;
		this.explDamage = explDamage;
		
		this.team = team;
		this.game = game;
	}
	
	@Override
	public void integrate(float delta) {
		timeAlive += delta;
		if(timeAlive > maxLife) {
			explode();
			isAlive = false;
		}
		vel.scl(0.95f*(1 - delta));
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
		sr.circle(pos.x, pos.y, radius, 4);
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
		explode();
		isAlive = false;
	}
	
	public void explode() {
		game.addAnimation(new ParticleBurst_Explosion(pos, explRadius, 10));
		game.addExplosion(pos, explRadius, explStrength, explDamage);
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
		} else if(penetration.y == 0) {
			vel.flipX();
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
	public Game.Team getTeam() {
		return team;
	}


}
