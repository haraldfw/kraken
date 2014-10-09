/**
 * 
 */
package com.smokebox.kraken.effect;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.smokebox.lib.utils.Vector2;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public class Particle {
	
	private Vector2 pos = new Vector2();
	private Vector2 vel = new Vector2();
	
	private float damping;
	
	private float radius;
	
	private float sizeScl;
	
	private int drawSegments;
	
	/**
	 * @param pos	Starting position
	 * @param vel	Starting velocity
	 * @param shape	The polygon to draw
	 * @param damping	Damping to scale vel by, should be in range <0, 1>
	 */
	public Particle(Vector2 pos, Vector2 vel, float size, float sizeScl, float damping) {
		this.pos.set(pos);
		this.vel.set(vel);
		
		this.radius = size;
		this.sizeScl = sizeScl;
		this.damping = damping;
		
		int r = (int)Math.ceil(radius + 4f);
		drawSegments = r*r;
	}

	public void integrate(float delta) {
		pos.addScaledVector(vel, delta);
		vel.scl(damping*(1 - delta));
		radius *= sizeScl*(1 - delta);
	}
	
	public void draw(ShapeRenderer sr) {
		sr.circle(pos.x, pos.y, radius, drawSegments);
	}
}
