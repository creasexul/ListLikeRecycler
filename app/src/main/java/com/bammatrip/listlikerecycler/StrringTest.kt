package com.bammatrip.listlikerecycler

import android.databinding.ViewDataBinding

class StringTest : BaseRecyclerViewAdapter.IItem {

    var content : String? = null

    override val variableId : Int
        get() = BR.string
    override val viewType : Int
        get() = R.layout.item_string

    override fun initBinding(viewDataBinding : ViewDataBinding, position : Int) {

    }
}