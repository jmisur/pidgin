package sk.jmisur.pidgin.html;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.google.gwt.core.shared.GWT;

import sk.jmisur.pidgin.core.Log;
import sk.jmisur.pidgin.core.Pidgin;
import sk.jmisur.pidgin.core.PidginConfig;

public class PidginHtml extends GwtApplication {

	@Override
	public ApplicationListener getApplicationListener() {
		return new Pidgin(new GwtLog(), new GwtConfig());
	}

	public static class GwtLog implements Log {

		@Override
		public void log(String string) {
			GWT.log(string);
		}
	}

	public static class GwtConfig implements PidginConfig {}

	@Override
	public GwtApplicationConfiguration getConfig() {
		return new GwtApplicationConfiguration(480, 320);
	}
}
