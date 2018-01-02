@file:Suppress("unused", "MemberVisibilityCanPrivate")

package com.crease.listlikerecyclerview.touch

import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.GestureDetector
import android.view.MotionEvent
import com.crease.listlikerecyclerview.view.ListLikeRecyclerView

/**
 * ListItemTouchHelper
 *
 * 当[ListLikeRecyclerView]的item被选中时，阻止[ListLikeRecyclerView.headRefreshView]获取滑动事件
 *
 * @author Crease
 * @version 1.0
 */
class ListItemTouchHelper(
        private val itemTouchCallback: ItemTouchCallback
) : ItemTouchHelper(itemTouchCallback) {

    companion object {
        private const val TAG = "ListItemTouchHelper"
    }

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)

        if (recyclerView is ListLikeRecyclerView) {
            itemTouchCallback.callback = object : ItemTouchCallback.RecyclerItemSelectedChangeCallback {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition - recyclerView.headViewSize
                    itemTouchCallback.onItemSwiped(viewHolder, position, direction)
                }

                override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                    recyclerView.isInterruptTouchEvent = viewHolder != null
                }
            }

            val gestureDetector = GestureDetectorCompat(recyclerView.context,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onLongPress(e: MotionEvent) {
                            if (! recyclerView.preRefreshing) {

                                val childView = recyclerView.findChildViewUnder(e.x, e.y)
                                if (null != childView) {

                                    startDrag(recyclerView.getChildViewHolder(childView))
                                }
                            }

                        }
                    })

            recyclerView.addOnItemTouchListener(
                    object : RecyclerView.SimpleOnItemTouchListener() {
                        override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
                            return gestureDetector.onTouchEvent(e)
                        }
                    })
        } else if (null != recyclerView) {

            val gestureDetector = GestureDetectorCompat(recyclerView.context,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onLongPress(e: MotionEvent) {
                            val childView = recyclerView.findChildViewUnder(e.x, e.y)
                            if (null != childView) {
                                val itemPosition = recyclerView.getChildLayoutPosition(childView)

                                startDrag(recyclerView.getChildViewHolder(recyclerView.getChildAt(itemPosition)))
                            }
                        }
                    })

            recyclerView.addOnItemTouchListener(
                    object : RecyclerView.SimpleOnItemTouchListener() {
                        override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
                            return gestureDetector.onTouchEvent(e)
                        }
                    })
        }
    }
}