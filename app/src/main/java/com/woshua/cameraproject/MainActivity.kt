package com.woshua.cameraproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.Camera
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.woshua.cameraproject.file.FileManger
import com.woshua.cameraproject.file.MediaManger
import com.woshua.cameraproject.utils.FileUtils
import com.woshua.cameraproject.view.TakePictureCameraView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), TakePictureCameraView.TakePictureCallBack {
    override fun onPictureTaken(img: Bitmap?) {
        img?.let { bitmap ->
            layout_cardresult.visibility = View.VISIBLE
            img_picture.setImageBitmap(bitmap)
            bitmapResult = bitmap

            iv_picture_show.setImageBitmap(bitmap)
        }
    }

    private var bitmapResult: Bitmap? = null

    private val rxPermissions: RxPermissions by lazy { RxPermissions(this) }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rxPermissions.request(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
            .subscribe {}

        camera.takePictureCallBack = this

        view_takepicture.setOnClickListener {
            camera.takePicture()
        }

        img_cancle.setOnClickListener {
            onBackPressed()
        }

        img_ok.setOnClickListener {
            bitmapResult?.let { bitmap ->
                val path = FileUtils.saveBitmap(this@MainActivity, bitmap)

                if (path.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, "图片保存成功---$path", Toast.LENGTH_SHORT).show()
                }
            }

            onBackPressed()
        }

        //使用录像 必须配置的参数
        camera.cameraCallBack = object : TakePictureCameraView.CameraCallBack {
            override fun onCamera(camera: Camera) {
                MediaManger.setCamera(camera)
                MediaManger.setHolder(this@MainActivity.camera.mHolder)
            }
        }
        view_takeAudio.setOnClickListener {
            if (MediaManger.isRecording()) {
                MediaManger.stopRecording()

                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, FileManger.outputMediaFileUri))
                val thumbnail = ThumbnailUtils.createVideoThumbnail(
                    FileManger.outputMediaFileUri?.path,
                    MediaStore.Video.Thumbnails.MINI_KIND
                )
                iv_picture_show.setImageBitmap(thumbnail)

                tv_status_title.text = "录像"
            } else {
                if (MediaManger.startRecording()) {
                    tv_status_title.text = "停止"
                }
            }
        }

        iv_picture_show.setOnClickListener {
            FileManger.outputMediaFileUri?.let { uri ->
                val intent = Intent(this, ShowPhotoVideoActivity::class.java)
                intent.setDataAndType(uri, FileManger.outputMediaFileType)
                startActivityForResult(intent, 0)
            }
        }
    }

    override fun onBackPressed() {
        if (!camera.isPreviewing) {
            layout_cardresult.visibility = View.GONE
            camera.startCameraPreview()
        } else {
            super.onBackPressed()
        }
    }
}
