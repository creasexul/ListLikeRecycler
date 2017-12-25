package com.crease.listlikerecyclerview

import android.content.Context
import com.crease.listlikerecyclerview.view.FootLoadView
import com.crease.listlikerecyclerview.view.HeadRefreshView
import com.crease.listlikerecyclerview.view.SimpleFootLoadView
import com.crease.listlikerecyclerview.view.SimpleHeadRefreshView

object ListLikeRecyclerViewClient {

    var headRefreshView: HeadRefreshView? = null
        private set

    var footLoadView: FootLoadView? = null
        private set

    fun init(
            context: Context,
            headRefreshView: HeadRefreshView = SimpleHeadRefreshView(context),
            footLoadView: FootLoadView = SimpleFootLoadView(context)
    ) {

        this.headRefreshView = headRefreshView
        this.footLoadView = footLoadView

    }
}