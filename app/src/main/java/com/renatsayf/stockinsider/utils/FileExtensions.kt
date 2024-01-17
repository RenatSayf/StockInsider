package com.renatsayf.stockinsider.utils

import android.content.Context
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.OutputStreamWriter

fun Context.createTextFile(fileName: String, content: String) {
    try {
        // Открываем или создаем файл
        val file = File(this.filesDir, fileName)

        // Открываем поток для записи в файл
        val fileOutputStream = FileOutputStream(file)
        val outputStreamWriter = OutputStreamWriter(fileOutputStream)

        // Записываем содержимое файла
        outputStreamWriter.write(content)

        // Закрываем потоки
        outputStreamWriter.close()
        fileOutputStream.close()

        "*********** The $fileName file has been successfully created. ******************".printIfDebug()
    } catch (e: Exception) {
        e.printStackTrace()
        "***************** Error creating the file: ${e.message} ***********".printIfDebug()
    }
}


fun Context.appendTextToFile(fileName: String, content: String) {
    try {
        // Открываем файл для добавления данных
        val file = File(this.filesDir, fileName)

        // Создаем FileWriter с параметром true для добавления данных в конец файла
        val fileWriter = FileWriter(file, true)

        // Создаем BufferedWriter для эффективной записи
        val bufferedWriter = BufferedWriter(fileWriter)

        // Записываем новую строку в конец файла
        bufferedWriter.write(content)
        bufferedWriter.newLine()

        // Закрываем потоки
        bufferedWriter.close()
        fileWriter.close()

        "******************** The entry was successfully added to the $fileName file. ***********".printIfDebug()
    } catch (e: Exception) {
        e.printStackTrace()
        "************* Error when adding an entry to a file: ${e.message} ************".printIfDebug()
    }
}

fun Context.isFileExists(
    fileName: String,
    isExists: () -> Unit = {},
    notExists: () -> Unit = {}
): Boolean {
    val file = File(this.filesDir, fileName)
    val exists = file.exists()
    if (exists) {
        isExists.invoke()
    }
    else {
        notExists.invoke()
    }
    return exists
}

