package com.thisispiri.mnk.andr;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/**Houses {@link SettingFragment}.*/
public class SettingActivity extends AppCompatActivity {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
	}
}