package com.crease.listlikerecyclerview.view

import android.support.annotation.IntDef
import android.support.design.widget.AppBarLayout

public abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {
    companion object {
        const val EXPANDED = 0L
        const val COLLAPSED = 1L
        const val IDLE = 2L

        @Target(AnnotationTarget.VALUE_PARAMETER)
        @IntDef(EXPANDED, COLLAPSED, IDLE)
        @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
        annotation class AppBarState
    }

    private var state = IDLE

    override fun onOffsetChanged(appBarLayout : AppBarLayout, verticalOffset : Int) {
        when {
            verticalOffset == 0 -> {
                if (state != EXPANDED) {
                    onStateChanged(appBarLayout, EXPANDED)
                }
                state = EXPANDED
            }
            Math.abs(verticalOffset) >= appBarLayout.totalScrollRange -> {
                if (state != COLLAPSED) {
                    onStateChanged(appBarLayout, COLLAPSED);
                }
                state = COLLAPSED
            }
            else -> {
                if (state != IDLE) {
                    onStateChanged(appBarLayout, IDLE);
                }
                state = IDLE
            }
        }
    }

    abstract fun onStateChanged(appBarLayout : AppBarLayout, @AppBarState state : Long)
}