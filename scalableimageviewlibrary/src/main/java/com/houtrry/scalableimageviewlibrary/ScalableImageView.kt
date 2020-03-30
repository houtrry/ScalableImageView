package com.houtrry.scalableimageviewlibrary

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * @author: houtrry
 * @time: 2020/3/29
 * @desc:
 */
class ScalableImageView : View, GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    private lateinit var mBitmap: Bitmap
    private val mPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mOriginalOffsetX: Float = 0F
    private var mOriginalOffsetY: Float = 0F
    private var mOffsetX: Float = 0F
    private var mOffsetY: Float = 0F

    private var mMinScale: Float = 0f
    private var mMaxScale: Float = 0f

    private var mCurrentScale: Float = 0f
    private var isBigType: Boolean = false
    private var scaleFraction: Float = 0f
        set(value) {
            field = value
            ViewCompat.postInvalidateOnAnimation(this)
        }

    private var mScaleCenterX = 0f
    private var mScaleCenterY = 0f

    private val mGestureDetector: GestureDetectorCompat by lazy {
        GestureDetectorCompat(context, this)
    }

    private val mScroller: OverScroller by lazy {
        OverScroller(context)
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    companion object {
        private const val TAG: String = "ScalableImageView"
    }

    init {
        mBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.test)
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h

        if (mBitmap.width * 1.0f / mBitmap.height > w * 1.0f / h) {
            mMinScale = w * 1.0f / mBitmap.width
            mMaxScale = h * 1.0f / mBitmap.height
        } else {
            mMinScale = h * 1.0f / mBitmap.height
            mMaxScale = w * 1.0f / mBitmap.width
        }

        mOriginalOffsetX = (w - mBitmap.width) * 0.5f
        mOriginalOffsetY = (h - mBitmap.height) * 0.5f

        mScaleCenterX = w * 0.5f
        mScaleCenterY = h * 0.5f
        log("mMinScale: $mMinScale, mMaxScale: $mMaxScale")
        log("mWidth: $mWidth, mHeight: $mHeight, mBitmap:(${mBitmap.width}, ${mBitmap.height}), mOffsetX: $mOriginalOffsetX, mOffsetY： $mOriginalOffsetY")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawColor(Color.RED)
            it.translate(mOffsetX * scaleFraction, mOffsetY * scaleFraction)
            mCurrentScale = mMinScale + scaleFraction * (mMaxScale - mMinScale)
            it.scale(mCurrentScale, mCurrentScale, mScaleCenterX, mScaleCenterY)
            it.drawBitmap(mBitmap, mOriginalOffsetX, mOriginalOffsetY, mPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mGestureDetector.onTouchEvent(event)
    }

    override fun onShowPress(p0: MotionEvent?) {
        log("===>>>onShowPress")
    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        log("===>>>onSingleTapUp")
        return false
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        log("===>>>onDown")
        return true
    }

    override fun onFling(
        downEvent: MotionEvent?,
        currentEvent: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        log("===>>>onFling")
        if (isBigType) {
            mScroller.fling(
                mOffsetX.toInt(),
                mOffsetY.toInt(),
                velocityX.toInt(),
                velocityY.toInt(),
                (-abs(mWidth - mBitmap.width * mMaxScale) * 0.5f).toInt(),
                (abs(mWidth - mBitmap.width * mMaxScale) * 0.5f).toInt(),
                (-abs(mHeight - mBitmap.height * mMaxScale) * 0.5f).toInt(),
                (abs(mHeight - mBitmap.height * mMaxScale) * 0.5f).toInt()
            )
        }
        return false
    }

    override fun onScroll(
        downEvent: MotionEvent?,
        currentEvent: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        log("===>>>onScroll, downEvent: $downEvent")
        log("===>>>onScroll, currentEvent: $currentEvent")
        log("===>>>onScroll, distanceX: $distanceX")
        log("===>>>onScroll, distanceY: $distanceY")

        if (isBigType) {
            mOffsetX -= distanceX
            mOffsetY -= distanceY

            mOffsetX = max(mOffsetX, -abs(mWidth - mBitmap.width * mMaxScale) * 0.5f)
            mOffsetX = min(mOffsetX, abs(mWidth - mBitmap.width * mMaxScale) * 0.5f)

            mOffsetY = max(mOffsetY, -abs(mHeight - mBitmap.height * mMaxScale) * 0.5f)
            mOffsetY = min(mOffsetY, abs(mHeight - mBitmap.height * mMaxScale) * 0.5f)

            log("===>>>onScroll, mOffsetX: $mOffsetX, mOffsetY: $mOffsetY")
            ViewCompat.postInvalidateOnAnimation(this)
        }

        return false
    }

    override fun onLongPress(p0: MotionEvent?) {
        log("===>>>onLongPress")
    }

    override fun onDoubleTap(p0: MotionEvent?): Boolean {
        log("===>>>onDoubleTap")
        isBigType = !isBigType
        if (isBigType) {
            mScaleCenterX = p0!!.x
            mScaleCenterX = p0.y

            mOffsetX = 0f
            mOffsetY = 0f

        }
        displayAnimator()
        return false
    }

    override fun onDoubleTapEvent(p0: MotionEvent?): Boolean {
        log("===>>>onDoubleTapEvent, $isBigType")
        return false
    }

    override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean {
        log("===>>>onSingleTapConfirmed")
        return false
    }

    override fun computeScroll() {
        super.computeScroll()
        log("===>>>computeScroll")
        if (mScroller.computeScrollOffset()) {
            mOffsetX = mScroller.currX.toFloat()
            mOffsetY = mScroller.currY.toFloat()
            log("===>>>computeScroll， mOffsetX: $mOffsetX, mOffsetY: $mOffsetY")
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private val scaleAnimator: ObjectAnimator by lazy {
        val animator = ObjectAnimator.ofFloat(this, "scaleFraction", 0f, 1f)
        animator.interpolator = LinearInterpolator()
        animator.duration = 2000
        animator
    }

    private fun displayAnimator() {
        if (isBigType) {
            scaleAnimator.start()
        } else {
            scaleAnimator.reverse()
        }
    }

}