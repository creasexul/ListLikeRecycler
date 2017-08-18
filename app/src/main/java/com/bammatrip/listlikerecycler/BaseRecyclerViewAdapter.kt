package com.bammatrip.listlikerecycler

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bammatrip.listlikerecycler.BaseRecyclerViewAdapter.IItem

/**
 * 采用[DataBindingUtil]的[RecyclerView.Adapter]
 * 子View的ViewModel需继承自[IItem]
 *
 *  @author Crease
 *  @version 1.0
 * */
internal class BaseRecyclerViewAdapter : RecyclerView.Adapter<BaseRecyclerViewAdapter.ItemViewHolder>() {

    private val iItemList = mutableListOf<IItem>()

    /** 设置[listOfT]为数据源*/
    fun <T : IItem> setItemList(listOfT : List<T>) {
        removeAll()

        insertItems(listOfT)
    }

    /** 添加[t]到[iItemList]的[position]位置*/
    fun <T : IItem> insertItem(position : Int, t : T) {
        val realPosition = position

        iItemList.add(position, t)
        notifyItemInserted(realPosition)
    }

    /** 添加[listOfT]到[iItemList]*/
    fun <T : IItem> insertItems(listOfT : List<T>) {
        val startPosition = iItemList.size

        iItemList.addAll(listOfT)
        notifyItemRangeInserted(startPosition, listOfT.size)
    }

    /** 从[iItemList]中移除[t] */
    fun <T : IItem> removeItem(t : T) {
        val removePosition = iItemList.indexOf(t)

        iItemList.remove(t)
        notifyItemRemoved(removePosition)
    }

    /** 从[iItemList]中所有元素*/
    fun removeAll() {
        val size = iItemList.size

        iItemList.clear()
        notifyItemRangeRemoved(0, size)
    }


    override fun getItemCount() : Int {
        return iItemList.size
    }

    override fun onBindViewHolder(holder : ItemViewHolder, position : Int) {
        holder.bindTo(iItemList[position], position)
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ItemViewHolder {
        return ItemViewHolder.create(parent, viewType)
    }

    override fun getItemViewType(position : Int) : Int {
        return iItemList[position].viewType
    }

    internal interface IItem {
        val variableId : Int
        val viewType : Int
        fun initBinding(viewDataBinding : ViewDataBinding, position : Int)
    }

    class ItemViewHolder(val viewDataBinding : ViewDataBinding) : RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            fun create(parent : ViewGroup, viewType : Int) : ItemViewHolder {
                val viewBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), viewType, parent,
                        false)
                return ItemViewHolder(viewBinding)
            }
        }

        fun bindTo(iItem : IItem, position : Int) {
            @Suppress("MISSING_DEPENDENCY_CLASS")
            viewDataBinding.setVariable(iItem.variableId, iItem)
            viewDataBinding.executePendingBindings()
            iItem.initBinding(viewDataBinding, position)
        }
    }

}