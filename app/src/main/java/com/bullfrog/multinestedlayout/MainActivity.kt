package com.bullfrog.multinestedlayout

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.bullfrog.layoutmanagerdemo.data.Item
import com.bullfrog.multinestedlayout.adapter.RvAdapter
//import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.activity_main_multi_scroll.*
//import kotlinx.android.synthetic.main.activity_main_1.*


class MainActivity : AppCompatActivity() {

    private val adapter: RvAdapter by lazy {
        RvAdapter(Item.data)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_multi_scroll)
        window.statusBarColor = getColor(R.color.teal_200)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter

        rv.post {
            nsv.topHeight = vTop.height - 200
            nsv2.topHeight = tvSearch.height - 200

            Log.d("test", "rv height = ${rv.height}, nsv height = ${nsv.height}, nsv2 height = ${nsv2.height}," +
                    "tvRvTitle height = ${tvRvTitle.height}")
//
//            val params = rv.layoutParams as LinearLayout.LayoutParams
////            params.height = nsv.height - tvRvTitle.height
//            params.height = 2058
//            rv.layoutParams = params

        }
    }
}