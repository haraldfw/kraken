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
public class Player implements Moveable, Drawable, CollidesWithWorld,
		Wielding, ProjectileHittable, ImpulseAffected {

	public  Vector2 pos;
	private Vector2 vel = new Vector2();
	private Vector2 acc = new Vector2();
	private Vector2 forceAcm = new Vector2();
	
	private Game game;
	
	private Vector2 lookDir = new Vector2();

	private Vector2 steeringForce = new Vector2();		// Debugging
	private Vector2 wantedDirection = new Vector2();	// Debugging
	
	private boolean aiming = false;
	private boolean isAlive = true;
	
	float s = 0.4f;
	private Polygon shape = new Polygon(new Vector2[]{
			new Vector2(-1f*s, -1f*s),
			new Vector2(1.5f*s, 0f*s),
			new Vector2(-1f*s, 1f*s),
			new Vector2(-0.5f*s, 0f*s)
	});
	
	private float boundingRadius = 0.5f;
	
	private Weapon[] weapons = new Weapon[20];
	private int activeWeapon = 0;
	private boolean spaceLock = false;
	
	private float timeSinceLastShot = 0;
											// minimum/maximum
	private float mod_mass = 0;				// further down
	private int mod_maxHealth = 0; 			// [1, ->
	private float mod_damage = 0; 			// further down
	private float mod_range = 0; 			// further down
	private float mod_roundsPerSecond = 0; 	// in weapon
	private float mod_aimInfluence = 0; 	// [0, 1>
	private float mod_walkStrength = 0; 	// further down
	private float mod_accuracy = 0; 		// [0, 1>
	private float mod_projectileSpeed = 0; 	// further down
	private int mod_burstLength = 0;
	private float mod_burstRpsScale = 0;
	private float mod_burstDelay = 0;
	private boolean mod_burstOverride = false;
	
	private boolean midBurst = false;
	
	private int burstProgress = 0;
	
	// health
	private int base_maxHealth = 10;
	private int health = (int) Math.round(base_maxHealth);
	
	// ProjectileSpeed. BaseStat in weapon
	private float max_projectileSpeed = 50f;
	private float min_projectileSpeed = 1f;
	
	// Damage
	private float min_damage = 1;
	
	// Range
	private float min_range = 0.2f;
	
	private float base_mass = 5f;
	private float max_mass = 10f;
	private float min_mass = 2f;
	// walkStrength-values
	private float base_walkStrength = 40f; // Newtons, experiment with different numbers
	private float max_walkStrength = 600f;
	private float min_walkStrength = 25f;
	
	private Game.Team team;
	
	// These method calculate active stats and returns the calculation
	public float getActiveInverseMass() {
		float f = base_mass + mod_mass/3f;
		if(f > max_mass) f = max_mass;
		else if(f < min_mass) f = min_mass;
		return 1/f;
	}
	
	public float getActiveDamage() {
		float f = weapons[activeWeapon].wep_damage + mod_damage;
		if(f < min_damage) f = min_damage;
		return f;
	}
	
	public float getActiveRange() {
		float f = weapons[activeWeapon].proj_range + mod_range;
		if(f < min_range) f = min_range;
		return f;
	}
	
	public float getActiveMaxHealth() {
		float f = base_maxHealth + mod_maxHealth;
		if(f < 1) f = 1;
		return f;
	}
	
	public float getActiveWalkStrength() {
		float f = base_walkStrength + mod_walkStrength*5;
		if(f > max_walkStrength) f = max_walkStrength;
		else if(f < min_walkStrength) f = min_walkStrength;
		return f;
	}
	
	public float getActiveAccuracy() { // make it so this goes asymptotic towards max
		float f = weapons[activeWeapon].wep_accuracy + mod_accuracy*0.05f;
		if(f > 1) f = 1;
		else if(f < 0) f = 0;
		return f;
	}
	
	public float getActiveInverseRoundsPerSecond() {
		Weapon w = weapons[activeWeapon];
		float f = w.rps_base + mod_roundsPerSecond*0.25f;
		if(getBurstStatus()) f *= getActiveBurstRpsScale();
		if(f > w.rps_max) f = w.rps_max;
		else if(f < w.rps_min) f = w.rps_min;
		return 1/f;
	}
	
	public float getActiveProjectileSpeed() {
		// Should probably have a limit
		Weapon w = weapons[activeWeapon];
		float f = w.proj_speed + mod_projectileSpeed;
		if(f > max_projectileSpeed) f = max_projectileSpeed;
		else if(f < min_projectileSpeed) f = min_projectileSpeed;
		return f;
	}
	
	public float getActiveAimInfluence() { // make it so this goes asymptotic towards max
		float f = weapons[activeWeapon].wep_aimInfluence + mod_aimInfluence*0.1f;
		if(f < 0) f = 0;
		else if(f > 1) f = 1;
		return f;
	}

	@Override
	public int getActiveBurstLength() {
		return weapons[activeWeapon].burst_length + mod_burstLength;
	}

	@Override
	public boolean getBurstStatus() {
		return mod_burstOverride || weapons[activeWeapon].burst_active;
	}

	@Override
	public float getActiveBurstRpsScale() {
		return weapons[activeWeapon].burst_rpsScale + mod_burstRpsScale;
	}

	@Override
	public float getActiveBurstDelay() {
		return weapons[activeWeapon].burst_delay + mod_burstDelay;
	}
	
	public Player(Vector2 pos, Game game) {
		this.pos = new Vector2(pos);
		this.game = game;
		shape.origin.set(0, 0);
		team = Game.Team.PLAYER;
		
		// TODO put stats in xml-document
	}
	
	public void setWeapon(Weapon newWeapon) {
		weapons[activeWeapon] = newWeapon;
	}
	
	public void draw(ShapeRenderer sr) {
		lookDir.nlerp(getWantedLookDirection(), 0.2f);
		shape.setRotation(lookDir.getAngleAsRadians());
		sr.setColor(1, 1, 1, 1);
		sr.polygon(shape.getVerticesAsFloatArray(pos.x, pos.y));
		
		weapons[activeWeapon].draw(this.pos, game.toMouse().getAngleAsRadians(), sr);
	}
	
	public void drawDebug(ShapeRenderer sr) {
		sr.circle(pos.x, pos.y, boundingRadius, 20);
		sr.setColor(1, 0, 0, 1);
		// Steeringforce RED
		sr.line(pos.x, pos.y, pos.x + steeringForce.x*0.01f, pos.y + steeringForce.y*0.01f);
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
		aiming = Gdx.input.isButtonPressed(Buttons.RIGHT);
		timeSinceLastShot += delta;
		if(Gdx.input.isKeyPressed(Keys.SPACE) && ! spaceLock) {
			switchWeapon();
			spaceLock = true;
		}
		if(!Gdx.input.isKeyPressed(Keys.SPACE)) spaceLock = false;
		
		if(midBurst && timeSinceLastShot > getActiveInverseRoundsPerSecond()) {
			burstProgress++;
			weapons[activeWeapon].attack(this.pos, game.toMouse(), this);
			timeSinceLastShot = 0;
			if(burstProgress >= getActiveBurstLength()) {
				burstProgress = 0;
				midBurst = false;
				timeSinceLastShot -= getActiveBurstDelay();
			}
		}
		
		//vel.scl(0.9f*(1 -delta));
		vel.addScaledVector(acc, delta);
		//float lookScl = (float) (1 - (1 / (Math.abs(steeringForce.getAngleTo(lookDir) - 2*Math.PI) )));
		pos.addScaledVector(vel, delta);
		acc.clear();
		
		float im = getActiveInverseMass();
		if(im <= 0) return; // If mass is infinite, do not continue
		
		// TODO handle damping
		//addForce(new Vector2(vel).pow(2).flip());
		addForce(getSteeringForce(im));
		acc.addScaledVector(forceAcm, im);
		forceAcm.clear();
	}
	
	private Vector2 getSteeringForce(float im) {
		// TODO Make it so looking in one direction while walking the other
		//  slow you down. (difference between lookDirection and walkingDirection
		
		//float angle = (float)((Math.acos(move.getDotProduct(look)))/(move.getMag()*look.getMag()));
		Vector2 dir = getWantedMoveDirection();
		steeringForce = dir.scl(base_walkStrength*im*2).sub(vel).scl(base_walkStrength);
		return steeringForce;
	}
	
	private Vector2 getWantedMoveDirection() {
		return new Vector2(
				Gdx.input.isKeyPressed(Keys.D) ? 1 :
					Gdx.input.isKeyPressed(Keys.A) ? -1 : 0,
				Gdx.input.isKeyPressed(Keys.W) ? 1 :
					Gdx.input.isKeyPressed(Keys.S) ? -1 : 0
				).nor();
	}
	
	public void attack() {
		boolean burstStatus = getBurstStatus();
		if(burstStatus) {
			midBurst = true;
		} else
		if(timeSinceLastShot > getActiveInverseRoundsPerSecond()) {
			weapons[activeWeapon].attack(this.pos, game.toMouse(), this);
			timeSinceLastShot = 0;
		}
	}
	
	public void addWeapon(Weapon w) {
		int nextWeapon = activeWeapon + 1;
		if(nextWeapon >= weapons.length) nextWeapon = 0;
		if(weapons[nextWeapon] == null) {
			weapons[nextWeapon] = w;
			activeWeapon = nextWeapon;
		} else {
			weapons[activeWeapon] = w;
		}
	}
	
	private void switchWeapon() {
		int oldWeapon = activeWeapon;
		activeWeapon++;
		if(activeWeapon >= weapons.length || weapons[activeWeapon] == null) activeWeapon = 0;
	}
	
	private Vector2 getWantedLookDirection() {
		Vector2 toMouse = game.toMouse();
		return aiming ? new Vector2(toMouse) : getWantedMoveDirection().nor();
	}
	
	public void addForce(Vector2 force) {
		forceAcm.add(force);
	}

	public Vector2 getPos() {
		return pos;
	}

	public float getBoundingRadius() {
		return boundingRadius;
	}

	public Vector2 getVel() {
		return vel;
	}

	public void handleWallIntersection(Vector2 penetration) {
		pos.add(penetration);
		vel.add(new Vector2(penetration).scl(vel.getMag()));
	}

	public void handleOutsideWalls(Vector2 moveBy) {
		pos.add(moveBy);
	}
	
	public boolean isAlive() {
		return isAlive;
	}
	
	public Weapon getWeapon() {
		return weapons[activeWeapon];
	}

	public void handleGettingHit(Projectile ammo) {
		
	}
	
	public void damage(float amount) {
		health -= (int) amount;
	}
	
	public Game.Team getTeam() {
		return team;
	}

	@Override
	public void setVel(Vector2 newVel) {
		vel.set(newVel);
	}
	
	public float getInverseMass() {
		return getActiveInverseMass();
	}
}
