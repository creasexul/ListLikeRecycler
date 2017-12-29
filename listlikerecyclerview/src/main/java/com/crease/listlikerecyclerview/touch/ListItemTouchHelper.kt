@file:Suppress("unused", "MemberVisibilityCanPrivate")

package com.crease.listlikerecyclerview.touch

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.crease.listlikerecyclerview.view.ListLikeRecyclerView

/**
 * ListItemTouchHelper
 *
 * @author Crease
 * @version 1.0
 */
class ListItemTouchHelper : ItemTouchHelper.Callback() {

    var moveFlag: Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or
            ItemTouchHelper.RIGHT
    var dragFlag: Int = ItemTouchHelper.RIGHT

    override fun isLongPressDragEnabled(): Boolean = false

    override fun isItemViewSwipeEnabled(): Boolean = true

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (recyclerView is ListLikeRecyclerView) {
            val isHeaderOrFooter = recyclerView.isHeader(viewHolder.itemViewType)
                    || recyclerView.isFooter(viewHolder.itemViewType)

            if (isHeaderOrFooter) {
                return makeMovementFlags(0, 0)
            }
        }

        return makeMovementFlags(moveFlag, dragFlag)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return viewHolder.itemViewType == target.itemViewType
    }

    override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)

        if (recyclerView is ListLikeRecyclerView){

            val fromPos = viewHolder.adapterPosition - recyclerView.headViewSize
            val toPos = target.adapterPosition - recyclerView.headViewSize
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        //TODO remove this item
    }
}