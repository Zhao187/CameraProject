package com.woshua.cameraproject.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * @author Steven.zhao
 * email:hongtu.zhao@goodwinsoft.net
 * date:2019/7/28
 * desc:
 */
class MaskView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private val mPaint_text:Paint

    private lateinit var mCardRectF:RectF
    private var cardWidth:Float=0f


    init {
        setLayerType(LAYER_TYPE_SOFTWARE,null)
        mPaint.style=Paint.Style.FILL
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        mPaint_text= Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint_text.textSize=60f
        mPaint_text.color=Color.WHITE
        mPaint_text.textAlign=Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        //由于身份证的长为85.6mm，身份证的宽为54mm因此我们可以得出身份证的长宽比
        val ratio = (85.6 / 54).toFloat()

        //这里我们设置显示身份证区域的宽为整个控件宽度的2/3
        cardWidth=(w/3.0*2).toFloat()

        //身份证高度
        val cardHeight=cardWidth*ratio

        mCardRectF= RectF(((w-cardWidth)/2),(h-cardHeight)/2, (w-(w-cardWidth)/2),h-(h-cardHeight)/2)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

       canvas?.apply {
           save()

           drawColor(Color.parseColor("#aa000000"))

           drawRect(mCardRectF,mPaint)
           //清除混合模式
           mPaint.xfermode = null

           translate((width /2).toFloat(), (height /2).toFloat())
           rotate(90f)
           drawText("请扫描本人身份证人像面",0f,-cardWidth/2-20,mPaint_text);
           restore()
       }
    }
}