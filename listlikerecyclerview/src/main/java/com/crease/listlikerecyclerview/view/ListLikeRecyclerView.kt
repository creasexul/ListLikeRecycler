package com.crease.listlikerecyclerview.view

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.CallSuper
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import android.util.SparseArray
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.crease.listlikerecyclerview.R


/**
 * [RecyclerView]派生类
 * 实现Item点击事件，添加去除headerView, footerView, 下拉刷新上拉加载
 *
 * @author Crease
 * @version 1.0
 * */
public class ListLikeRecyclerView @JvmOverloads constructor(context: Context,
                                                            attrs: AttributeSet? = null,
                                                            defStyle: Int = 0)
    : RecyclerView(context, attrs, defStyle) {

    companion object {

        private val DEFAULT_ELASTIC = 3

    }

    /** ---------------------------------------- private val ---------------------------------------------*/
    private val dataObserver = DataObserver()

    /** ---------------------------------------- private var ---------------------------------------------*/
    private var lastY = 0f

    private var appBarState = AppBarStateChangeListener.EXPANDED
    private var maskAdapter: ListLikeMaskAdapter<*>? = null
    private var headIds = mutableListOf<Int>()
    private var footIds = mutableListOf<Int>()
    private var headerViewList = SparseArray<View>()
    private var footerViewList = SparseArray<View>()
    private var gestureDetector: GestureDetectorCompat? = null
    private val itemTouchListener = object : SimpleOnItemTouchListener() {
        override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
            gestureDetector?.onTouchEvent(e)
            return false
        }
    }

    /** ---------------------------------------- internal var ---------------------------------------------*/
    var isRefreshing = false
        set(value) {
            if (field != value) {
                if (value) {
                    headRefreshView.setState(HeadRefreshView.Companion.STATE_REFRESH)
                    loadCallback?.onHeadRefresh()
                } else {
                    headRefreshView.setState(HeadRefreshView.Companion.STATE_DONE)
                }
                field = value
            }
        }

    var isLoading = false
        set(value) {
            if (field != value) {
                if (! value && ! noMore) {
                    footLoadView.setState(FootLoadView.STATE_LOAD_FINISHED)
                }else if (value){
                    footLoadView.setState(FootLoadView.STATE_LOADING)
                    loadCallback?.onFootLoad()
                }

                field = value
            }
        }

    var noMore = false
        set(value) {
            if (field != value) {
                if (value) {
                    footLoadView.setState(FootLoadView.STATE_NO_MORE)
                } else {
                    footLoadView.setState(FootLoadView.STATE_LOADING)
                }
                field = value
            }
        }

    var emptyView: View? = null
        set(value) {
            field = value
            dataObserver.onChanged()
        }

    var refreshEnabled = false
        set(value) {
            field = value
            if (value) {
                headIds.add(0, headRefreshView.layoutId)
                headerViewList.put(headRefreshView.layoutId, headRefreshView)
            } else {
                headIds.remove(headRefreshView.layoutId)
                headerViewList.remove(headRefreshView.layoutId)
            }
        }

    var loadingEnabled = false
        set(value) {
            field = value
            if (value) {
                footIds.add(0, footLoadView.layoutId)
                footerViewList.put(footLoadView.layoutId, footLoadView)
                footLoadView.setState(FootLoadView.STATE_NO_MORE)
            } else {
                footIds.remove(footLoadView.layoutId)
                footerViewList.remove(footLoadView.layoutId)
            }
        }

    var headRefreshView: HeadRefreshView = SimpleHeadRefreshView(context)
        set(value) {
            val index = headIds.indexOf(field.layoutId)
            if (index >= 0) {
                headIds.removeAt(index)
                headerViewList.remove(field.layoutId)
                headIds.add(index, value.layoutId)
                headerViewList.put(value.layoutId, value)
            }
            field = value
        }

    var footLoadView: FootLoadView = SimpleFootLoadView(context)
        set(value) {
            val index = footIds.indexOf(field.layoutId)
            if (index >= 0) {
                footIds.removeAt(index)
                footerViewList.remove(field.layoutId)
                footIds.add(index, value.layoutId)
                footerViewList.put(value.layoutId, value)
            }
            field = value
        }

    var loadCallback: OnRecyclerViewLoadCallback? = null

    init {

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListLikeRecyclerView, defStyle, 0)

            refreshEnabled = typedArray.getBoolean(R.styleable.ListLikeRecyclerView_refreshable, false)
            loadingEnabled = typedArray.getBoolean(R.styleable.ListLikeRecyclerView_loadable, false)

            typedArray.recycle()
        }

    }

    /** ---------------------------------------- override ---------------------------------------------*/
    override fun setAdapter(adapter: Adapter<*>?) {
        maskAdapter = ListLikeMaskAdapter(adapter)
        super.setAdapter(maskAdapter)

        adapter?.registerAdapterDataObserver(dataObserver)
        dataObserver.onChanged()
    }

    override fun getAdapter(): Adapter<*>? {
        return maskAdapter?.adapter
    }

    override fun setLayoutManager(layout: LayoutManager) {
        super.setLayoutManager(layout)
        if (layout is GridLayoutManager) {
            val gridLayoutManager = layoutManager as GridLayoutManager
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (isInHead(position) || isInFoot(position)) {
                        gridLayoutManager.spanCount
                    } else {
                        1
                    }
                }
            }
        }
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == RecyclerView.SCROLL_STATE_IDLE && null != loadCallback && loadingEnabled && ! isLoading) {
            val lastPosition: Int
            lastPosition = when (layoutManager) {
                is GridLayoutManager -> (layoutManager as GridLayoutManager).findLastVisibleItemPosition()
                is StaggeredGridLayoutManager -> {
                    val staggeredManager = layoutManager as StaggeredGridLayoutManager
                    val lastSpan = IntArray(staggeredManager.spanCount)
                    staggeredManager.findLastVisibleItemPositions(lastSpan)
                    lastSpan.max() ?: 0
                }
                else -> (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            }

            if (layoutManager.childCount > 0 && lastPosition >= layoutManager.itemCount && layoutManager.itemCount >
                    layoutManager.childCount && ! noMore) {

                isLoading = true
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {

        if (! isTop()) {
            return super.onTouchEvent(e)
        }

        val currentPos = if (layoutManager.canScrollHorizontally()) {
            e.rawX
        } else {
            e.rawY
        }

        if (- 1f == lastY) {
            lastY = currentPos
        }

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = currentPos
            }

            MotionEvent.ACTION_MOVE -> {
                val offset = currentPos - lastY
                if (isTop() && refreshEnabled && appBarState == AppBarStateChangeListener.EXPANDED) {
                    if (layoutManager.canScrollHorizontally()) {
                        headRefreshView.move(offset / DEFAULT_ELASTIC, 0f)
                    } else {
                        headRefreshView.move(0f, offset / DEFAULT_ELASTIC)
                    }
                    if (headRefreshView.layoutParams.height > 0 && headRefreshView.state < HeadRefreshView.Companion
                            .STATE_REFRESH) {
                        return false
                    }
                }
            }

            else -> {
                lastY = - 1f
                if (isTop() && refreshEnabled && appBarState == AppBarStateChangeListener.EXPANDED && headRefreshView.isPreLoading) {
                    isRefreshing = true
                } else {
                    headRefreshView.setState(HeadRefreshView.Companion.STATE_NORMAL)
                }
            }
        }
        return super.onTouchEvent(e)
    }


    /** ---------------------------------------- internal fun declare ---------------------------------------------*/

    fun addHeaderView(id: Int, headerView: View, position: Int = - 1) {

        val isContain = headIds.contains(id)

        if (isContain) {
            headIds.remove(id)
        }

        val realPosition = if (position < 0) headIds.size else {
            position + (if (refreshEnabled) 1 else 0)
        }

        headerViewList.put(id, headerView)
        if (position < 0 || position > headIds.size) {
            headIds.add(id)
        } else {
            headIds.add(realPosition, id)
        }

        //通过真正adapter刷新达到头部动态添加效果
        if (isContain) {
            maskAdapter?.adapter?.notifyDataSetChanged()
        } else {
            maskAdapter?.adapter?.notifyItemInserted(0)
        }
    }

    fun addFooterView(id: Int, footerView: View, position: Int = - 1) {
        val isContain = footIds.contains(id)

        val maxPosition = footIds.size - (if (loadingEnabled) 1 else 0) - 1

        footerViewList.put(id, footerView)

        if (position < 0 || position > maxPosition) {
            footIds.add(maxPosition + 1, id)
        } else {
            footIds.add(position, id)
        }

        //通过真正adapter刷新达到足部动态添加效果
        if (isContain) {
            maskAdapter?.adapter?.notifyDataSetChanged()
        } else {
            maskAdapter?.adapter?.notifyItemInserted(maskAdapter !!.itemCount - footIds.size)
        }
    }

    fun removeHeaderView(id: Int) {
        if (headIds.contains(id)) {

            headIds.remove(id)
            headerViewList.remove(id)

            //通过真正adapter刷新达到头部动态删除效果
            maskAdapter?.adapter?.notifyItemRemoved(0)
        }
    }

    fun removeFooterView(id: Int) {
        if (footIds.contains(id)) {

            footIds.remove(id)
            footerViewList.remove(id)

            //通过真正adapter刷新达到尾部动态删除效果
            maskAdapter?.adapter?.notifyItemRemoved(maskAdapter !!.itemCount - footIds.size)
        }

    }

    fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
        gestureDetector ?: let { addOnItemTouchListener(itemTouchListener) }

        gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val childView = findChildViewUnder(e.x, e.y)
                return if (null != childView) {
                    val itemPosition = getChildLayoutPosition(childView)
                    if (itemPosition in headerViewList.size() until (maskAdapter?.itemCount ?: 0)
                            - footerViewList.size()) {
                        itemClickListener.onItemClick(childView, getChildLayoutPosition(childView) - headerViewList.size())
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }

            override fun onLongPress(e: MotionEvent) {
                val childView = findChildViewUnder(e.x, e.y)
                if (null != childView) {
                    val itemPosition = getChildLayoutPosition(childView)
                    if (itemPosition in headerViewList.size() until (maskAdapter?.itemCount ?: 0)
                            - footerViewList.size()) {
                        itemClickListener.onItemLongClick(childView, getChildLayoutPosition(childView) - headerViewList.size())
                    }
                }

            }
        })
    }


    /** ---------------------------------------- private fun declare ---------------------------------------------*/
    private fun isTop(): Boolean {
        return headRefreshView.parent != null
    }

    private fun isInHead(position: Int): Boolean {
        return position < headerViewList.size()
    }

    private fun isInFoot(position: Int): Boolean {

        val itemCount = (maskAdapter?.itemCount ?: 0) - footerViewList.size() - 1
        return position > itemCount
    }


    /** ------------------------------------ inner class declare ---------------------------------------------*/
    /**
     * 作为[ListLikeRecyclerView]的[RecyclerView.Adapter]的托管类
     * 实现头布局和尾布局的显示以及将[ListLikeRecyclerView]中的主体交由真正[RecyclerView.Adapter]处理
     * */
    @Suppress("UNCHECKED_CAST")
    private inner class ListLikeMaskAdapter<VH : ViewHolder>(val adapter: Adapter<VH>?)
        : RecyclerView.Adapter<ViewHolder>() {

        override fun getItemCount(): Int {
            adapter?.let { return headerViewList.size() + adapter.itemCount + footerViewList.size() }
            return headerViewList.size() + footerViewList.size()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
            return when {
                headIds.contains(viewType) -> SimpleViewHolder(headerViewList.get(viewType))
                footIds.contains(viewType) -> SimpleViewHolder(footerViewList.get(viewType))
                null != adapter -> adapter.onCreateViewHolder(parent, viewType)
                else -> null
            }
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            if (isInHead(position) || isInFoot(position)) {
                return
            }

            holder?.let {

                val realPosition = position - headerViewList.size()
                adapter?.onBindViewHolder(holder as VH, realPosition)
            }
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int, payloads: MutableList<Any>?) {
            if (isInHead(position) || isInFoot(position)) {
                return
            }

            holder?.let {

                val realPosition = position - headerViewList.size()
                payloads?.let {
                    if (payloads.isEmpty()) {
                        adapter?.onBindViewHolder(holder as VH, realPosition)
                    } else {
                        adapter?.onBindViewHolder(holder as VH, realPosition, payloads)
                    }
                }
            }
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
            super.onAttachedToRecyclerView(recyclerView)
            if (layoutManager is GridLayoutManager) {
                val gridLayoutManager = layoutManager as GridLayoutManager
                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (isInHead(position) || isInFoot(position)) {
                            gridLayoutManager.spanCount
                        } else {
                            1
                        }
                    }
                }
            }

            adapter?.onAttachedToRecyclerView(recyclerView)
        }

        override fun onViewRecycled(holder: ViewHolder?) {
            adapter?.onViewRecycled(holder as VH)
        }

        override fun onFailedToRecycleView(holder: ViewHolder?): Boolean {
            return adapter?.onFailedToRecycleView(holder as VH) ?: false
        }

        override fun getItemId(position: Int): Long {
            if (null != adapter && ! isInHead(position) && ! isInFoot(position)) {
                val realPosition = position - headerViewList.size()
                return adapter.getItemId(realPosition)
            }

            return - 1L
        }

        override fun setHasStableIds(hasStableIds: Boolean) {
            adapter?.setHasStableIds(hasStableIds)
        }

        override fun unregisterAdapterDataObserver(observer: AdapterDataObserver?) {
            adapter?.unregisterAdapterDataObserver(observer)
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder?) {
            adapter?.onViewDetachedFromWindow(holder as VH)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
            adapter?.onDetachedFromRecyclerView(recyclerView)
        }

        override fun getItemViewType(position: Int): Int {
            return when {
                isInHead(position) -> headIds[position]
                isInFoot(position) -> {
                    var realPosition = position - headerViewList.size()
                    if (null != adapter) {
                        realPosition -= adapter.itemCount
                    }
                    footIds[realPosition]
                }
                null != adapter -> adapter.getItemViewType(position - headerViewList.size())
                else -> - 1
            }
        }

        override fun registerAdapterDataObserver(observer: AdapterDataObserver?) {
            adapter?.registerAdapterDataObserver(observer)
        }

        override fun onViewAttachedToWindow(holder: ViewHolder?) {
            adapter?.onViewAttachedToWindow(holder as VH)
        }

        inner class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }


    /**
     * [RecyclerView.Adapter]的[AdapterDataObserver],
     * 将[RecyclerView.Adapter]的刷新交给[ListLikeMaskAdapter]来处理
     * */
    inner class DataObserver : AdapterDataObserver() {

        private fun checkIsEmpty() {
            emptyView?.let {
                val emptyCount = headerViewList.size() + footerViewList.size()
                if (maskAdapter?.itemCount == emptyCount) {
                    emptyView?.visibility = View.VISIBLE
                    this@ListLikeRecyclerView.visibility = View.GONE
                } else {
                    emptyView?.visibility = View.GONE
                    this@ListLikeRecyclerView.visibility = View.VISIBLE
                }
            }
        }

        override fun onChanged() {
            if (null != maskAdapter) {
                maskAdapter?.notifyDataSetChanged()

                checkIsEmpty()
            }
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            maskAdapter?.notifyItemRangeRemoved(positionStart + headerViewList.size(), itemCount)

            checkIsEmpty()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            maskAdapter?.notifyItemRangeRemoved(fromPosition + headerViewList.size(), itemCount)

            checkIsEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            maskAdapter?.notifyItemRangeInserted(positionStart + headerViewList.size(), itemCount)

            checkIsEmpty()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            maskAdapter?.notifyItemRangeChanged(positionStart + headerViewList.size(), itemCount)

            checkIsEmpty()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            maskAdapter?.notifyItemRangeChanged(positionStart + headerViewList.size(), itemCount, payload)

            checkIsEmpty()
        }
    }


    /** ------------------------------------ simple class declare ---------------------------------------------*/
    /** [OnRecyclerViewLoadCallback]的空实现类 */
    internal open class SimpleLoadCallback : OnRecyclerViewLoadCallback {
        @CallSuper
        override fun onHeadRefresh() {

        }

        @CallSuper
        override fun onFootLoad() {
        }
    }


    /** [OnItemClickListener]的空实现类 */
    internal open class SimpleItemClickListener : OnItemClickListener {
        @CallSuper
        override fun onItemClick(childView: View, itemPosition: Int) {
        }

        override fun onItemLongClick(childView: View, position: Int) {
            onItemClick(childView, position)
        }
    }


    /** ---------------------------------------- interface declare ---------------------------------------------*/
    /** [ListLikeRecyclerView]刷新回调*/
    public interface OnRecyclerViewLoadCallback {
        fun onHeadRefresh()

        fun onFootLoad()
    }


    /** [ListLikeRecyclerView]点击事件*/
    public interface OnItemClickListener {
        fun onItemClick(childView: View, itemPosition: Int)

        fun onItemLongClick(childView: View, position: Int)
    }
}