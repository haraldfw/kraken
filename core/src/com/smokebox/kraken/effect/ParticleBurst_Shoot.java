/**
 * 
 */
package com.smokebox.kraken.effect;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.smokebox.lib.utils.Vector2;

import java.util.ArrayList;

/**
 * @author Harald Floor Wilhelmsen
 *d
 */
public class ParticleBurst_Shoot implements Effect {

	float time = 0f;
	float length = 0.2f;
	
	float particleDamping = 0.9f;
	
	private ArrayList<Particle> particles;
	
	public ParticleBurst_Shoot(Vector2 pos, Vector2 dir, int amount) {
		particles = new ArrayList<Particle>();
		
		for(int i = 0; i < amount; i++) {
			particles.add(
				new Particle(
						pos, 
						new Vector2(dir).add(new Vector2(-dir.y, dir.x).scl((float) Math.random()*2 - 1)).nor().scl((float)Math.random()*10), 
						(float) (Math.random()*0.3f),
						0.8f,
						particleDamping
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
