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

    /**
     * 手指按下后，100毫秒内未抬起、未移动；
     */
    override fun onShowPress(p0: MotionEvent?) {
        log("===>>>onShowPress")
    }

    /**
     * 手指按下后未移动，并在500毫秒内抬起（可以认定为单击）
     */
    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        log("===>>>onSingleTapUp")
        return false
    }

    /**
     * 手指按下
     */
    override fun onDown(p0: MotionEvent?): Boolean {
        log("===>>>onDown")
        return true
    }

    /**
     * 手指快速拖动后松手（惯性滚动）
     */
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

    /**
     * 手指拖动
     */
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

    /**
     * 长按（手指按下后，500毫秒内未抬起、未移动）
     */
    override fun onLongPress(p0: MotionEvent?) {
        log("===>>>onLongPress")
    }

    /**
     * 触发双击事件
     * （手指抬起后300毫秒内再次按下（注意：是再次按下时就触发，并不是等它抬起后才触发））
     */
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

    /**
     * 触发双击后的手指触摸事件，包括ACTION_DOWN、ACTION_MOVE、ACTION_UP
     * （注意：在触发长按后，不会继续收到ACTION_MOVE事件，因为在手指长按过程中，
     * 是不需要处理手指移动的动作的，也就是会直接忽略ACTION_MOVE的事件。
     * 还有，此方法回调后，在触发长按事件之前，如有新手指按下，则不再认定是双击了，
     * 所以不会继续回调此方法，取而代之的是onScroll）。
     * 此方法与上面的onDoubleTap方法的区别就是，
     * onDoubleTap在一次双击事件中只会回调一次，而这个方法能回调多次；
     */
    override fun onDoubleTapEvent(p0: MotionEvent?): Boolean {
        log("===>>>onDoubleTapEvent, $isBigType")
        return false
    }

    /**
     * 已经确认这是一次单击事件，想触发双击必须继续快速点击两次屏幕
     * （即：手指抬起之后，300毫秒内没等到手指再次按下）；
     */
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