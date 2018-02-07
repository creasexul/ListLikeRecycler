@file:Suppress("unused", "MemberVisibilityCanPrivate")

package com.crease.listlikerecyclerview.view

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.CallSuper
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.*
import com.crease.listlikerecyclerview.ListLikeRecyclerViewClient
import com.crease.listlikerecyclerview.R


/**
 * [RecyclerView]派生类
 * 实现Item点击事件，添加去除headerView, footerView, 下拉刷新上拉加载
 *
 * @author Crease
 * @version 1.0
 * */
class ListLikeRecyclerView @JvmOverloads constructor(context: Context,
                                                     attrs: AttributeSet? = null,
                                                     defStyle: Int = 0)
    : RecyclerView(context, attrs, defStyle) {

    companion object {
        private const val TAG = "ListLikeRecyclerView"

        private val DEFAULT_ELASTIC = 3

        private const val DEFAULT_LOAD_MORE_OFFSET = 2

    }

    /** ---------------------------------------- private val ---------------------------------------------*/
    private val dataObserver = DataObserver()
    /** ---------------------------------------- private var ---------------------------------------------*/
    private var lastY = 0f

    /** [AppBarLayout]状态，解决滑动冲突 */
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

    /** ---------------------------------------- public var ---------------------------------------------*/
    val headViewSize: Int
        get() = headerViewList.size()

    val footViewSize: Int
        get() = footerViewList.size()

    var isRefreshing = false
        set(value) {
            if (field != value) {
                if (value) {
                    headRefreshView.setState(HeadRefreshView.STATE_REFRESH)
                    loadCallback?.onHeadRefresh()
                } else {
                    headRefreshView.setState(HeadRefreshView.STATE_DONE)
                    preRefreshing = false
                }
                field = value
            }
        }

    var isLoading = false
        set(value) {
            if (field != value) {
                if (! value && ! noMore) {
                    footLoadView.setState(FootLoadView.STATE_LOAD_FINISHED)
                } else if (value) {
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

    /** 是否阻断由[ListLikeRecyclerView]处理滑动事件，即是否允许[HeadRefreshView]响应滑动事件*/
    var isInterruptTouchEvent = false

    var refreshEnabled = false
        set(value) {
            field = value
            if (value) {
                headIds.add(0, headRefreshView.layoutId)
                headerViewList.put(headRefreshView.layoutId, headRefreshView)
                maskAdapter?.notifyItemInserted(0)
            } else {
                headIds.remove(headRefreshView.layoutId)
                headerViewList.remove(headRefreshView.layoutId)
                maskAdapter?.notifyItemRemoved(0)
            }
        }

    var loadingEnabled = false
        set(value) {
            field = value
            if (value) {
                footIds.add(footLoadView.layoutId)
                footerViewList.put(footLoadView.layoutId, footLoadView)
                footLoadView.setState(FootLoadView.STATE_NO_MORE)
                maskAdapter?.let { it.notifyItemInserted(it.itemCount) }
            } else {
                footIds.remove(footLoadView.layoutId)
                footerViewList.remove(footLoadView.layoutId)
                maskAdapter?.let { it.notifyItemRemoved(it.itemCount) }
            }
        }

    /** [HeadRefreshView]是否出现 */
    var preRefreshing = false

    var headRefreshView: HeadRefreshView = ListLikeRecyclerViewClient.headRefreshView
            .getConstructor(Context::class.java).newInstance(context)
        set(value) {
            val index = headIds.indexOf(field.layoutId)
            if (index >= 0) {
                headIds.removeAt(index)
                headerViewList.remove(field.layoutId)
                headIds.add(index, value.layoutId)
                headerViewList.put(value.layoutId, value)

                maskAdapter?.notifyItemChanged(index)
            }
            field = value
        }

    var footLoadView: FootLoadView = ListLikeRecyclerViewClient.footLoadView
            .getConstructor(Context::class.java).newInstance(context)
        set(value) {
            val index = footIds.indexOf(field.layoutId)
            if (index >= 0) {
                footIds.removeAt(index)
                footerViewList.remove(field.layoutId)
                footIds.add(index, value.layoutId)
                footerViewList.put(value.layoutId, value)

                maskAdapter?.let {
                    it.notifyItemChanged(it.itemCount - 1)
                }
            }
            field = value
        }

    var loadCallback: OnRecyclerViewLoadCallback? = null

    /** 上滑加载提前量 */
    var loadOffset: Int = DEFAULT_LOAD_MORE_OFFSET

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
            val layoutManager = layoutManager
            lastPosition = when (layoutManager) {
                is GridLayoutManager -> layoutManager.findLastVisibleItemPosition()
                is StaggeredGridLayoutManager -> {
                    val lastSpan = IntArray(layoutManager.spanCount)
                    layoutManager.findLastVisibleItemPositions(lastSpan)
                    lastSpan.max() ?: 0
                }
                else -> (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            }

            if (layoutManager.childCount > 0 && lastPosition >= layoutManager.itemCount - loadOffset &&
                    layoutManager.itemCount > layoutManager.childCount && ! noMore) {

                isLoading = true
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {

        if (! refreshEnabled || (refreshEnabled && isRefreshing) || isInterruptTouchEvent) {
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
                if (offset < 0 && headRefreshView.layoutParams.height > 0 || offset > 0) {

                    if (isTop() && refreshEnabled && appBarState == AppBarStateChangeListener.EXPANDED) {

                        if (layoutManager.canScrollHorizontally()) {
                            headRefreshView.move(offset / DEFAULT_ELASTIC, 0f)
                            preRefreshing = headRefreshView.layoutParams.height > 0 && headRefreshView.state < HeadRefreshView.STATE_REFRESH
                            if (preRefreshing) {
                                return true
                            }
                        } else {
                            headRefreshView.move(0f, offset / DEFAULT_ELASTIC)
                            preRefreshing = headRefreshView.layoutParams.height > 0 && headRefreshView.state < HeadRefreshView.STATE_REFRESH
                            if (preRefreshing) {
                                return true
                            }
                        }
                    }
                }
            }

            else -> {
                lastY = - 1f
                if (isTop() && refreshEnabled && appBarState == AppBarStateChangeListener.EXPANDED && headRefreshView.isPreLoading) {
                    isRefreshing = true
                }
            }
        }
        return super.onTouchEvent(e)
    }

    /** [AppBarLayout]状态监听*/
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        var appBarLayout: AppBarLayout? = null
        var p: ViewParent? = parent
        while (p != null) {
            if (p is CoordinatorLayout) {
                break
            }
            p = p.parent
        }
        if (p is CoordinatorLayout) {
            val coordinatorLayout = p as CoordinatorLayout?
            val childCount = coordinatorLayout !!.childCount
            for (i in childCount - 1 downTo 0) {
                val child = coordinatorLayout.getChildAt(i)
                if (child is AppBarLayout) {
                    appBarLayout = child
                    break
                }
            }
            if (appBarLayout != null) {
                appBarLayout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
                    override fun onStateChanged(appBarLayout: AppBarLayout, @AppBarState state: Long) {
                        appBarState = state
                    }
                })
            }
        }
    }


    /** ---------------------------------------- public fun declare ---------------------------------------------*/

    fun addHeaderView(id: Int, headerView: View, position: Int = - 1) {

        val index = headIds.indexOf(id)
        val isContain = index > 0

        if (isContain) {
            headIds.remove(id)
            maskAdapter?.notifyItemRemoved(index)
        }

        val realPosition = when {
            position < 0 || position >= headIds.size -> {
                if (isContain) index else headIds.size
            }

            else -> position + (if (refreshEnabled) 1 else 0)
        }

        headerViewList.put(id, headerView)
        headIds.add(realPosition, id)

        maskAdapter?.notifyItemInserted(realPosition)
    }

    fun addFooterView(id: Int, footerView: View, position: Int = - 1) {

        val index = footIds.indexOf(id)
        val isContain = index > 0

        if (isContain) {
            footIds.remove(id)
            maskAdapter?.notifyItemRemoved(footIndex(index))
        }

        val realPosition = when {
            position < 0 || position >= footIds.size - 1 -> {
                if (isContain) index else footIds.size
            }

            else -> position
        }

        footerViewList.put(id, footerView)
        footIds.add(realPosition, id)

        maskAdapter?.notifyItemInserted(footIndex(realPosition))
    }

    fun removeHeaderView(id: Int) {
        val index = headIds.indexOf(id)
        if (index > 0) {

            headIds.remove(id)
            headerViewList.remove(id)

            maskAdapter?.notifyItemRemoved(index)
        }
    }

    fun removeFooterView(id: Int) {
        val index = footIds.indexOf(id)
        if (index > 0) {

            footIds.remove(id)
            footerViewList.remove(id)

            //通过真正adapter刷新达到尾部动态删除效果
            maskAdapter?.notifyItemRemoved(footIndex(index))
        }

    }

    fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
        gestureDetector ?: let { addOnItemTouchListener(itemTouchListener) }

        gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val childView = findChildViewUnder(e.x, e.y)
                return if (! preRefreshing && null != childView && ! isInterruptTouchEvent) {
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
                if (! preRefreshing && null != childView && ! isInterruptTouchEvent) {

                    Log.d(TAG, "LongClick")

                    val itemPosition = getChildLayoutPosition(childView)
                    if (itemPosition in headerViewList.size() until (maskAdapter?.itemCount ?: 0)
                            - footerViewList.size()) {
                        itemClickListener.onItemLongClick(childView, getChildLayoutPosition(childView) - headerViewList.size())
                    }
                }

            }
        })
    }

    fun isHeader(viewType: Int) = headIds.contains(viewType)
    fun isFooter(viewType: Int) = footIds.contains(viewType)


    /** ---------------------------------------- private fun declare ---------------------------------------------*/
    private fun isTop(): Boolean {
        return headRefreshView.parent != null
    }

    private fun isInHead(position: Int): Boolean {
        return position < headerViewList.size() && position >= 0
    }

    private fun isInFoot(position: Int): Boolean {

        val itemCount = (maskAdapter?.itemCount ?: 0) - footerViewList.size() - 1
        return position > itemCount
    }

    private fun footIndex(index: Int) = index + headIds.size +
            (maskAdapter?.adapter?.itemCount ?: 0)


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
                adapter?.let {
                    if (it.itemCount > realPosition)
                        it.onBindViewHolder(holder as VH, realPosition)
                }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int, payloads: MutableList<Any>?) {
            if (isInHead(position) || isInFoot(position)) {
                return
            }

            payloads?.let {
                val realPosition = position - headerViewList.size()
                if (adapter?.itemCount ?: 0 > realPosition) {
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
            val layoutManager = layoutManager
            if (layoutManager is GridLayoutManager) {
                layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (isInHead(position) || isInFoot(position)) {
                            layoutManager.spanCount
                        } else {
                            1
                        }
                    }
                }
            }

            adapter?.onAttachedToRecyclerView(recyclerView)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
            adapter?.onDetachedFromRecyclerView(recyclerView)
        }

        override fun onViewRecycled(holder: ViewHolder?) {
            holder?.let {
                adapter?.onViewRecycled(it as VH)
            }
        }

        override fun onFailedToRecycleView(holder: ViewHolder?): Boolean {
            return if (null == holder)
                false
            else
                adapter?.onFailedToRecycleView(holder as VH) ?: false
        }

        override fun getItemId(position: Int): Long {
            if (null != adapter && ! isInHead(position) && ! isInFoot(position)) {
                val realPosition = position - headerViewList.size()
                if (realPosition < adapter.itemCount) {
                    return adapter.getItemId(realPosition)
                }
            }

            return - 1L
        }

        override fun setHasStableIds(hasStableIds: Boolean) {
            adapter?.setHasStableIds(hasStableIds)
        }

        override fun onViewAttachedToWindow(holder: ViewHolder?) {
            holder?.let {
                val lp = holder.itemView.layoutParams
                if (lp != null
                        && lp is StaggeredGridLayoutManager.LayoutParams
                        && (isInHead(holder.layoutPosition) || isInFoot(holder.layoutPosition))) {
                    lp.isFullSpan = true
                }
                if (holder is ListLikeMaskAdapter<*>.SimpleViewHolder) {
                    super.onViewAttachedToWindow(holder)
                } else {
                    adapter?.onViewAttachedToWindow(it as VH)
                }
            }
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder?) {
            holder?.let {
                if (holder is ListLikeMaskAdapter<*>.SimpleViewHolder) {
                    super.onViewDetachedFromWindow(holder)
                } else {
                    adapter?.onViewDetachedFromWindow(holder as VH)
                }
            }
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
                else -> 0
            }
        }

        inner class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }


    /**
     * [RecyclerView.Adapter]的[RecyclerView.AdapterDataObserver],
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

            // TODO improve this
            // because if RecyclerView isn't full, FooterView will stay the refreshing state
            postDelayed({
                val layoutManager = layoutManager
                if (layoutManager.itemCount > 0 && layoutManager.childCount >= layoutManager.itemCount
                        && loadingEnabled) {
                    isLoading = true
                }
            }, 32L)

        }

        override fun onChanged() {
            maskAdapter?.notifyDataSetChanged()

            checkIsEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            maskAdapter?.notifyItemRangeRemoved(positionStart + headerViewList.size(),
                    itemCount)

            checkIsEmpty()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            for (i in 0 until itemCount) {

                Log.d(TAG, "fromPosition=$fromPosition, toPosition=$toPosition, i=$i, " +
                        "headerViewSize=$headViewSize")

                maskAdapter?.notifyItemMoved(fromPosition + headerViewList.size() + i,
                        toPosition + headerViewList.size() + i)
            }

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
    open class SimpleLoadCallback : OnRecyclerViewLoadCallback {
        @CallSuper
        override fun onHeadRefresh() {

        }

        @CallSuper
        override fun onFootLoad() {
        }
    }


    /** [OnItemClickListener]的空实现类 */
    open class SimpleItemClickListener : OnItemClickListener {
        @CallSuper
        override fun onItemClick(childView: View, itemPosition: Int) {
        }

        override fun onItemLongClick(childView: View, position: Int) {
            onItemClick(childView, position)
        }
    }


    /** ---------------------------------------- interface declare ---------------------------------------------*/
    /** [ListLikeRecyclerView]刷新回调*/
    interface OnRecyclerViewLoadCallback {
        fun onHeadRefresh()

        fun onFootLoad()
    }


    /** [ListLikeRecyclerView]点击事件*/
    interface OnItemClickListener {
        fun onItemClick(childView: View, itemPosition: Int)

        fun onItemLongClick(childView: View, position: Int)
    }
}