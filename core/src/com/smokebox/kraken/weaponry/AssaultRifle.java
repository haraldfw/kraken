/**
 * 
 */
package com.smokebox.kraken.weaponry;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.smokebox.kraken.Game;
import com.smokebox.kraken.character.Wielding;
import com.smokebox.kraken.effect.ParticleBurst_Shoot;
import com.smokebox.kraken.weaponry.ammunition.Bullet;
import com.smokebox.lib.utils.Vector2;
import com.smokebox.lib.utils.geom.Polygon;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public class AssaultRifle extends Weapon {
	
	Game game;
	
	// drawScale
	float s = 0.2f;
	// shift along x-axis
	float ax = s*3;
	// Shift along y-axis
	float ay = 0;
	private Polygon shape = new Polygon(new Vector2[]{
			new Vector2(-0.5f*s + ax, -0.5f*s + ay),
			new Vector2(-0.5f*s + ax, 0.5f*s + ay),
			new Vector2(2*s + ax, 0.5f*s + ay),
			new Vector2(2*s + ax, -0.5f*s + ay),
	});
	
	private float muzzleLength = 2*s + ax;
	
	public Sprite getIcon() {
		return null;
	}
	
	public AssaultRifle(Element xmlWep, Game game) {
		super(xmlWep);
		
		this.game = game;
		shape.origin.set(0, 0);
	}
	
	public void draw(Vector2 pos, float rotation, ShapeRenderer sr) {
		sr.setColor(0.7f, 0.7f, 0.7f, 1);
		shape.setRotation(rotation);
		sr.polygon(shape.getVerticesAsFloatArray(pos.x, pos.y));
	}
	
	public void attack(Vector2 pos, Vector2 direction, Wielding wielder) {
		Vector2 at = new Vector2(pos).addScaledVector(direction, muzzleLength);
		game.addProjectile(
				new Bullet(
						at,
						new Vector2(direction).add(
								new Vector2(
									-direction.y, direction.x
								).scl(
									(float) ((1 - wielder.getActiveAccuracy())*(Math.random()*2 - 1)))
								).nor().scl(wielder.getActiveProjectileSpeed()), 
						1/proj_mass, 
						proj_radius,
						wielder.getActiveRange(),
						wielder.getTeam(),
						game
						)
				);
		game.addAnimation(new ParticleBurst_Shoot(at, direction, 2));
		Vector2 kickVec = new Vector2(direction).nor().scl(wep_kick);
		game.shakeScreen(kickVec);
		wielder.addForce(kickVec.scl(500).flip());
		game.setMouseLock(!wep_automatic);
	}
}
