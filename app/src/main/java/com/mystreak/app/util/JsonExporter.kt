package com.mystreak.app.util

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mystreak.app.data.repository.ExportData
import java.io.OutputStreamWriter

object JsonExporter {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun export(context: Context, uri: Uri, data: ExportData): Result<Unit> = runCatching {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            OutputStreamWriter(stream).use { writer ->
                writer.write(gson.toJson(data))
            }
        } ?: error("Could not open output stream")
    }
}
