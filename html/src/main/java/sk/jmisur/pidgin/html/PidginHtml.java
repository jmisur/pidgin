package sk.jmisur.pidgin.html;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.google.gwt.core.shared.GWT;

import sk.jmisur.pidgin.core.Log;
import sk.jmisur.pidgin.core.Pidgin;

public class PidginHtml extends GwtApplication {

	@Override
	public ApplicationListener getApplicationListener() {
		return new Pidgin(new GwtLog());
	}

	public static class GwtLog implements Log {

		@Override
		public void log(String string) {
			GWT.log(string);
		}
	}

	@Override
	public GwtApplicationConfiguration getConfig() {
		return new GwtApplicationConfiguration(480, 320);
	}
}
