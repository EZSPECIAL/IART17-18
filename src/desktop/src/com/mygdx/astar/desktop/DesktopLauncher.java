package com.mygdx.astar.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.astar.AStar;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.title = "Pukoban";
		config.useGL30 = false;
		config.width = 800;
		config.height = 600;
		config.resizable = false;

		new LwjglApplication(new AStar(), config);
	}
}
