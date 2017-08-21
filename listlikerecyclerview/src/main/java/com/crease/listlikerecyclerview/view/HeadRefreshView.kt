package com.crease.listlikerecyclerview.view

import android.content.Context
import android.support.annotation.IntDef
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 * [ListLikeRecyclerView]的下拉刷新头布局抽象类
 * 继承自[LinearLayout]待定
 *
 * @author Crease
 * @version 1.0
 * */
public abstract class HeadRefreshView @JvmOverloads constructor(context : Context?,
                                                                attrs : AttributeSet? = null,
                                                                defStyleAttr : Int = 0,
                                                                defStyleRes : Int = 0)
    : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        const val STATE_NORMAL = 0L
        const val STATE_RELEASE_TO_REFRESH = 1L
        const val STATE_REFRESH = 2L
        const val STATE_DONE = 3L

        @Target(AnnotationTarget.VALUE_PARAMETER)
        @IntDef(STATE_NORMAL, STATE_RELEASE_TO_REFRESH, STATE_REFRESH, STATE_DONE)
        @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
        annotation class HeadRefreshState
    }


    abstract val isPreLoading : Boolean
    abstract val layoutId : Int
    abstract val state : Long


    abstract fun move(offsetX : Float, offsetY : Float)

    abstract fun setState(@HeadRefreshState state : Long)
}