package com.woshua.cameraproject

import android.os.Bundle
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity


class ShowPhotoVideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val relativeLayout = RelativeLayout(this)
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        val uri = intent.data
        if (intent.type == "image/*") {
            val view = ImageView(this)
            view.setImageURI(uri)
            view.layoutParams = layoutParams
            relativeLayout.addView(view)
        } else {
            val mc = MediaController(this)
            val view = VideoView(this)
            mc.setAnchorView(view)
            mc.setMediaPlayer(view)
            view.setMediaController(mc)
            view.setVideoURI(uri)
            view.start()
            view.layoutParams = layoutParams
            relativeLayout.addView(view)
        }
        setContentView(relativeLayout, layoutParams)
    }
}
