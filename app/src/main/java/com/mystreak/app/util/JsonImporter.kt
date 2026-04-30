package com.mystreak.app.util

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.mystreak.app.data.repository.ExportData
import java.io.InputStreamReader

object JsonImporter {
    private val gson = Gson()

    fun import(context: Context, uri: Uri): Result<ExportData> = runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            InputStreamReader(stream).use { reader ->
                gson.fromJson(reader, ExportData::class.java)
                    ?: error("Empty or invalid file")
            }
        } ?: error("Could not open input stream")
    }
}
