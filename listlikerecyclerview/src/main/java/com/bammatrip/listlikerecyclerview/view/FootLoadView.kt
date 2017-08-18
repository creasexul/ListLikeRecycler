package com.bammatrip.listlikerecyclerview.view

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
public abstract class FootLoadView @JvmOverloads constructor(context : Context?,
                                                      attrs : AttributeSet? = null,
                                                      defStyleAttr : Int = 0,
                                                      defStyleRes : Int = 0)
    : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {


    companion object {
        const val STATE_LOADING = 0L
        const val STATE_LOAD_FINISHED = 1L
        const val STATE_NO_MORE = 2L

        @Target(AnnotationTarget.VALUE_PARAMETER)
        @IntDef(STATE_LOADING, STATE_LOAD_FINISHED, STATE_NO_MORE)
        @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
        annotation class FooterLoadState
    }


    abstract val layoutId : Int


    abstract fun setState(@FooterLoadState state : Long)

}