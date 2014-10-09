package com.smokebox.kraken.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.smokebox.kraken.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		int scl = 60;
		config.width = 32*scl;
		config.height = 24*scl;
		new LwjglApplication(new Game(), config);
	}
}
