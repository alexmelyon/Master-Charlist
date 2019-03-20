package com.helloandroid.ui

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.textColor

class RecyclerStringAdapter<T>(val context: Context, val onItemClickListener: (Int) -> Unit = { pos -> }) : RecyclerView.Adapter<RecyclerStringAdapter.ViewHolder>() {

    var onItemLongclickListener: (Int, T) -> Unit = { pos, item -> }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    var items: MutableList<T> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        v.findViewById<TextView>(android.R.id.text1).textColor = Color.BLACK
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val text1 = holder.text1
        text1.text = items[position].toString()
        text1.setOnClickListener { view ->
            val correctPosition = holder.adapterPosition
            onItemClickListener(correctPosition)
        }
        text1.setOnLongClickListener { view ->
            val correctPosition = holder.adapterPosition
            onItemLongclickListener(correctPosition, items[position])
            return@setOnLongClickListener true
        }
    }

    fun itemAddedAt(pos: Int, name: T) {
        items.add(pos, name)
        notifyItemInserted(pos)
    }

    fun itemRemovedAt(pos: Int) {
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text1 = view.findViewById<TextView>(android.R.id.text1)
//        val text2 = view.findViewById<TextView>(android.R.id.text2)
    }
}