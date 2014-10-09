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
import com.smokebox.kraken.weaponry.ammunition.Pellet;
import com.smokebox.lib.utils.Vector2;
import com.smokebox.lib.utils.geom.Polygon;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public class Shotgun extends Weapon {
	
	private Game game;
	
	private final int spray_amount;
	private final float spray_speedScale;
	
	float s = 0.2f;
	float ax = s*2;
	float ay = 0;
	private Polygon shape = new Polygon(new Vector2[]{
			new Vector2(-0.5f*s + ax, -0.6f*s + ay),
			new Vector2(-0.5f*s + ax, 0.6f*s + ay),
			new Vector2(1.7f*s + ax, 0.6f*s + ay),
			new Vector2(1.7f*s + ax, -0.6f*s + ay),
	});
	
	private float muzzleLength = 1.7f*s + ax;
	
	public Sprite getIcon() {
		return null;
	}
	
	public Shotgun(Element xml, Game game) {
		super(xml);

		spray_amount = (int)Game.getStatByName("sprayAmount", xml);
		spray_speedScale = Game.getStatByName("velDamp", xml);
		
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
		for(int i = 0; i < spray_amount; i++)
			game.addProjectile(
					new Pellet(
							at,
							new Vector2(direction).add(
									new Vector2(
										-direction.y, direction.x
									).scl(
										(float) ((1 - wielder.getActiveAccuracy())*(Math.random()*2 - 1)))
									).nor().scl(wielder.getActiveProjectileSpeed()*(0.7f + (float)Math.random()*0.6f)), 
							1/proj_mass, 
							proj_radius,
							wielder.getActiveRange(),
							spray_speedScale,
							wielder.getTeam(),
							game
							)
					);
		game.addAnimation(new ParticleBurst_Shoot(at, direction, 15));
		Vector2 kickVec = new Vector2(direction).nor().scl(wep_kick);
		game.shakeScreen(kickVec);
		//wielder.addForce(kickVec.scl(500).flip());
		game.setMouseLock(!wep_automatic);
	}

}
