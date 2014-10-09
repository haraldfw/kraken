/**
 * 
 */
package com.smokebox.kraken.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.smokebox.kraken.Game;
import com.smokebox.kraken.ability.*;
import com.smokebox.kraken.weaponry.Weapon;
import com.smokebox.kraken.weaponry.ammunition.Projectile;
import com.smokebox.lib.utils.Vector2;
import com.smokebox.lib.utils.geom.Polygon;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public class Droid implements Moveable, Drawable, CollidesWithWorld, Wielding, ProjectileHittable, ImpulseAffected {
	
	public  Vector2 pos;
	private Vector2 vel = new Vector2();
	private Vector2 acc = new Vector2();
	private Vector2 forceAcm = new Vector2();
	private float inverseMass = 1f/6f;
	private float walkStrength = 40f;
	
	private Game game;
	
	private Vector2 wantedDirection = new Vector2();
	private Vector2 lookDir = new Vector2();
	private Vector2 steeringForce = new Vector2();
	
	private boolean attackStance = false;
	private boolean isAlive = true;
	
	public Weapon getWeapon() {return activeWeapon;}
	
	public float getActiveMaxHealth() {return 10;}
	public float getActiveInverseMass() {return inverseMass;}
	public float getActiveDamage() {return activeWeapon.wep_damage;}
	public float getActiveRange() {return activeWeapon.proj_range;}
	public float getActiveWalkStrength() {return walkStrength;}
	public float getActiveAccuracy() {return activeWeapon.wep_accuracy;}
	public float getActiveInverseRoundsPerSecond() {return 1/activeWeapon.rps_base;}
	public float getActiveProjectileSpeed() {return activeWeapon.proj_speed;}
	public float getActiveAimInfluence() {return activeWeapon.wep_aimInfluence;}
	
	float s = 0.4f;
	private Polygon shape = new Polygon(new Vector2[]{
			new Vector2(-1f*s, -1f*s),
			new Vector2(1.5f*s, -0.2f*s),
			new Vector2(1.3f*s, 0f*s),
			new Vector2(1.5f*s, 0.2f*s),
			new Vector2(-1f*s, 1f*s),
			new Vector2(-0.5f*s, 0f*s)
	});
	
	private float boundingRadius = 0.5f;
	
	private Weapon activeWeapon;
	
	private Game.Team team;
	
	public float getBoundingRadius() {
		return boundingRadius;
	}
	
	public Droid(Vector2 pos, Game.Team team, Game game) {
		this.game = game;
		this.pos = new Vector2(pos);
		shape.origin.set(0, 0);
		this.team = team;
	}
	
	public void setWeapon(Weapon newWeapon) {
		activeWeapon = newWeapon;
	}

	public void draw(ShapeRenderer sr) {
		lookDir.nlerp(getWantedLookDirection(), 0.2f);
		shape.setRotation(lookDir.getAngleAsRadians());
		sr.setColor(1, 1, 1, 1);
		sr.polygon(shape.getVerticesAsFloatArray(pos.x, pos.y));
		
		activeWeapon.draw(this.pos, 0, sr);
	}
	
	public void drawDebug(ShapeRenderer sr) {
		sr.circle(pos.x, pos.y, boundingRadius, 20);
		sr.setColor(1, 0, 0, 1);
		// Steeringforce RED
		sr.line(pos.x, pos.y, pos.x + steeringForce.x*0.001f, pos.y + steeringForce.y*0.001f);
		sr.setColor(0, 1, 0, 1);
		// WantedDirection GREEN
		sr.line(pos.x, pos.y, pos.x + wantedDirection.x, pos.y + wantedDirection.y);
		sr.setColor(0, 0, 1, 1);
		// Vel BLUE
		sr.line(pos.x, pos.y, pos.x + vel.x, pos.y + vel.y);
		
		sr.setColor(0.5f, 0, 1, 1);
		sr.line(pos.x, pos.y, pos.x + lookDir.x, pos.y + lookDir.y);
	}
	
	public void integrate(float delta) {
		attackStance = Gdx.input.isButtonPressed(Buttons.RIGHT);
		
		vel.addScaledVector(acc, delta);
		//float lookScl = (float) (1 - (1 / (Math.abs(steeringForce.getAngleTo(lookDir) - 2*Math.PI) )));
		pos.addScaledVector(vel, delta);
		acc.clear();
		
		if(inverseMass <= 0) return; // If mass is infinite, do not continue
		
		// TODO handle damping
		//addForce(new Vector2(vel).pow(2).flip());
		addForce(getSteeringForce(inverseMass));
		acc.addScaledVector(forceAcm, inverseMass);
		forceAcm.clear();
	}
	
	private Vector2 getSteeringForce(float inverseMass) {
		// TODO Make it so looking in one direction while walking the other
		//  slow you down. (difference between lookDirection and walkingDirection
		
		//float angle = (float)((Math.acos(move.getDotProduct(look)))/(move.getMag()*look.getMag()));
		steeringForce = getWantedMoveDirection().scl(walkStrength*inverseMass*2).sub(vel).scl(walkStrength);
		return steeringForce;
	}
	
	private Vector2 getWantedMoveDirection() {
		Vector2 v;
		if(Gdx.input.isKeyPressed(Keys.Q)) v = new Vector2(game.getPlayerPos()).sub(pos).nor();
		else v = new Vector2();
		return v;
	}
	
	public void attack() {
		activeWeapon.attack(this.pos, new Vector2(1, 1), this);
	}
	
	private Vector2 getWantedLookDirection() {
		Vector2 toPlayer = game.toPlayer(pos);
		return attackStance ? new Vector2(toPlayer) : getWantedMoveDirection().nor();
	}

	public void handleWallIntersection(Vector2 penetration) {
		pos.add(penetration);
		vel.add(new Vector2(penetration).scl(vel.getMag()));
	}

	public void handleOutsideWalls(Vector2 moveBy) {
		pos.add(moveBy);
	}

	public void addForce(Vector2 force) {
		forceAcm.add(force);
	}

	public Vector2 getPos() {
		return pos;
	}

	public Vector2 getVel() {
		return vel;
	}

	public boolean isAlive() {
		return isAlive;
	}

	@Override
	public void damage(float damage) {
		
	}

	@Override
	public void handleGettingHit(Projectile ammo) {
		
	}

	@Override
	public void setVel(Vector2 newVel) {
		vel.set(newVel);
	}

	@Override
	public Game.Team getTeam() {
		return team;
	}

	@Override
	public float getInverseMass() {
		return inverseMass;
	}

	@Override
	public int getActiveBurstLength() {
		return activeWeapon.burst_length;
	}

	@Override
	public boolean getBurstStatus() {
		return activeWeapon.burst_active;
	}

	@Override
	public float getActiveBurstRpsScale() {
		return activeWeapon.burst_rpsScale;
	}

	@Override
	public float getActiveBurstDelay() {
		return activeWeapon.burst_delay;
	}
}
