/**
 * 
 */
package com.smokebox.kraken.screen;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.smokebox.kraken.Game;
import com.smokebox.kraken.character.Player;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public class HUD {
	
	Player p;
	Game game;
	
	int h;
	int w;
	
	public HUD(Player p, Game game) {
		this.p = p;
		this.game = game;
		h = game.hTiles;
		w = game.wTiles;
	}
	
	public void update(float delta) {
		
	}

	public void draw(ShapeRenderer sr) {
		// draw stuff
		//sr.polygon(p.getActiveWeapon().getIcon().getVerticesAsFloatArray(game.getCam().position.x - w/2 + 1, game.getCam().position.y + h/2 - 1));
	}
}
