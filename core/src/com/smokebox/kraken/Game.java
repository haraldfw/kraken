/**
 * 
 */
package com.smokebox.kraken;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.smokebox.kraken.ability.*;
import com.smokebox.kraken.character.Droid;
import com.smokebox.kraken.character.Player;
import com.smokebox.kraken.effect.Effect;
import com.smokebox.kraken.screen.HUD;
import com.smokebox.kraken.weaponry.*;
import com.smokebox.kraken.weaponry.ammunition.Projectile;
import com.smokebox.kraken.world.World;
import com.smokebox.lib.utils.Intersect;
import com.smokebox.lib.utils.Vector2;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public class Game implements ApplicationListener {

	OrthographicCamera cam;
	ShapeRenderer sr;
	
	private Vector2 camPos = new Vector2();
	
	World world;
	ArrayList<Moveable> moveables;
	ArrayList<Drawable> drawables;
	ArrayList<CollidesWithWorld> collidables;
	ArrayList<Projectile> projectiles;
	ArrayList<ProjectileHittable> projHtbls;
	ArrayList<ImpulseAffected> explAffs;
	
	ArrayList<Effect> effects;
	
	public enum Team {PLAYER, NEUTRAL, ENEMY}
	
	/*
	 * TODO
	 * 	Add poise. This is a threshold-stat kept in the characters.
	 * 		On hit by projectile or the likes, basically when a
	 * 		character is affected by an impulse. Check to see if
	 * 		the character is to be stunned. Poise-recovery uses
	 * 		lerp(or just a plain scalar) to reduce activePoise
	 * 		until it is zero again. All impulses add some stun-
	 * 		value to the activePoise-thing. If activePoise
	 * 		surpasses poise, then the character is stunned for a small 
	 * 		amount of time until he/she/it completely recovers.
	 */
	Weapon[] weapons;
	private enum Wep{
		ASSAULTRIFLE,
		GRENADELAUNCHER,
		MACHINEGUN,
		REVOLVER,
		SHOTGUN,
		SUBMACHINEGUN
		};
	
	HUD hud;
	
	public Vector2 mousePos = new Vector2();
	public Vector2 toMouse = new Vector2();
	
	private Player player; // Only to use for values
	// do not use this to check collisions and or integrate
	
	public int wTiles = 32;
	public int hTiles = 24;
	private float tileSize;
	
	private boolean mouseLockUntilRelease = false;
	
	@Override
	public void create() {
		cam = new OrthographicCamera();
		sr = new ShapeRenderer();
		cam.setToOrtho(false, wTiles, hTiles);
		sr.setProjectionMatrix(cam.combined);
		
		moveables = new ArrayList<Moveable>();
		drawables = new ArrayList<Drawable>();
		collidables = new ArrayList<CollidesWithWorld>();
		projectiles = new ArrayList<Projectile>();
		explAffs = new ArrayList<ImpulseAffected>();
		projHtbls = new ArrayList<ProjectileHittable>();

		effects = new ArrayList<Effect>();

		XmlReader reader = new XmlReader();
		Element root = null;
		try {
			FileHandle f = Gdx.files.internal("stats.xml");
			System.out.println(f);
			root = reader.parse(f);
		} catch(IOException e) {
			e.printStackTrace();
		}

		weapons = new Weapon[] {
				new AssaultRifle(getOwnerByName("assaultRifle", root), this),
				new GrenadeLauncher(getOwnerByName("grenadeLauncher", root), this),
				new MachineGun(getOwnerByName("machineGun", root), this),
				new Revolver(getOwnerByName("revolver", root), this),
				new Shotgun(getOwnerByName("shotgun", root), this),
				new SubMachineGun(getOwnerByName("smg", root), this)
			};
		
		player = new Player(new Vector2(), this);
		player.setWeapon(weapons[Wep.GRENADELAUNCHER.ordinal()]);
		player.addWeapon(weapons[Wep.ASSAULTRIFLE.ordinal()]);
		player.addWeapon(weapons[Wep.REVOLVER.ordinal()]);
		player.addWeapon(weapons[Wep.SHOTGUN.ordinal()]);
		player.addWeapon(weapons[Wep.SUBMACHINEGUN.ordinal()]);

		moveables.add(player);
		drawables.add(player);
		collidables.add(player);
		explAffs.add(player);
		projHtbls.add(player);
		
		Droid d = new Droid(new Vector2(), Team.ENEMY, this);
		d.setWeapon(weapons[Wep.SUBMACHINEGUN.ordinal()]);
		moveables.add(d);
		drawables.add(d);
		collidables.add(d);
		explAffs.add(d);
		projHtbls.add(d);
		
		tileSize = Gdx.graphics.getWidth()/(float)wTiles;
		
		world = new World(150, (long)(Math.random()*10000l), this);
		
		hud = new HUD(player, this);
		
		Gdx.gl20.glLineWidth(2);
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		float delta = Gdx.graphics.getDeltaTime();
		
		if(Gdx.input.isButtonPressed(Buttons.LEFT) && !mouseLockUntilRelease) {
			player.attack();
		}
		if(!Gdx.input.isButtonPressed(Buttons.LEFT)) {
			mouseLockUntilRelease = false;
		}
		
		for(int i = 0; i < moveables.size();) {
			Moveable e = moveables.get(i);
			if(!e.isAlive()) {
				moveables.remove(i);
			} else {
				e.integrate(delta);
				i++;
			}
		}
		
		for(int i = 0; i < projectiles.size();) {
			Projectile p = projectiles.get(i);
			if(!p.isAlive()) {
				projectiles.remove(i);
			} else {
				p.integrate(delta);
				world.handleProjectileWithWalls(p);
				i++;
			}
		}
		
		world.calulateIntersectionForces(collidables);
		
// DRAW --------------------------------------------------------------------------------
		sr.begin(ShapeType.Line);
		
		for(int i = 0; i < drawables.size();) {
			Drawable e = drawables.get(i);
			if(!e.isAlive()) {
				drawables.remove(i);
			} else {
				e.draw(sr);
				i++;
			}
		}
		
		for(Projectile p : projectiles) {
			p.draw(sr);
			handleProjectileWithEntities(p, projHtbls);
		}
		
		for(int i = 0; i < effects.size();) {
			Effect e = effects.get(i);
			if(e.isFinished()) {
				effects.remove(i);
			} else {
				e.draw(sr);
				e.update(delta);
				i++;
			}
		}
		
		// Draw fancy walls
		world.drawFancyWalls(sr, camPos, 5);
		
		// Update camera position
		System.out.println(player.pos.x + ", " + player.pos.y);
		camPos.lerp(new Vector2(player.pos).add(new Vector2(mousePos).sub(player.pos).scl(player.getActiveAimInfluence())), 0.3f);
		cam.position.set(camPos.x, camPos.y, 0);
		
		// Apply camera changes
		cam.update();
		sr.setProjectionMatrix(cam.combined);
		
		
		
		
		// Set mousePos-vector's values
		mousePos.set(camPos.x + Gdx.input.getX()/tileSize - wTiles/2, camPos.y + hTiles/2f - Gdx.input.getY()/tileSize);
		
		// Update toMouse-vector's values
		toMouse.set(new Vector2(mousePos).sub(player.pos)).nor();
		
		// Draw crosshair
		float rd = 1.1f;
		float s1 = 0.4f*rd;
		float s2 = 0.8f*rd;
		sr.setColor(1, 1, 1, 1);
		sr.circle(mousePos.x, mousePos.y, 0.1f, 8);
		sr.line(mousePos.x, mousePos.y + s1, mousePos.x, mousePos.y + s2);
		sr.line(mousePos.x, mousePos.y - s1, mousePos.x, mousePos.y - s2);
		sr.line(mousePos.x + s1, mousePos.y, mousePos.x + s2, mousePos.y);
		sr.line(mousePos.x - s1, mousePos.y, mousePos.x - s2, mousePos.y);
		
		// Draw HUD
		hud.draw(sr);
		
		// Draw line to closest wall ------------------ DEBUG
//		sr.setColor(1, 0.3f, 0, 1);
//		Vector2 p = player.pos;
//		Vector2 closestDist = world.getClosestWall(world.getWalls(), player.pos).line.getMinimumDistance(player.pos);
//		sr.line(p.x, p.y, p.x + closestDist.x, p.y + closestDist.y);
		// ------------------------------------------- /DEBUG
		
		sr.end();
		
//		System.out.println("Lists:" 
//				+ "\nCollides:\t" + collidables.size() 
//				+ "\nDrawables:\t" + drawables.size()
//				+ "\nMoveables:\t" + moveables.size());
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void dispose() {
		
	}
	
	public void handleProjectileWithEntities(Projectile p, ArrayList<ProjectileHittable> ents) {
		Vector2 p1 = p.getPos();
		for(ProjectileHittable e : ents) {
			Vector2 p2 = e.getPos();
			if(Intersect.circleCircle(p1.x, p1.y, p.getBoundingRadius(), p2.x, p2.y, e.getBoundingRadius())) {
				e.handleGettingHit(p);
				p.handleHitting(e);
			}
		}
	}
	
	public void addExplosion(Vector2 pos, float radius, float strength, float damage) {
		for(ImpulseAffected m : explAffs) {
			Vector2 distVec = new Vector2(m.getPos()).sub(pos);
			float dist = distVec.getMag();
			//System.out.println("Distance to impulseAffected: " + distVec.x + ", " + distVec.y + ".  " + dist + "/" + radius*radius);
			shakeScreen(toCamera(pos).invert().scl(strength/10000f).truncate(3f));
			if(dist < radius) {
				float distInv = 1/dist;
				Vector2 force = new Vector2(distVec).nor().scl(distInv*strength);
				force.truncate(30000f);
				//System.out.println("Force is " + force.x + ", " + force.y);
				m.addForce(force);
				m.damage(distInv*damage);
			}
		}
	}
	
	public void addAnimation(Effect a) {
		effects.add(a);
	}
	
	public void addProjectile(Projectile p) {
		projectiles.add(p);
	}
	
	public void shakeScreen(Vector2 amount) {
		camPos.add(-amount.x, -amount.y);
	}
	
	public Vector2 toMouse() {
		return toMouse;
	}
	
	public OrthographicCamera getCam() {
		return cam;
	}
	
	public boolean isOnScreen(float x, float y) {
		return ((Intersect.onRng(x, cam.position.x - wTiles, cam.position.x + wTiles))
				&& Intersect.onRng(y, cam.position.y - hTiles, cam.position.y + hTiles));
	}
	
	public Vector2 getPlayerPos() {
		return player.getPos();
	}
	
	public void setMouseLock(Boolean b) {
		mouseLockUntilRelease = b;
	}

	public Vector2 toPlayer(Vector2 start) {
		return new Vector2(player.pos).sub(start).nor();
	}
	
	public Vector2 toCamera(Vector2 from) {
		return new Vector2(cam.position.x - from.x, cam.position.y - from.y);
	}

	public static Element getOwnerByName(String name, Element root) {
		for(int i = 0; i < root.getChildCount(); i++) {
			Element childToCheck = root.getChild(i);
			String gotten = childToCheck.get("name");
			if(gotten.equals(name)) {
				System.out.println("Owner: " + gotten);
				return childToCheck;
			}
		}
		throw new IllegalArgumentException("String '" + name + "' does not match any owners.");
	}

	public static float getStatByName(String name, Element owner) {
		Array<Element> toCheck = owner.getChildrenByName("stat");
		for(int i = 0; i < toCheck.size; i++) {
			Element childToCheck = toCheck.get(i);
			if (childToCheck.get("statName").toString().equals(name)) {
				Float f = childToCheck.getFloat("value");
				System.out.println("\t" + name + ": " + f);
				return f;
			}
		}
		throw new IllegalArgumentException("String '" + name + "' does not match any stats in owner " + owner.get("name"));
	}
}
