package com.thisispiri.util

import android.app.Activity
import android.os.Environment
import android.os.Looper
import android.support.annotation.StringRes
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import java.io.File
import java.io.IOException

//TODO: separate into a library

@Throws(IOException::class)
fun getFile(directoryName: String, fileName: String, allowCreation: Boolean): File {
	if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
		throw IOException()
	}
	val directory = Environment.getExternalStoragePublicDirectory(directoryName)
	val file = File(directory, fileName)
	if (!directory.exists() && !directory.mkdir()) {
		throw IOException()
	}
	if (!file.exists()) {
		if (!allowCreation || !file.createNewFile()) throw IOException()
	}
	return file
}

/**Shows a short `Toast` `saying` something.
 * @param inActivity the `Activity` to display the `Toast` in.
 * @param saying The `String` to display.
 * @param length The length of the `Toast`. Must either be `Toast.LENGTH_SHORT` or `Toast.LENGTH_LONG`. Defaults to `LENGTH_SHORT`.*/
fun showToast(inActivity: Activity, saying: String) {
	if (Looper.myLooper() != Looper.getMainLooper())
		inActivity.runOnUiThread {showToast(inActivity, saying)}
	else
		Toast.makeText(inActivity, saying, Toast.LENGTH_SHORT).show()
}

/**@see showToast(Activity, String, Int)
 * @param saying The resource ID of the string to show.*/
fun showToast(inActivity: Activity, @StringRes saying: Int) { //TODO: add jvmoverloads and test
	showToast(inActivity, inActivity.getString(saying))
}
/**Changes the status of the `RadioButton` without alerting its `group`'s `OnCheckedChangeListener`.
 * Note that this function doesn't touch any listener attached to the `button` itself.
 * @param group The `RadioGroup` `button` is in.
 * @param button The `RadioButton` to click.
 * @param listener The `OnCheckedChangeListener` to attach to the `group` after clicking.
 * @param to If `true`, `button` will be checked. If `false`, the opposite.*/
fun hiddenClick(group: RadioGroup, button: RadioButton, listener: RadioGroup.OnCheckedChangeListener, to: Boolean) {
	group.setOnCheckedChangeListener(null)
	button.isChecked = to
	group.setOnCheckedChangeListener(listener)
}