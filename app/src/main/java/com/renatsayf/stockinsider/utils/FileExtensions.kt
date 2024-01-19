package com.renatsayf.stockinsider.utils

import android.content.Context
import android.os.Build
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.OutputStreamWriter


const val LOGS_FILE_NAME = "insider-logs.txt"

fun Context.createTextFile(fileName: String, content: String) {
    try {
        // Opening or creating a file
        val file = File(this.filesDir, fileName)

        // Opening the stream to write to a file
        val fileOutputStream = FileOutputStream(file)
        val outputStreamWriter = OutputStreamWriter(fileOutputStream)

        // Writing down the contents of the file
        outputStreamWriter.write(content)

        // Closing the streams
        outputStreamWriter.close()
        fileOutputStream.close()

        "*********** The $fileName file has been successfully created. ******************".printIfDebug()
    } catch (e: Exception) {
        e.printStackTrace()
        "***************** Error creating the file: ${e.message} ***********".printIfDebug()
    }
}


fun Context.appendTextToFile(fileName: String, content: String) {

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
        try {
            // Opening the file to add data
            val file = File(this.filesDir, fileName)

            // Creating a FileWriter with the true parameter to add data to the end of the file
            val fileWriter = FileWriter(file, true)

            // Creating a BufferedWriter for efficient writing
            val bufferedWriter = BufferedWriter(fileWriter)

            // Writing a new line to the end of the file
            bufferedWriter.newLine()
            bufferedWriter.newLine()
            bufferedWriter.write(content)

            // Closing the streams
            bufferedWriter.close()
            fileWriter.close()

            "******************** The entry was successfully added to the $fileName file. ***********".printIfDebug()
        } catch (e: Exception) {
            e.printStackTrace()
            "************* Error when adding an entry to a file: ${e.message} ************".printIfDebug()
        }
    }
}

fun Context.isLogsFileExists(
    fileName: String,
    onExists: (file: File) -> Unit = {},
    onNotExists: () -> Unit = {}
): Boolean {
    val file = File(this.filesDir, fileName)
    val exists = file.exists()
    if (exists) {
        onExists.invoke(file)
    }
    else {
        onNotExists.invoke()
    }
    return exists
}

fun Context.reduceFileSize(fileName: String, maxSizeInBytes: Long) {
    try {
        val file = File(this.filesDir, fileName)

        // We check if the file does not exist or its size does not exceed the maximum size
        if (!file.exists() || file.length() <= maxSizeInBytes) {
            "************** The file does not need to be reduced in size *****************".printIfDebug()
            return
        }

        // Reading all the lines from the file
        val fileReader = FileReader(file)
        val bufferedReader = BufferedReader(fileReader)
        val lines = ArrayList<String>()
        var line = bufferedReader.readLine()
        while (line != null) {
            lines.add(line)
            line = bufferedReader.readLine()
        }
        bufferedReader.close()

        // We determine how many old records need to be deleted
        val linesToRemove = lines.size - (maxSizeInBytes / 1024).toInt()

        // Deleting old records
        if (linesToRemove > 0) {
            lines.subList(0, linesToRemove).clear()
        }

        // Overwriting the file with the updated contents
        val fileWriter = FileWriter(file)
        val bufferedWriter = BufferedWriter(fileWriter)
        for (updatedLine in lines) {
            bufferedWriter.write(updatedLine)
            bufferedWriter.newLine()
        }
        bufferedWriter.close()
        "The size of the $fileName file has been successfully reduced.".printIfDebug()
    } catch (e: Exception) {
        e.printStackTrace()
        "Error when reducing the file size: ${e.message}".printIfDebug()
    }
}


