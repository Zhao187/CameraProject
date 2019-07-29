package com.woshua.cameraproject.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.Surface.*

/**
 * @author Steven.zhao
 * email:hongtu.zhao@goodwinsoft.net
 * date:2019/7/28
 * desc:
 */
class TakePictureCameraView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private val TAG = "TakePictureCameraView"

    override fun surfaceCreated(holder: SurfaceHolder?) {
        try {
            releaseCamera()
            openCamera()
            initCamera()
        } catch (e: Exception) {
            mCamera = null
        }

    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        stopCameraPreview()
        initCamera()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        releaseCamera()
    }

    private var screenHeight: Int
    private var screenWidth: Int

    private var mCamera: Camera? = null

    var isPreviewing: Boolean = false

    private val isSupportAutoFocus: Boolean

    var takePictureCallBack: TakePictureCallBack? = null

    var cameraCallBack: CameraCallBack? = null

    private var oldDist: Float = 1f

    private var mRotation:Int=0

    val mHolder: SurfaceHolder

    init {
        val dm = context.resources.displayMetrics
        screenWidth = dm.widthPixels
        screenHeight = dm.heightPixels

        isSupportAutoFocus = context.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)
        mHolder = holder

        holder.addCallback(this)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount == 1) {
            handFocus(event)
        } else {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> oldDist = getFingerSpacing(event)
                MotionEvent.ACTION_MOVE -> {
                    val newDist = getFingerSpacing(event)
                    if (newDist > oldDist) {
                        handleZoom(true)
                    } else if (newDist < oldDist) {
                        handleZoom(false)
                    }
                    oldDist = newDist
                }
                else -> {
                }
            }
        }

        return true
    }

    /**
     * 获取焦点
     * */
    private fun handFocus(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (mCamera != null && isPreviewing) {
                if (isSupportAutoFocus) {
                    mCamera?.autoFocus({ _, _ -> })
                }
            }
        }
        return true
    }

    /**
     * 释放相机
     */
    private fun releaseCamera() {
        if (mCamera != null) {
            stopCameraPreview()
            mCamera!!.setPreviewCallback(null)
            mCamera!!.release()
            mCamera = null
        }
    }

    /**
     * 停止预览
     * */
    private fun stopCameraPreview() {
        if (mCamera != null && isPreviewing) {
            mCamera!!.stopPreview()
            isPreviewing = false
        }
    }

    /**
     * 打开摄像头
     * */
    private fun openCamera() {
        val cameraInfo = Camera.CameraInfo()
        val size = Camera.getNumberOfCameras()

        for (cameraId in 0 until size) {
            Camera.getCameraInfo(cameraId, cameraInfo)

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    mCamera = Camera.open()

                    cameraCallBack?.onCamera(mCamera!!)
                } catch (e: Exception) {
                    if (mCamera != null) {
                        mCamera!!.release()
                        mCamera = null
                    }
                }
            }
        }
    }

    /**
     * 加载相机配置
     * */
    private fun initCamera() {
        try {
            mCamera?.apply {
                //当前控件显示相机数据
                setPreviewDisplay(holder)
                //调整预览角度
                mRotation=setCameraDisplayOrientation(context,mCamera!!)
                setCameraParameters()
                startCameraPreview()//打开相机
            }
        } catch (e: Exception) {
//            releaseCamera()
        }

    }

    /**
     * 配置相机参数
     */
    private fun setCameraParameters() {
        mCamera?.let {
            val parameters = it.parameters
            val sizes = parameters.supportedPreviewSizes

            //确定前面定义的预览宽高是camera支持的，不支持取就更大的
            for (i in 0 until sizes.size) {
                if (sizes[i].width >= screenWidth && sizes[i].height >= screenHeight || i == sizes.size - 1) {
                    screenWidth = sizes[i].width
                    screenHeight = sizes[i].height
                    break
                }
            }

            //设置确定最终拍照图片的大小
            parameters.setPreviewSize(screenWidth, screenHeight)
            parameters.setPictureSize(screenWidth, screenHeight)
            //设置图片质量
            parameters.jpegQuality = 100
            parameters.setRotation(mRotation)
            it.parameters = parameters
        }
    }

    /**
     * 开始预览
     * */
    internal fun startCameraPreview() {
        mCamera?.apply {
            startPreview()
            isPreviewing = true
        }
    }

    /**
     * 拍照
     * */
    fun takePicture() {
        mCamera?.apply {
            autoFocus { _, camera ->
                camera.takePicture(null, null, object : Camera.PictureCallback {
                    override fun onPictureTaken(data: ByteArray, camera: Camera) {
                        val bitmap: Bitmap? = BitmapFactory.decodeByteArray(data, 0, data.size)

                        stopCameraPreview()

                        takePictureCallBack?.apply {
                            onPictureTaken(bitmap)
                        }
                    }
                })
            }
        }
    }

    interface TakePictureCallBack {
        /**
         * 拍照的回调
         */
        fun onPictureTaken(img: Bitmap?)
    }

    interface CameraCallBack {
        /**
         * 返回初始化成功的Camera对象
         * */
        fun onCamera(camera: Camera)
    }

    //获取屏幕旋转的方向对摄像头进行矫正，使视图正常
    fun setCameraDisplayOrientation(context: Context, camera: Camera):Int {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val rotation = display.rotation
        var degrees = 0
        degrees = when (rotation) {
            ROTATION_0 -> 0
            ROTATION_90 -> 90
            ROTATION_180 -> 180
            ROTATION_270 -> 270
            else -> 0
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;// compensate the mirror
        } else {// back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result)//显示翻转result度
        return result
    }

    private fun handleZoom(isZoom: Boolean) {
        mCamera?.apply {
            val params = parameters
            if (params.isZoomSupported) {
                val maxZoom = params.maxZoom
                var zoom = params.zoom

                if (isZoom && zoom < maxZoom) {
                    zoom++
                } else if (zoom > 0) {
                    zoom--
                }

                params.zoom = zoom
                parameters = params
            } else {
                Log.i(TAG, "zoom not supported")
            }
        }
    }

    /**
     * 获取手指触摸的距离
     * */
    private fun getFingerSpacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)

        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }
}