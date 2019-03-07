package com.thisispiri.util

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import androidx.annotation.StringRes
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

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

/**Shows a short `Toast` `saying` something. Safe to call from any thread.
 * @param inActivity the `Activity` to display the `Toast` in.
 * @param saying The `String` to display.
 * @param length The length of the `Toast`. Must either be `Toast.LENGTH_SHORT` or `Toast.LENGTH_LONG`. Defaults to `LENGTH_SHORT`.*/
@JvmOverloads fun showToast(inActivity: Activity, saying: String, length: Int = Toast.LENGTH_SHORT) {
	if (Looper.myLooper() != Looper.getMainLooper())
		inActivity.runOnUiThread {showToast(inActivity, saying)}
	else
		Toast.makeText(inActivity, saying, length).show()
}

/**@see showToast(Activity, String, Int)
 * @param saying The resource ID of the string to show.*/
@JvmOverloads fun showToast(inActivity: Activity, @StringRes saying: Int, length: Int = Toast.LENGTH_SHORT) {
	showToast(inActivity, inActivity.getString(saying), length)
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

/**Puts `these` in a `Bundle` and returns it.
 * @param these A vararg. The `String`s in even indices are the keys and the ones in odd indices the values.
 * In other words, the `Bundle` will contain (these[0], these[1]), (these[2], these[3]), ... (these[size - 2] these[size - 1]).
 * The number of arguments should be even; if it is odd, the last one will be ignored.
 * @return The `Bundle`. An empty one if less than 2 arguments are supplied.*/
fun bundleWith(vararg these: String): Bundle {
	val result = Bundle()
	for(i in 1 until these.size step 2) {
		result.putString(these[i - 1], these[i])
	}
	return result
}

@Throws(IOException::class)
fun assetToMappedByteBuffer(context: Context, filename: String): MappedByteBuffer {
	val desc = context.assets.openFd(filename)
	return FileInputStream(desc.fileDescriptor).channel.map(FileChannel.MapMode.READ_ONLY, desc.startOffset, desc.declaredLength)
}
