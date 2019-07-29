package com.woshua.cameraproject.file

import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import android.view.SurfaceHolder
import com.woshua.cameraproject.file.FileManger
import java.io.IOException

/**
 * @author Steven.zhao
 * email:hongtu.zhao@goodwinsoft.net
 * date:2019/7/29
 * desc:媒体文件管理器
 */
object MediaManger {

    private var mMediaRecorder: MediaRecorder? = null

    private var mCamera: Camera? = null

    private var mHolder: SurfaceHolder? = null

    private val TAG = "MediaManger"

    private var outputMediaFileUri: Uri?=null

    /**
     * 开始录像
     * */
    fun startRecording(): Boolean {
        if (prepareVideoRecorder()) {
            mMediaRecorder?.start()
            outputMediaFileUri= FileManger.outputMediaFileUri
            return true
        } else {
            releaseMediaRecorder()
        }
        return false
    }

    /**
     * 停止录像
     * */
    fun stopRecording() {
        Log.e(TAG,"存储路径为:${outputMediaFileUri?.path}")

        mMediaRecorder?.stop()

        releaseMediaRecorder()
    }

    fun isRecording(): Boolean = mMediaRecorder != null

    /**
     * 录像准备
     * */
    private fun prepareVideoRecorder(): Boolean {
        mMediaRecorder = MediaRecorder()

        mCamera?.apply {
            unlock()
            mMediaRecorder!!.setCamera(this)

            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)

            mMediaRecorder!!.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))

//            设置拍摄视频的大小
            mMediaRecorder!!.setVideoSize(640, 480)
            mMediaRecorder!!.setOutputFile(FileManger.getOutputMediaFile(FileManger.MEDIA_TYPE_VIDEO).toString())

            if (mHolder != null) {
                mMediaRecorder!!.setPreviewDisplay(mHolder!!.surface)
            } else {
                Log.e(TAG, "Holder can not null")
            }

            try {
                mMediaRecorder!!.prepare()
            } catch (e: IllegalStateException) {
                Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.message)
                releaseMediaRecorder()
                return false
            } catch (e: IOException) {
                Log.d(TAG, "IOException preparing MediaRecorder: " + e.message)
                releaseMediaRecorder()
                return false
            }
            return true
        }.run {
            Log.e(TAG, "Camera can not null")
            return false
        }
    }

    private fun releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder!!.reset()
            mMediaRecorder!!.release()
            mMediaRecorder = null
        }
    }

    fun setCamera(camera: Camera) {
        this.mCamera = camera
    }

    fun setHolder(holder: SurfaceHolder) {
        this.mHolder = holder
    }
}