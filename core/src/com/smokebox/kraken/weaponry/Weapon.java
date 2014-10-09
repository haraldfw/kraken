package com.smokebox.kraken.weaponry;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.smokebox.kraken.Game;
import com.smokebox.kraken.character.Wielding;
import com.smokebox.lib.utils.Vector2;
import com.smokebox.lib.utils.geom.Polygon;

public abstract class Weapon {

	public final float wep_kick;
	public final float wep_damage;
	public final String name;
	public final float wep_accuracy;
	public final float wep_aimInfluence;
	public final boolean wep_automatic;
	
	public final float proj_mass;
	public final float proj_range;
	public final float proj_speed;
	public final float proj_radius;
	
	public final float rps_base;
	public final float rps_max;
	public final float rps_min;

	public final boolean burst_active;
	public final int burst_length;
	public final float burst_rpsScale;
	public final float burst_delay;
	
	public Polygon shape;
	
	public Weapon(Element wep) {
		name = wep.getName();
		wep_kick = Game.getStatByName("kick", wep);
		wep_damage = Game.getStatByName("damage", wep);
		wep_accuracy = Game.getStatByName("accuracy", wep);
		wep_aimInfluence = Game.getStatByName("aimInfluence", wep);
		wep_automatic = Game.getStatByName("automatic", wep) == 1;

		proj_mass = Game.getStatByName("mass", wep);
		proj_radius = Game.getStatByName("radius", wep);
		proj_speed = Game.getStatByName("speed", wep);
		proj_range = Game.getStatByName("range", wep);

		rps_base = Game.getStatByName("baseRps", wep);
		rps_max = Game.getStatByName("maxRps", wep);
		rps_min = Game.getStatByName("minRps", wep);

		burst_active = Game.getStatByName("burst", wep) == 1;
		burst_length = (int) Game.getStatByName("burstLength", wep);
		burst_rpsScale = Game.getStatByName("burstRpsScale", wep);
		burst_delay = Game.getStatByName("burstDelay", wep);
		
		System.out.println("Weapon created with stats: " + 
				"\nkick: " + wep_kick + 
				"\ndamage: " + wep_damage + 
				"\naccuracy: " + wep_accuracy + 
				"\naimInfluence: " + wep_aimInfluence);
	}
	
	public abstract void attack(Vector2 pos, Vector2 direction, Wielding wielder);
	
	public abstract void draw(Vector2 pos, float rotation, ShapeRenderer sr);
}
