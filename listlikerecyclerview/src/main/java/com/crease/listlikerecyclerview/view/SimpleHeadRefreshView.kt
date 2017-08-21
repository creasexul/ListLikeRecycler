package com.crease.listlikerecyclerview.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import com.crease.listlikerecyclerview.R

public class SimpleHeadRefreshView @JvmOverloads constructor(context : Context?,
                                                             attrs : AttributeSet? = null,
                                                             defStyleAttr : Int = 0,
                                                             defStyleRes : Int = 0)
    : HeadRefreshView(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val ANIMATION_DURATION = 300L
    }

    private var currentState : Long = STATE_NORMAL
    private val contentPaddingLeft : Int
    private val contentPaddingRight : Int
    private val contentPaddingTop : Int
    private val contentPaddingBottom : Int
    private val parentPaddingLeft : Int
    private val parentPaddingRight : Int
    private val parentPaddingTop : Int
    private val parentPaddingBottom : Int
    private var contentHeight : Int = 0
    private var contentWidth : Int = 0

    override val state : Long
        get() = currentState

    private val contentView = LayoutInflater.from(context).inflate(R.layout.layout_recycler_head, this, true)

    override val layoutId : Int
        get() = R.layout.layout_recycler_head

    override val isPreLoading : Boolean
        get() = currentState == STATE_RELEASE_TO_REFRESH

    init {
        post {
            contentHeight = contentView.measuredHeight
            contentWidth = contentView.measuredWidth

            val layoutParams = contentView.layoutParams
            layoutParams.width = 0
            layoutParams.height = 0
            contentView.layoutParams = layoutParams
        }

        contentPaddingLeft = contentView.paddingLeft
        contentPaddingRight = contentView.paddingRight
        contentPaddingTop = contentView.paddingTop
        contentPaddingBottom = contentView.paddingBottom

        val layoutParams = contentView.layoutParams
        if (layoutParams is MarginLayoutParams) {
            parentPaddingLeft = layoutParams.leftMargin
            parentPaddingRight = layoutParams.rightMargin
            parentPaddingTop = layoutParams.topMargin
            parentPaddingBottom = layoutParams.bottomMargin

        } else {
            parentPaddingLeft = 0
            parentPaddingRight = 0
            parentPaddingTop = 0
            parentPaddingBottom = 0
        }

    }

    override fun move(offsetX : Float, offsetY : Float) {

        val contentPaddingLeft : Int
        val contentPaddingRight : Int
        val contentPaddingTop : Int
        val contentPaddingBottom : Int
        val parentPaddingLeft : Int
        val parentPaddingRight : Int
        val parentPaddingTop : Int
        val parentPaddingBottom : Int
        val width : Int
        val height : Int

        when {
            offsetX <= 0f && offsetY <= 0f -> {
                contentPaddingLeft = 0
                contentPaddingRight = 0
                parentPaddingLeft = 0
                parentPaddingRight = 0

                width = 0

                if (currentState != STATE_NORMAL) {
                    currentState = STATE_NORMAL
                }
            }

            offsetX == 0f -> {
                contentPaddingLeft = this.contentPaddingLeft
                contentPaddingRight = this.contentPaddingRight
                parentPaddingLeft = this.parentPaddingLeft
                parentPaddingRight = this.parentPaddingRight

                width = contentWidth

                if (currentState != STATE_NORMAL) {
                    currentState = STATE_NORMAL
                }
            }

            offsetX < contentWidth && offsetX > 0 -> {
                val ratio = offsetX / contentWidth

                contentPaddingLeft = (this.contentPaddingLeft * ratio).toInt()
                contentPaddingRight = (this.contentPaddingRight * ratio).toInt()
                parentPaddingLeft = (this.parentPaddingLeft * ratio).toInt()
                parentPaddingRight = (this.parentPaddingRight * ratio).toInt()

                width = offsetX.toInt()

                if (currentState != STATE_NORMAL) {
                    currentState = STATE_NORMAL
                }
            }

            else -> {
                contentPaddingLeft = this.contentPaddingLeft
                contentPaddingRight = this.contentPaddingRight
                parentPaddingLeft = this.parentPaddingLeft
                parentPaddingRight = this.parentPaddingRight

                width = contentWidth

                if (currentState != STATE_RELEASE_TO_REFRESH) {
                    currentState = STATE_RELEASE_TO_REFRESH
                }
            }
        }

        when {
            offsetX <= 0f && offsetY <= 0f -> {
                contentPaddingTop = 0
                contentPaddingBottom = 0
                parentPaddingTop = 0
                parentPaddingBottom = 0

                height = 0

                if (currentState != STATE_NORMAL) {
                    currentState = STATE_NORMAL
                }
            }

            offsetY == 0f -> {
                contentPaddingTop = this.contentPaddingTop
                contentPaddingBottom = this.contentPaddingBottom
                parentPaddingTop = this.parentPaddingTop
                parentPaddingBottom = this.parentPaddingBottom

                height = contentHeight

                if (currentState != STATE_NORMAL) {
                    currentState = STATE_NORMAL
                }
            }

            offsetY > 0 && offsetY < contentHeight -> {
                val ratio = offsetY / contentHeight

                contentPaddingTop = (this.contentPaddingTop * ratio).toInt()
                contentPaddingBottom = (this.contentPaddingBottom * ratio).toInt()
                parentPaddingTop = (this.parentPaddingTop * ratio).toInt()
                parentPaddingBottom = (this.parentPaddingBottom * ratio).toInt()

                height = offsetY.toInt()

                if (currentState != STATE_NORMAL) {
                    currentState = STATE_NORMAL
                }
            }

            else -> {
                contentPaddingTop = this.contentPaddingTop
                contentPaddingBottom = this.contentPaddingBottom
                parentPaddingTop = this.parentPaddingTop
                parentPaddingBottom = this.parentPaddingBottom

                height = contentHeight

                if (currentState != STATE_RELEASE_TO_REFRESH) {
                    currentState = STATE_RELEASE_TO_REFRESH
                }
            }
        }

        setPadding(parentPaddingLeft, parentPaddingTop, parentPaddingRight, parentPaddingBottom)
        contentView.setPadding(contentPaddingLeft, contentPaddingTop, contentPaddingRight, contentPaddingBottom)
        val layoutParams = contentView.layoutParams
        layoutParams.height = height
        layoutParams.width = width
        contentView.layoutParams = layoutParams
    }

    override fun setState(state : Long) {
        currentState = state

        when (state) {
            STATE_REFRESH -> paramsChanged(1f)
            else -> paramsChanged(0f)
        }
    }

    private fun paramsChanged(ratio : Float) {

        val currentRatio : Float
        val layoutParams = contentView.layoutParams
        currentRatio = if (layoutParams.height == contentHeight) {
            layoutParams.width.toFloat() / contentWidth
        } else {
            layoutParams.height.toFloat() / contentHeight
        }

        val valueAnimator = ValueAnimator.ofFloat(currentRatio, ratio)
        valueAnimator.addUpdateListener {

            val currentValue = it.animatedValue as Float
            val contentPaddingLeft = (this.contentPaddingLeft * currentValue).toInt()
            val contentPaddingRight = (this.contentPaddingRight * currentValue).toInt()
            val contentPaddingTop = (this.contentPaddingTop * currentValue).toInt()
            val contentPaddingBottom = (this.contentPaddingBottom * currentValue).toInt()
            val parentPaddingLeft = (this.parentPaddingLeft * currentValue).toInt()
            val parentPaddingRight = (this.parentPaddingRight * currentValue).toInt()
            val parentPaddingTop = (this.parentPaddingTop * currentValue).toInt()
            val parentPaddingBottom = (this.parentPaddingBottom * currentValue).toInt()
            val width = (this.width * currentValue).toInt()
            val height = (this.height * currentValue).toInt()

            setPadding(parentPaddingLeft, parentPaddingTop, parentPaddingRight, parentPaddingBottom)
            contentView.setPadding(contentPaddingLeft, contentPaddingTop, contentPaddingRight, contentPaddingBottom)
            val contentLayoutParams = contentView.layoutParams
            contentLayoutParams.height = height
            contentLayoutParams.width = width
            contentView.layoutParams = contentLayoutParams
        }

        valueAnimator.setDuration(ANIMATION_DURATION).start()

    }
}