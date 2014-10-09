/**
 * 
 */
package com.smokebox.kraken.effect;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public interface Effect {
	
	public void update(float delta);
	
	public void draw(ShapeRenderer sr);
	
	public boolean isFinished();
}