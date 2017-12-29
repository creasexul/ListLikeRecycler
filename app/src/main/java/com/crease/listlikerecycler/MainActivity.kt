package com.crease.listlikerecycler

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.crease.listlikerecycler.databinding.ActivityMainBinding
import com.crease.listlikerecyclerview.view.ListLikeRecyclerView
import java.util.*

class MainActivity : AppCompatActivity() {

    var dragFlag: Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN
    var swipeFlag: Int = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainBinding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        val stringList = mutableListOf<StringTest>()
        (1 .. 30).forEach {
            val stringTest = StringTest()
            stringTest.content = it.toString()
            stringList.add(stringTest)
        }

        val adapter = BaseRecyclerViewAdapter()
        adapter.setItemList(stringList)

        mainBinding.recyclerView.adapter = adapter
        mainBinding.recyclerView.setOnItemClickListener(object : ListLikeRecyclerView.OnItemClickListener {
            override fun onItemClick(childView: View, itemPosition: Int) {
                Toast.makeText(this@MainActivity, "Click:$itemPosition", Toast.LENGTH_LONG).show()
            }

            override fun onItemLongClick(childView: View, position: Int) {
                Toast.makeText(this@MainActivity, "LongClick:$position", Toast.LENGTH_LONG).show()
            }
        })

        mainBinding.recyclerView.loadCallback = object : ListLikeRecyclerView
                                                         .OnRecyclerViewLoadCallback {
            override fun onHeadRefresh() {
//                mainBinding.root.postDelayed({
//                    adapter.setItemList(stringList)
//                    Toast.makeText(this@MainActivity, "Head refreshing finished", Toast
//                            .LENGTH_LONG).show()
//                    mainBinding.recyclerView.isRefreshing = false
//                }, 1000)
            }

            override fun onFootLoad() {
                mainBinding.root.postDelayed({
                    Toast.makeText(this@MainActivity, "Foot loading finished", Toast.LENGTH_LONG)
                            .show()
                    mainBinding.recyclerView.noMore = true
                }, 1000)
            }
        }
//
//        mainBinding.btnAdd.setOnClickListener {
//            mainBinding.recyclerView.addHeaderView(R.layout.layout_head,
//                    LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_head,
//                            mainBinding.root as LinearLayout, false))
//        }
//
//        mainBinding.btnRemove.setOnClickListener {
//            mainBinding.recyclerView.removeHeaderView(R.layout.layout_head)
//        }
//
//        mainBinding.recyclerView.isRefreshing = true

        val callback = object : ItemTouchHelper.Callback() {
            override fun isLongPressDragEnabled(): Boolean = false

            override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder): Int {
                val position = viewHolder.adapterPosition
                return if (position > mainBinding.recyclerView.headViewSize) {
                    makeMovementFlags(dragFlag, swipeFlag)
                } else {
                    makeMovementFlags(0, 0)
                }
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

                val fromPos = viewHolder.adapterPosition - mainBinding.recyclerView.headViewSize
                val toPos = target.adapterPosition - mainBinding.recyclerView.headViewSize
//                val fromPos = viewHolder.adapterPosition
//                val toPos = target.adapterPosition

                Log.d("ListLikeRecycler", "fromPos : $fromPos  toPos : $toPos")

                if (inRange(fromPos) && inRange(toPos)) {
                    if (fromPos < toPos) {
                        for (i in fromPos until toPos) {
                            Log.d("ListLikeRecycler", i.toString())
                            Collections.swap(stringList, i, i + 1)
                        }
                    } else {
                        for (i in fromPos downTo (toPos + 1)) {
                            Log.d("ListLikeRecycler", i.toString())
                            Collections.swap(stringList, i, i - 1)
                        }
                    }

                    adapter.moveItem(fromPos, toPos)

                    return true

                } else {
                    return false
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val index = viewHolder.adapterPosition - mainBinding.recyclerView.headViewSize
//                val index = viewHolder.adapterPosition
                stringList.removeAt(index)

                adapter.notifyItemRemoved(index)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                mainBinding.recyclerView.isInterruptTouchEvent = viewHolder != null
            }
        }

        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(mainBinding.recyclerView)

        val gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {

            override fun onLongPress(e: MotionEvent) {
                if (! mainBinding.recyclerView.preRefreshing) {

                    val childView = mainBinding.recyclerView.findChildViewUnder(e.x, e.y)
                    if (null != childView) {
                        val itemPosition = mainBinding.recyclerView.getChildLayoutPosition(childView)

                        touchHelper.startDrag(mainBinding.recyclerView.getChildViewHolder(mainBinding
                                .recyclerView.getChildAt(itemPosition)))
                    }
                }

            }
        })

        mainBinding.recyclerView.addOnItemTouchListener(
                object : RecyclerView.SimpleOnItemTouchListener() {
                    override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
                        return gestureDetector.onTouchEvent(e)
                    }
                })
    }

    private fun inRange(int: Int): Boolean = int in 0 .. 31

}
