package com.crease.listlikerecyclerview.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.crease.listlikerecyclerview.R

public class SimpleFootLoadView @JvmOverloads constructor(context: Context?,
                                                          attrs: AttributeSet? = null,
                                                          defStyleAttr: Int = 0,
                                                          defStyleRes: Int = 0)
    : FootLoadView(context, attrs, defStyleAttr, defStyleRes) {

    private val contentView = LayoutInflater.from(context).inflate(R.layout.layout_recycler_foot, this, true)
    private val textView = contentView.findViewById<TextView>(R.id.tv_content)
    private val progressBar = contentView.findViewById<ProgressBar>(R.id.progress)

    init {
        var layoutParams = contentView.layoutParams
        if (layoutParams == null) {
            layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            contentView.layoutParams = layoutParams
        }
    }

    override fun setState(state: Long) {

        when (state) {
            STATE_LOADING -> {
                progressBar.visibility = View.VISIBLE
                textView.text = context.getString(R.string.loading)
            }
            STATE_LOAD_FINISHED -> {
                progressBar.visibility = View.GONE
                textView.text = context.getString(R.string.loading_finished)
            }
            STATE_NO_MORE -> {
                progressBar.visibility = View.GONE
                textView.text = context.getString(R.string.no_more)
            }
        }

    }

    override val layoutId: Int
        get() {
            return R.layout.layout_recycler_foot
        }
}