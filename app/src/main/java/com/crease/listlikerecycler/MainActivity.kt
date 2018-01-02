package com.crease.listlikerecycler

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.crease.listlikerecycler.databinding.ActivityMainBinding
import com.crease.listlikerecyclerview.touch.ItemTouchCallback
import com.crease.listlikerecyclerview.touch.ListItemTouchHelper
import com.crease.listlikerecyclerview.view.ListLikeRecyclerView
import java.util.*

class MainActivity : AppCompatActivity() {

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

        mainBinding.recyclerView.loadCallback = object : ListLikeRecyclerView
                                                         .OnRecyclerViewLoadCallback {
            override fun onHeadRefresh() {
                mainBinding.root.postDelayed({
                    adapter.setItemList(stringList)
                    Toast.makeText(this@MainActivity, "Head refreshing finished", Toast
                            .LENGTH_LONG).show()
                    mainBinding.recyclerView.isRefreshing = false
                }, 1000)
            }

            override fun onFootLoad() {
                mainBinding.root.postDelayed({
                    Toast.makeText(this@MainActivity, "Foot loading finished", Toast.LENGTH_LONG)
                            .show()
                    mainBinding.recyclerView.noMore = true
                }, 1000)
            }
        }

        val callbackNew = object : ItemTouchCallback() {
            override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder, position: Int, direction: Int) {
                stringList.removeAt(position)

                adapter.removeItem(position)
            }

            override fun onItemCanMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                       position: Int, targetViewHolder: RecyclerView.ViewHolder,
                                       targetPosition: Int) = true

            override fun onItemMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, position: Int, targetViewHolder: RecyclerView.ViewHolder, targetPosition: Int, x: Int, y: Int) {
                if (position < targetPosition) {
                    for (i in position until targetPosition) {
                        Collections.swap(stringList, i, i + 1)
                    }
                } else {
                    for (i in position downTo (targetPosition + 1)) {
                        Collections.swap(stringList, i, i - 1)
                    }
                }

                adapter.moveItem(position, targetPosition)
            }

        }

        val touchHelper = ListItemTouchHelper(callbackNew)
        touchHelper.attachToRecyclerView(mainBinding.recyclerView)

        mainBinding.recyclerView.setOnItemClickListener(object : ListLikeRecyclerView.OnItemClickListener {
            override fun onItemClick(childView: View, itemPosition: Int) {
                Toast.makeText(this@MainActivity, "Click:$itemPosition", Toast.LENGTH_LONG).show()
            }

            override fun onItemLongClick(childView: View, position: Int) {
                Toast.makeText(this@MainActivity, "LongClick:$position", Toast.LENGTH_LONG).show()
            }
        })

        val layoutInflater = LayoutInflater.from(this)


        mainBinding.btnAdd.setOnClickListener {
            mainBinding.recyclerView.addHeaderView(R.layout.layout_head, layoutInflater.inflate(R
                    .layout.layout_head, mainBinding.recyclerView, false), 1)
            mainBinding.recyclerView.addHeaderView(R.layout.layout_head_2, layoutInflater.inflate(R
                    .layout.layout_head_2, mainBinding.recyclerView, false), 1)
        }

        mainBinding.btnRemove.setOnClickListener {
            mainBinding.recyclerView.removeHeaderView(R.layout.layout_head)
            mainBinding.recyclerView.removeHeaderView(R.layout.layout_head_2)
        }
    }

}
