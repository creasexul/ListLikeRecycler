@file:Suppress("unused")

package com.crease.listlikerecyclerview

import com.crease.listlikerecyclerview.view.FootLoadView
import com.crease.listlikerecyclerview.view.HeadRefreshView
import com.crease.listlikerecyclerview.view.SimpleFootLoadView
import com.crease.listlikerecyclerview.view.SimpleHeadRefreshView

object ListLikeRecyclerViewClient {

    var headRefreshView: Class<out HeadRefreshView> = SimpleHeadRefreshView::class.java
        private set

    var footLoadView: Class<out FootLoadView> = SimpleFootLoadView::class.java
        private set

    fun initHeaderAndFooter(
            headRefreshView: Class<out HeadRefreshView> = SimpleHeadRefreshView::class.java,
            footLoadView: Class<out FootLoadView> = SimpleFootLoadView::class.java
    ) {
        this.headRefreshView = headRefreshView
        this.footLoadView = footLoadView
    }
}