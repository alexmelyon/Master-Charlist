package com.helloandroid.session

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.helloandroid.R
import com.helloandroid.room.Effect
import kotlinx.android.synthetic.main.session_item_comment.view.*
import kotlinx.android.synthetic.main.session_item_int.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.sdk15.listeners.onClick
import org.jetbrains.anko.textColor

class SessionDiffsAdapter(val context: Context, val editable: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items = mutableListOf<SessionItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onItemPlus: (Int, SessionItemType) -> Unit = { pos, type -> }
    var onItemMinus: (Int, SessionItemType) -> Unit = { pos, type -> }
    var onCommentChanged: (Int, String) -> Unit = { pos, comment -> }
    var onItemClickListener = { pos: Int -> }
    var onItemLongClickListener = { pos: Int -> }
    var onSubitemPlus = { item: Int, subitem: Int -> }
    var onSubitemMinus = { item: Int, subitem: Int -> }

    private val textWatchers = mutableMapOf<EditText, IndexTextWatcher>()
    private var layoutManager: RecyclerView.LayoutManager? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val type = SessionItemType.values()[viewType]
        when (type) {
            SessionItemType.ITEM_HP,
            SessionItemType.ITEM_SKILL,
            SessionItemType.ITEM_THING -> {
                val view = parent.context.layoutInflater.inflate(R.layout.session_item_int, parent, false)
                return ItemIntViewHolder(view)
            }
            SessionItemType.ITEM_EFFECT -> {
//                val view = parent.context.layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                val view = parent.context.layoutInflater.inflate(R.layout.session_item_effect, parent, false)
                return ItemEffectViewHolder(view)
            }
            SessionItemType.ITEM_COMMENT -> {
                val view = parent.context.layoutInflater.inflate(R.layout.session_item_comment, parent, false)
                return ItemCommentViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = SessionItemType.values()[holder.itemViewType]
        when (type) {
            SessionItemType.ITEM_HP,
            SessionItemType.ITEM_SKILL,
            SessionItemType.ITEM_THING -> {
                holder as ItemIntViewHolder
                holder.title.text = items[position].title
                holder.desc.text = items[position].desc
                holder.value.text = items[position].value.toString()
                holder.minusButton.visibility = if (editable) View.VISIBLE else View.INVISIBLE
                holder.minusButton.onClick { view ->
                    val correctPosition = holder.adapterPosition
                    onItemMinus(items[correctPosition].index, type)
                }
                holder.plusButton.visibility = if (editable) View.VISIBLE else View.INVISIBLE
                holder.plusButton.onClick { view ->
                    val correctPosition = holder.adapterPosition
                    onItemPlus(items[correctPosition].index, type)
                }
            }
            SessionItemType.ITEM_EFFECT -> {
                val correctPosition = holder.adapterPosition
                holder as ItemEffectViewHolder
                holder.title.text = items[correctPosition].title
                holder.title.textColor = Color.BLACK
                if(items[correctPosition].value < 0) {
                    holder.title.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                }
                holder.desc.text = items[correctPosition].desc
                holder.desc.textColor = Color.GRAY
                holder.attachedSkills.visibility = if(items[correctPosition].effectSkills.isEmpty()) View.GONE else View.VISIBLE
                holder.attachedSkills.text = items[correctPosition].effectSkills.joinToString("\n")
                holder.itemView.setOnClickListener({ view ->
                    onItemClickListener(correctPosition)
                })
            }
            SessionItemType.ITEM_COMMENT -> {
                holder as ItemCommentViewHolder
                holder.editText.isEnabled = editable
                holder.editText.setText(items[position].comment, TextView.BufferType.EDITABLE)
                if (textWatchers[holder.editText] == null) {
                    val watcher = IndexTextWatcher() { index, comment ->
                        val correctPosition = holder.adapterPosition
                        onCommentChanged(correctPosition, comment)
                    }
                    textWatchers[holder.editText] = watcher
                    holder.editText.addTextChangedListener(watcher)
                }
                textWatchers[holder.editText]!!.index = position
            }
        }
    }

    fun itemAdded(pos: Int, sessionItem: SessionItem) {
        items.add(pos, sessionItem)
        notifyItemInserted(pos)
        layoutManager?.scrollToPosition(0)
    }

    class IndexTextWatcher(val textChanged: (Int, String) -> Unit) : TextWatcher {
        var index: Int = 0

        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            textChanged(index, s.toString())
        }
    }

    class ItemIntViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.title
        val desc = view.desc
        val value = view.value
        val minusButton = view.minusButton
        val plusButton = view.plusButton
    }

    class ItemEffectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.effect_title)
        val desc = view.findViewById<TextView>(R.id.effect_desc)
        val attachedSkills = view.findViewById<TextView>(R.id.attached_skills)
    }

    class ItemCommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val editText = view.editText
    }
}