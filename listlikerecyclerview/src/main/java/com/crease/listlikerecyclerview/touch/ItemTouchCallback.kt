package com.crease.listlikerecyclerview.touch

import android.support.annotation.CallSuper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.crease.listlikerecyclerview.view.ListLikeRecyclerView

/**
 * [ListLikeRecyclerView]的[ItemTouchHelper.Callback]
 *
 * 修正由[ListLikeRecyclerView.headerViewList]引起的位置偏差
 * 若采用[RecyclerView.ViewHolder.getAdapterPosition]需要手动修正位置
 * (减去[ListLikeRecyclerView.headViewSize])
 *
 * 如果需要自定义侧滑删除样式，重写[ItemTouchCallback.isItemViewSwipeEnabled]为false
 * 并在[ItemTouchCallback.onChildDraw]中判断actionState为[ItemTouchHelper.ACTION_STATE_SWIPE]时由item处理滑动事件
 *
 * @author Crease
 * @version 1.0
 */
abstract class ItemTouchCallback : ItemTouchHelper.Callback() {

    var dragFlag: Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN
    var swipeFlag: Int = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
    internal var callback: RecyclerItemSelectedChangeCallback? = null

    abstract fun onItemCanMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            position: Int,
            targetViewHolder: RecyclerView.ViewHolder,
            targetPosition: Int
    ): Boolean

    abstract fun onItemMoved(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            position: Int,
            targetViewHolder: RecyclerView.ViewHolder,
            targetPosition: Int,
            x: Int,
            y: Int
    )

    abstract fun onItemSwiped(
            viewHolder: RecyclerView.ViewHolder,
            position: Int,
            direction: Int
    )

    override fun isLongPressDragEnabled() = false

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return if (recyclerView is ListLikeRecyclerView) {
            val position = viewHolder.adapterPosition
            val headViewSize = recyclerView.headViewSize
            val contentSize = recyclerView.adapter?.itemCount ?: 0
            if (position in headViewSize until (headViewSize + contentSize)) {
                makeMovementFlags(dragFlag, swipeFlag)
            } else {
                makeMovementFlags(0, 0)
            }
        } else {
            makeMovementFlags(dragFlag, swipeFlag)
        }
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        return if (recyclerView is ListLikeRecyclerView) {
            val headViewSize = recyclerView.headViewSize
            val position = viewHolder.adapterPosition - headViewSize
            val targetPosition = target.adapterPosition - headViewSize
            val contentSize = recyclerView.adapter?.itemCount ?: 0

            if (position in 0 until contentSize && targetPosition in 0 until contentSize) {
                onItemCanMove(recyclerView, viewHolder, position, target, targetPosition)
            } else {
                false
            }
        } else {
            onItemCanMove(recyclerView, viewHolder, viewHolder.adapterPosition, target, target.adapterPosition)
        }
    }

    override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                         fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)

        if (recyclerView is ListLikeRecyclerView) {
            val headViewSize = recyclerView.headViewSize
            val position = viewHolder.adapterPosition - headViewSize
            val targetPosition = target.adapterPosition - headViewSize

            onItemMoved(recyclerView, viewHolder, position, target, targetPosition, x, y)
        } else {
            onItemMoved(recyclerView, viewHolder, viewHolder.adapterPosition,
                    target, target.adapterPosition, x, y)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        callback?.onSwiped(viewHolder, direction)
    }

    @CallSuper
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        callback?.onSelectedChanged(viewHolder, actionState)
    }

    interface RecyclerItemSelectedChangeCallback {
        fun onSelectedChanged(
                viewHolder: RecyclerView.ViewHolder?,
                actionState: Int
        )

        fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
        )
    }
}