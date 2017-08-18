package com.bammatrip.listlikerecyclerview.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import com.bammatrip.listlikerecyclerview.R

public class SimpleFootLoadView @JvmOverloads constructor(context : Context?,
                                                          attrs : AttributeSet? = null,
                                                          defStyleAttr : Int = 0,
                                                          defStyleRes : Int = 0)
    : FootLoadView(context, attrs, defStyleAttr, defStyleRes) {

    private val contentView = LayoutInflater.from(context).inflate(R.layout.layout_recycler_foot, this, true)
    private val textView = contentView.findViewById<TextView>(R.id.textview)

    override fun setState(state : Long) {

        when (state) {
            STATE_LOADING -> textView.text = context.getString(R.string.loading)
            STATE_LOAD_FINISHED -> textView.text = context.getString(R.string.loadingFinished)
            STATE_NO_MORE -> textView.text = context.getString(R.string.noMore)
        }

    }

    override val layoutId : Int
        get() {
            return R.layout.layout_recycler_foot
        }
}