package com.thisispiri.util

import android.os.Environment
import java.io.File
import java.io.IOException

//TODO: separate into a library

@Throws(IOException::class)
public fun getFile(directoryName: String, fileName: String, allowCreation: Boolean): File {
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
