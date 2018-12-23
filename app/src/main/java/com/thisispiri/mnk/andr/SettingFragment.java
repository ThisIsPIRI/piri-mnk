package com.thisispiri.mnk.andr;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.thisispiri.mnk.R;
import com.thisispiri.util.AndroidUtilsKt;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**Shows the preferences for the user to edit.*/
public class SettingFragment extends PreferenceFragment {
	private static final List<List<Integer>> rulePresets = asList(emptyList(), asList(3, 3, 3), asList(15, 15, 5), asList(19, 19, 5));
	private static final String[] ruleKeys = {"horSize", "verSize", "winStreak"};
	private static final List<List<?>> graphicPresets = asList(emptyList(), asList("0", "0", 0xFFFFFFFF, 0xFF0000FF, 0xFFFF0000),
			asList("1", "1", 0xFFB69B4C, 0xFF000000, 0xFFFFFFFF));
	private static final String[] graphicKeys = {"symbols", "lineType", "backgroundColor", "xColor", "oColor"};
	@Override public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		//TODO: Avoid registering listeners every time SettingActivity is entered?
		//TODO: Support user-defined presets?
		findPreference("rulesPreset").setOnPreferenceChangeListener(presetSetter(ruleKeys, rulePresets, sharedPref));
		//TODO: Make the changes show up immediately without reentering SettingActivity(seems to be a problem with ListPreference)
		findPreference("graphicsPreset").setOnPreferenceChangeListener(presetSetter(graphicKeys, graphicPresets, sharedPref));
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
	private Preference.OnPreferenceChangeListener presetSetter(final String[] keys, final List<? extends List> presets, final SharedPreferences sharedPref) {
		return (Preference pref, Object newVal) -> {
			int value = Integer.parseInt((String) newVal);
			if(value != 0) {
				final SharedPreferences.Editor edit = sharedPref.edit();
				for(int i = 0;i < presets.get(value).size();i++) {
					if(presets.get(value).get(i) instanceof Integer)
						edit.putInt(keys[i], (Integer) presets.get(value).get(i));
					else
						edit.putString(keys[i], (String) presets.get(value).get(i));
				}
				edit.apply();
			}
			return true;
		};
	}
}