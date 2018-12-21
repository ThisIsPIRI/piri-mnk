package com.thisispiri.mnk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceFragment;

import java.util.HashSet;
import java.util.Set;

/**Shows the preferences for the user to edit.*/
public class SettingFragment extends PreferenceFragment {
	private static final Set<String> presetTargets = new HashSet<>();
	static {
		presetTargets.add("symbols");
		presetTargets.add("lineType");
		presetTargets.add("backgroundColor");
		presetTargets.add("xColor");
		presetTargets.add("oColor");
	}
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		//TODO: Avoid registering listeners every time SettingActivity is entered?
		//TODO: Make the changes show up immediately without reentering SettingActivity
		findPreference("graphicsPreset").setOnPreferenceChangeListener((Preference pref, Object newVal) -> {
			final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
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
		Preference.OnPreferenceChangeListener listener = (Preference pref, Object newVal) -> {
			PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putString("graphicsPreset", "0").apply();
			return true;
		};
		PreferenceGroup graphics = ((PreferenceGroup) getPreferenceScreen().findPreference("graphicsCategory"));
		for(int i = 0; i < graphics.getPreferenceCount(); i++) {
			Preference p = graphics.getPreference(i);
			if(presetTargets.contains(p.getKey()))
				p.setOnPreferenceChangeListener(listener);
		}
	}
}