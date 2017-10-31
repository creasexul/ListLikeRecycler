package com.crease.listlikerecycler

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.crease.listlikerecycler.databinding.ActivityMainBinding
import com.crease.listlikerecyclerview.view.ListLikeRecyclerView

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
        mainBinding.recyclerView.setOnItemClickListener(object : ListLikeRecyclerView.OnItemClickListener {
            override fun onItemClick(childView: View, itemPosition: Int) {
                Toast.makeText(this@MainActivity, itemPosition.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onItemLongClick(childView: View, position: Int) {

            }
        })

        mainBinding.recyclerView.loadCallback = object : ListLikeRecyclerView
                                                         .OnRecyclerViewLoadCallback {
            override fun onHeadRefresh() {
                mainBinding.root.postDelayed({
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

        mainBinding.btnAdd.setOnClickListener {
            mainBinding.recyclerView.addHeaderView(R.layout.layout_head,
                    LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_head,
                            mainBinding.root as LinearLayout, false))
        }

        mainBinding.btnRemove.setOnClickListener {
            mainBinding.recyclerView.removeHeaderView(R.layout.layout_head)
        }

        mainBinding.recyclerView.isRefreshing = true

    }

}
