package com.bullfrog.multinestedlayout.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bullfrog.layoutmanagerdemo.data.Item
import com.bullfrog.multinestedlayout.R
import kotlinx.android.synthetic.main.rv_item_layout.view.*

class RvAdapter(private val data: List<Item>) : RecyclerView.Adapter<RvViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_layout, parent, false)
        Log.d("RvAdapter", "onCreateViewHolder called")
        return RvViewHolder(view)
    }

    override fun onBindViewHolder(holder: RvViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item.text)
        Log.d("RvAdapter", "onBindViewHolder called")
    }

    override fun getItemCount(): Int {
        return data.size
    }

}

class RvViewHolder(
    view: View
) : RecyclerView.ViewHolder(view) {

    fun bind(text: String) {
        with(itemView) {
            tvItem.text = text
        }
    }
}