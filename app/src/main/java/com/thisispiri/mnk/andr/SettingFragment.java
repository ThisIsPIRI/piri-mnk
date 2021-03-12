package com.thisispiri.mnk.andr;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.thisispiri.mnk.R;
import com.thisispiri.common.andr.AndrUtil;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**Shows the preferences for the user to edit.*/
public class SettingFragment extends PreferenceFragment {
	private static final List<List<?>> rulePresets = asList(emptyList(), asList(3, 3, 3, false), asList(15, 15, 5, false),
			asList(19, 19, 5, false), asList(7, 6, 4, true));
	private static final String[] ruleKeys = {"horSize", "verSize", "winStreak", "enableGravity"};
	private static final List<List<?>> graphicPresets = asList(emptyList(),
			asList("0", "1", "0", 0xFF000000, 0xFFFFFFFF, 0xFF0000FF, 0xFFFF0000),
			asList("2", "2", "1", 0xFF000000, 0xFFB69B4C, 0xFF000000, 0xFFFFFFFF),
			asList("2", "2", "2", 0xFFEEEEFF, 0xFF0000EE, 0xFFEEEE00, 0xFFEE0000));
	private static final String[] graphicKeys = {"firstSymbols", "secondSymbols", "lineType", "lineColor", "backgroundColor", "xColor", "oColor"};
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
			if(newVal.equals("3") && sharedPref.getInt("winStreak", 5) > 8) {
				AndrUtil.showToast(getActivity(), R.string.emacsLengthWarning, Toast.LENGTH_LONG);
				return false;
			}
			return true;
		});
		findPreference("winStreak").setOnPreferenceChangeListener((Preference pref, Object newVal) -> {
			if(((Integer) newVal) > 8 && sharedPref.getString("aiType", "2").equals("3")) {
				AndrUtil.showToast(getActivity(), R.string.emacsLengthWarning, Toast.LENGTH_LONG);
				return false;
			}
			return true;
		});
	}
	private Preference.OnPreferenceChangeListener presetSetter(final String[] keys, final List<? extends List<?>> presets, final SharedPreferences sharedPref) {
		return (Preference pref, Object newVal) -> {
			int value = Integer.parseInt((String) newVal);
			if(value != 0) {
				final SharedPreferences.Editor edit = sharedPref.edit();
				for(int i = 0;i < presets.get(value).size();i++) {
					if(presets.get(value).get(i) instanceof Integer)
						edit.putInt(keys[i], (Integer) presets.get(value).get(i));
					else if(presets.get(value).get(i) instanceof Boolean)
						edit.putBoolean(keys[i], (Boolean) presets.get(value).get(i));
					else
						edit.putString(keys[i], (String) presets.get(value).get(i));
				}
				edit.apply();
			}
			return true;
		};
	}
}