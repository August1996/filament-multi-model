package com.demo.multimodel

import android.content.res.AssetManager
import java.io.FileInputStream
import java.io.InputStream

object IOUtils {

    fun readAsset(asset: AssetManager, path: String): ByteArray {
        val inputStream = asset.open(path)
        val result = read(inputStream)
        inputStream.close()
        return result
    }

    fun readFile(path: String): ByteArray {
        val inputStream = FileInputStream(path)
        val result = read(inputStream)
        inputStream.close()
        return result
    }

    fun read(inputStream: InputStream): ByteArray = inputStream.readBytes()


}