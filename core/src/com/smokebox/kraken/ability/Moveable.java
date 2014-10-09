/**
 * 
 */
package com.smokebox.kraken.ability;

import com.smokebox.lib.utils.Vector2;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public interface Moveable {

	public void integrate(float delta);
	
	public void addForce(Vector2 force);
	
	public Vector2 getPos();
	public Vector2 getVel();
	
	public boolean isAlive();
}
