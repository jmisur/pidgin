package sk.jmisur.pidgin.java;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

import org.apache.commons.configuration.ConfigurationException;

import sk.jmisur.pidgin.core.Log;
import sk.jmisur.pidgin.core.Pidgin;

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
		new LwjglApplication(new Pidgin(new SysOutLog()), "Pidgin", 800, 480, false);
	}

	public static class SysOutLog implements Log {

		@Override
		public void log(String string) {
			System.out.println(string);
		}

	}

}
