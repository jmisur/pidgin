package sk.jmisur.pidgin.android;

import android.os.Bundle;
import android.util.DisplayMetrics;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import sk.jmisur.pidgin.core.Log;
import sk.jmisur.pidgin.core.Pidgin;
import sk.jmisur.pidgin.core.PidginConfig;

public class PidginActivity extends AndroidApplication {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGL20 = false;

		DisplayMetrics displayMetrics = new DisplayMetrics();
		displayMetrics = getResources().getDisplayMetrics();

		initialize(new Pidgin(displayMetrics.heightPixels, displayMetrics.widthPixels, new AndroidLog(), new AndroidConfig()), config);
	}

	public static class AndroidLog implements Log {

		@Override
		public void log(String string) {
			android.util.Log.i("Pidgin", string);
		}
	}

	public static class AndroidConfig implements PidginConfig {

		@Override
		public float getJumpVelocity() {
			return 15;
		}

		@Override
		public float getGravity() {
			return 30;
		}

		@Override
		public float getSpeed() {
			return 5;
		}

		@Override
		public float getMass() {
			return 0.1f;
		}

		@Override
		public int getPidginHeight() {
			return 200;
		}

		@Override
		public int getPidginWidth() {
			return 150;
		}

		@Override
		public String getMap() {
			return "ulica";
		}
	}
}
