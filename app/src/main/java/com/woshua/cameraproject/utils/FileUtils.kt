package com.woshua.cameraproject.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.woshua.cameraproject.file.FileManger
import java.io.FileOutputStream
import java.lang.Exception
import android.content.Intent


/**
 * @author Steven.zhao
 * email:hongtu.zhao@goodwinsoft.net
 * date:2019/7/28
 * desc:文件操作工具类
 */
object FileUtils {

    val TAG = "FileUtils"

    fun saveBitmap(context:Context,bitmap: Bitmap):String {
        val outputMediaFile = FileManger.getOutputMediaFile(FileManger.MEDIA_TYPE_IMAGE)
        if (outputMediaFile == null) {
            Log.e(TAG, "Error creating media file, check storage permissions")
            return ""
        }

        try {
            val fos = FileOutputStream(outputMediaFile)

            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)

            fos.flush()
            fos.close()

            //保存图片后发送广播通知更新数据库
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, FileManger.outputMediaFileUri))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return outputMediaFile.path
    }
}