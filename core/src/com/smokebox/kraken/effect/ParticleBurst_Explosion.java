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
public class ParticleBurst_Explosion implements Effect {
	
	private ArrayList<Particle> particles;
	
	private float time = 0;
	private int spawned = 0;
	private int	toSpawn; 
	private float timeBetweenParticles;
	
	private float length = 0.2f; // seconds
	
	private Vector2 pos = new Vector2();
	
	private float radius;
	
	public ParticleBurst_Explosion(Vector2 pos, float radius, int amount) {
		this.pos.set(pos);
		this.radius = radius;
		particles = new ArrayList<Particle>();
		
		toSpawn = amount;
		timeBetweenParticles = (float)length/amount;
		
		particles.add(new Particle(pos, new Vector2(), radius, 0.8f, 1));
	}

	public void update(float delta) {
		for(Particle p : particles) p.integrate(delta);
		time += delta;
		if(spawned < toSpawn && time > spawned*timeBetweenParticles) {
			float particleRadius = radius*(float)Math.random()/3;
			Vector2 newPos = new Vector2(pos).add(new Vector2().setRandom(-1, 1, -1, 1).nor().scl((radius - particleRadius)*Math.random()));
			particles.add(new Particle(newPos, new Vector2(), particleRadius, 0.5f, 1));
			spawned++;
		}
	}
	
	public void draw(ShapeRenderer sr) {
		sr.setColor(1, 0.7f, 0, 1);
		for(Particle p : particles) p.draw(sr);
	}
	
	public boolean isFinished() {
		return time > length;
	}

}
