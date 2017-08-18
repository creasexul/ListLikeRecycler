package com.bammatrip.listlikerecycler

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.bammatrip.listlikerecycler.databinding.ActivityMainBinding
import com.bammatrip.listlikerecyclerview.view.ListLikeRecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState : Bundle?) {
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
            override fun onItemClick(childView : View, position : Int) {
                Toast.makeText(this@MainActivity, position.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onItemLongClick(childView : View, position : Int) {

            }

        })

    }

}
