package com.github.alexmelyon.master_charlist.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.alexmelyon.master_charlist.R
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.textColor

class RecyclerStringAdapter<T>(val context: Context, @LayoutRes val layoutRes: Int = android.R.layout.simple_list_item_1, val onItemClickListener: (Int, T) -> Unit = { pos, item -> }) : RecyclerView.Adapter<RecyclerStringAdapter.ViewHolder>() {

    var layoutManager: RecyclerView.LayoutManager? = null
    var onItemLongclickListener: (Int, T) -> Unit = { pos, item -> }
    var onGetDescriptionValue: ((Int) -> String)? = null
    var onGetHeaderValue: ((Int) -> Int)? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
    }

    var items: MutableList<T> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(layoutRes, parent, false)
        v.layoutParams.width = matchParent
        val vh = ViewHolder(v)
        vh.text1.textColor = Color.BLACK
        vh.text2?.textColor = Color.GRAY
        return vh
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val correctPosition = holder.adapterPosition
        val text1 = holder.text1
        text1.text = items[position].toString()
        holder.itemView.setOnClickListener { view ->
            val correctPosition = holder.adapterPosition
            onItemClickListener(correctPosition, items[correctPosition])
        }
        holder.itemView.setOnLongClickListener { view ->
            val correctPosition = holder.adapterPosition
            onItemLongclickListener(correctPosition, items[correctPosition])
            return@setOnLongClickListener true
        }
        if (onGetDescriptionValue != null) {
            holder.text2.text = onGetDescriptionValue?.invoke(correctPosition)
        }
        if(onGetHeaderValue != null) {
            val header = onGetHeaderValue!!.invoke(correctPosition).let { context.getString(it) }
            if(header.isNotEmpty()) {
                holder.headerView?.visibility = View.VISIBLE
                holder.headerText?.text = header
            } else {
                holder.headerView?.visibility = View.GONE
                holder.headerText?.text = ""
            }
        }
    }

    fun itemAddedAt(pos: Int, name: T) {
        items.add(pos, name)
        notifyItemInserted(pos)

        layoutManager?.scrollToPosition(0)
    }

    fun itemRemovedAt(pos: Int) {
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val text2 = view.findViewById<TextView>(android.R.id.text2)
        val headerView = view.findViewById<FrameLayout>(R.id.header_view)
        val headerText = view.findViewById<TextView>(R.id.header_text)
    }
}