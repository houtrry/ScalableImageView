package com.houtrry.scalableimageviewlibrary

import android.content.res.Resources
import android.util.TypedValue

/**
 * @author: houtrry
 * @time: 2020/3/29
 * @desc:
 */
fun dipToPx(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        Resources.getSystem().displayMetrics
    )
}