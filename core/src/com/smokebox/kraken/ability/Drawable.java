/**
 * 
 */
package com.smokebox.kraken.ability;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public interface Drawable {
	
	public void draw(ShapeRenderer sr);
	
	public boolean isAlive();
}
