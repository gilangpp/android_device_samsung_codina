/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.settings.device;

import com.cyanogenmod.settings.device.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import java.lang.Runtime;
import java.io.IOException;

public class GeneralFragmentActivity extends PreferenceFragment {

	private static final String TAG = "GalaxyAce2_Settings_General";

	private static final String FILE_ACCELEROMETER_CALIB = "/sys/class/sensors/accelerometer_sensor/calibration";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.general_preferences);

		// PreferenceScreen prefSet = getPreferenceScreen();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		String boxValue;
		String key = preference.getKey();

		Log.w(TAG, "key: " + key);

		if (key.compareTo(DeviceSettings.KEY_USE_ACCELEROMETER_CALIBRATION) == 0) {
			boxValue = (((CheckBoxPreference) preference).isChecked() ? "1"
					: "0");
			Utils.writeValue(FILE_ACCELEROMETER_CALIB, boxValue);
		} else if (key.compareTo(DeviceSettings.KEY_CALIBRATE_ACCELEROMETER) == 0) {
			// when calibration data utilization is disabled and enabled back,
			// calibration is done at the same time by driver
			Utils.writeValue(FILE_ACCELEROMETER_CALIB, "0");
			Utils.writeValue(FILE_ACCELEROMETER_CALIB, "1");
			Utils.showDialog((Context) getActivity(),
					getString(R.string.accelerometer_dialog_head),
					getString(R.string.accelerometer_dialog_message));
		}
		
		if (key.equals(DeviceSettings.KEY_SWITCH_STORAGE)) {
			boolean b = ((CheckBoxPreference) preference).isChecked();
			String cmd = "SwapStorages.sh " + (b?"1":"0");

			try {
			    Process proc = Runtime.getRuntime().exec(new String[]{"su","-c",cmd});
			    proc.waitFor();
			} catch (IOException e) {
			    e.printStackTrace();
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		}

		return true;
	}

	public static boolean isSupported(String FILE) {
		return Utils.fileExists(FILE);
	}

	public static void restore(Context context) {

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean accelerometerCalib = sharedPrefs.getBoolean(
				DeviceSettings.KEY_USE_ACCELEROMETER_CALIBRATION, true);

		int sstor = SystemProperties.getInt("persist.sys.vold.switchexternal", 0) ;
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean(DeviceSettings.KEY_SWITCH_STORAGE,sstor==1?true:false);
		editor.commit();

		// When use accelerometer calibration value is set to 1, calibration is
		// done at the same time, which
		// means it is reset at each boot, providing wrong calibration most of
		// the time at each reboot.
		// So we only set it to "0" if user wants it, as it defaults to 1 at
		// boot
		if (!accelerometerCalib)
			Utils.writeValue(FILE_ACCELEROMETER_CALIB, "0");
	}

}
