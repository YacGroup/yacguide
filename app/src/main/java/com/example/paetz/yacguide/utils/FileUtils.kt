package com.example.paetz.yacguide.utils

import java.io.*
import java.util.zip.ZipFile

fun unpackZip(path: String, zipName: String) {
    ZipFile(path + zipName).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            zip.getInputStream(entry).use { input ->
                val file = File(path + entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile.mkdirs()
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}
