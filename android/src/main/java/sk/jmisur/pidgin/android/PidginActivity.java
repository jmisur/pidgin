package sk.jmisur.pidgin.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import sk.jmisur.pidgin.core.Log;
import sk.jmisur.pidgin.core.Pidgin;

public class PidginActivity extends AndroidApplication {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGL20 = true;
		initialize(new Pidgin(new AndroidLog()), config);
	}

	public static class AndroidLog implements Log {

		@Override
		public void log(String string) {
			android.util.Log.i("Pidgin", string);
		}

	}
}
