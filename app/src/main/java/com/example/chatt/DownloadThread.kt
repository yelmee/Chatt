package com.example.chatt

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

// 다운로드 쓰레드로 돌림..
class DownloadThread(var ServerUrl: String, var LocalPath: String) : Thread() {

    override fun run() {
        val imgurl: URL
        var Read: Int
        try {
            imgurl = URL(ServerUrl)
            val conn = imgurl
                .openConnection() as HttpURLConnection
            val len = conn.contentLength
            val tmpByte = ByteArray(len)
            val `is` = conn.inputStream
            //                Bitmap is = BitmapFactory.decodeStream(item_swipe);
            val file = File(LocalPath)
            val fos = FileOutputStream(file)
            while (true) {
                Read = `is`.read(tmpByte)
                if (Read <= 0) {
                    break
                }
                fos.write(tmpByte, 0, Read)
            }
            `is`.close()
            fos.close()
            conn.disconnect()

        } catch (e: MalformedURLException) {
            Log.e("ERROR1", e.message)
        } catch (e: IOException) {
            Log.e("ERROR2", e.message)
            e.printStackTrace()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
