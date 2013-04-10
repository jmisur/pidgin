package sk.jmisur.pidgin.java;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

import sk.jmisur.pidgin.core.Log;
import sk.jmisur.pidgin.core.Pidgin;
import sk.jmisur.pidgin.core.PidginConfig;

/**
 * Copyright 2011 David Kirchner dpk@dpk.net Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License. Source: http://dpk.net/2011/04/30/libgdx-box2d-tiled-maps-full-working-example-part-1/
 */

public class PidginDesktop {

	public static void main(String[] argv) throws ConfigurationException {
		HierarchicalINIConfiguration config = new HierarchicalINIConfiguration("settings.ini");
		new LwjglApplication(new Pidgin(new SysOutLog(), new IniConfig(config)), "JumperTutorial", config.getInt("main.width"), config.getInt("main.height"),
				false);
	}

	public static class SysOutLog implements Log {

		@Override
		public void log(String string) {
			System.out.println(string);
		}

	}

	public static class IniConfig implements PidginConfig {

		private final HierarchicalINIConfiguration config;

		public IniConfig(HierarchicalINIConfiguration config) {
			this.config = config;
		}

		@Override
		public float getJumpVelocity() {
			return config.getFloat("pidgin.jump");
		}

		@Override
		public float getGravity() {
			return config.getFloat("main.gravity");
		}

		@Override
		public float getSpeed() {
			return config.getFloat("pidgin.maxspeed");
		}

		@Override
		public float getMass() {
			return config.getFloat("pidgin.mass");
		}

		@Override
		public int getPidginHeight() {
			return config.getInt("pidgin.height");
		}

		@Override
		public int getPidginWidth() {
			return config.getInt("pidgin.width");
		}

		@Override
		public String getMap() {
			return config.getString("main.map");
		}

	}
}
