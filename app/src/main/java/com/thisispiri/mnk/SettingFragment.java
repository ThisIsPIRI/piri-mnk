package com.thisispiri.mnk;
import android.os.Bundle;
import android.preference.PreferenceFragment;
/**Shows the preferences for the user to edit.*/
public class SettingFragment extends PreferenceFragment {
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}