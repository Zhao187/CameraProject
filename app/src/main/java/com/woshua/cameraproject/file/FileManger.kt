package com.woshua.cameraproject.file

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Steven.zhao
 * email:hongtu.zhao@goodwinsoft.net
 * date:2019/7/28
 * desc:文件管理器
 */
object FileManger {

    private val TAG="FileManger"

    val MEDIA_TYPE_IMAGE=1
    val MEDIA_TYPE_VIDEO=2

    var outputMediaFileUri:Uri?=null
    var outputMediaFileType:String?=null

    @SuppressLint("SimpleDateFormat")
    fun getOutputMediaFile(type:Int):File?
    {
        val mediaStorageDir=File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), TAG
        )

        if(!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                Log.d(TAG, "failed to create directory")
                return null
            }
        }

        val timeStamp=SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        var mediaFile:File?=null

        if(type== MEDIA_TYPE_IMAGE)
        {
            mediaFile= File(mediaStorageDir.path+File.separator+"IMG_" + timeStamp + ".png")
            outputMediaFileType = "image/*"
        }
        else if(type== MEDIA_TYPE_VIDEO)
        {
            mediaFile =File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4")
            outputMediaFileType = "video/*"
        }

        outputMediaFileUri = Uri.fromFile(mediaFile)
        return mediaFile
    }
}