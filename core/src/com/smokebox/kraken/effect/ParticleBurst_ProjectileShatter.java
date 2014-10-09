/**
 * 
 */
package com.smokebox.kraken.effect;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.smokebox.lib.utils.Vector2;

import java.util.ArrayList;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public class ParticleBurst_ProjectileShatter implements Effect {

	float time = 0f;
	float length = 0.1f;
	
	float velDamping = 0.5f;
	
	private ArrayList<Particle> particles;
	
	public ParticleBurst_ProjectileShatter(Vector2 pos, Vector2 vel, int amount, float radius) {
		particles = new ArrayList<Particle>();
		
		for(int i = 0; i < amount; i++) {
			float r = (radius/2)*(float)Math.random();
			particles.add(
				new Particle(
						new Vector2(pos).add((radius - r)*((float)Math.random()*2 - 1), (radius - r)*((float)Math.random()*2 - 1)), 
						new Vector2(vel).add(new Vector2(-vel.y, vel.x).scl((float) (Math.random()*0.2 - 0.1))), 
						(float) (Math.random()*radius),
						0.8f,
						velDamping
				)
			);
		}
	}
	
	public void update(float delta) {
		for(Particle p : particles) p.integrate(delta);
		time += delta;
	}
	
	public void draw(ShapeRenderer sr) {
		sr.setColor(1, 0.7f, 0, 1);
		for(Particle p : particles) p.draw(sr);
	}
	
	public boolean isFinished() {
		return time > length;
	}

}
