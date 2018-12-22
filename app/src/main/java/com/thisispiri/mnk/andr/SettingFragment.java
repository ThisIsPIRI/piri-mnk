package com.thisispiri.mnk.andr;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.thisispiri.mnk.R;
import com.thisispiri.util.AndroidUtilsKt;

import java.util.HashSet;
import java.util.Set;

/**Shows the preferences for the user to edit.*/
public class SettingFragment extends PreferenceFragment {
	private static final int[][] rulePresets = {{}, {3, 3, 3}, {15, 15, 5}, {19, 19, 5}};
	private static final String[] ruleKeys = {"horSize", "verSize", "winStreak"};
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		//TODO: Avoid registering listeners every time SettingActivity is entered?
		//TODO: Make the changes show up immediately without reentering SettingActivity
		findPreference("rulesPreset").setOnPreferenceChangeListener((Preference pref, Object newVal) -> {
			int value = Integer.parseInt((String) newVal);
			if(value != 0) {
				final SharedPreferences.Editor edit = sharedPref.edit();
				for(int i = 0;i < rulePresets[value].length;i++)
					edit.putInt(ruleKeys[i], rulePresets[value][i]);
				edit.apply();
			}
			return true;
		});
		findPreference("graphicsPreset").setOnPreferenceChangeListener((Preference pref, Object newVal) -> {
			final SharedPreferences.Editor edit = sharedPref.edit();
			//TODO: Simplify, support user-defined presets?
			switch(Integer.parseInt((String) newVal)) {
			case 1:
			edit.putString("symbols", "0");
			edit.putString("lineType", "0");
			edit.putInt("backgroundColor", 0xFFFFFFFF);
			edit.putInt("xColor", 0xFF0000FF);
			edit.putInt("oColor", 0xFFFF0000);
			break;
			case 2:
			edit.putString("symbols", "1");
			edit.putString("lineType", "1");
			edit.putInt("backgroundColor", 0xFFB69B4C);
			edit.putInt("xColor", 0xFF000000);
			edit.putInt("oColor", 0xFFFFFFFF);
			break;
			}
			edit.apply();
			return true;
		});
		findPreference("aiType").setOnPreferenceChangeListener((Preference pref, Object newVal) -> {
			if(newVal.equals("2") && sharedPref.getInt("winStreak", 5) > 8) {
				AndroidUtilsKt.showToast(getActivity(), R.string.emacsLengthWarning, Toast.LENGTH_LONG);
				return false;
			}
			return true;
		});
		findPreference("winStreak").setOnPreferenceChangeListener((Preference pref, Object newVal) -> {
			if(((Integer) newVal) > 8 && sharedPref.getString("aiType", "1").equals("2")) {
				AndroidUtilsKt.showToast(getActivity(), R.string.emacsLengthWarning, Toast.LENGTH_LONG);
				return false;
			}
			return true;
		});
	}
}