package com.helloandroid.list_effects

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.helloandroid.R
import com.helloandroid.room.Effect
import com.helloandroid.room.Skill

class ListEffectsAdapter : RecyclerView.Adapter<ListEffectsAdapter.ViewHolder>() {

    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var items: MutableList<EffectRow>
    var onItemClickListener: (Int, EffectRow) -> Unit = { pos, item -> }
    var onItemLongclickListener: (Int, EffectRow) -> Unit = { pos, item -> }
    var onSubitemPlus = { pos: Int, effect: Effect, skill: Skill -> }
    var onSubitemMinus = { pos: Int, effect: Effect, skill: Skill -> }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.layoutManager = layoutManager
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_effects_item, parent, false)
        val holder = ViewHolder(view)
        return holder
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.effectName.text = items[position].name

        val effectSkills = items[position].effectSkills
        holder.attachedSkills.removeAllViews()
        val inflater = LayoutInflater.from(holder.itemView.context)
        for ((subPos, es) in effectSkills.withIndex()) {
            val row = inflater.inflate(R.layout.session_effect_skill, null)
            val title = row.findViewById<TextView>(R.id.title)
            title.text = es.name

            val value = row.findViewById<TextView>(R.id.value)
            value.text = "%+d".format(es.value)

            val minus = row.findViewById<Button>(R.id.minusButton)
            minus.setOnClickListener { v ->
                val correctPos = holder.adapterPosition
                val effect = items[correctPos].effect
                val skill = items[correctPos].effectSkills[subPos].skill
                onSubitemMinus(correctPos, effect, skill)
            }

            val plus = row.findViewById<Button>(R.id.plusButton)
            plus.setOnClickListener { v ->
                val correctPos = holder.adapterPosition
                val correctPosition = holder.adapterPosition
                val effect = items[correctPosition].effect
                val skill = items[correctPosition].effectSkills[subPos].skill
                onSubitemPlus(correctPos, effect, skill)
            }

            holder.attachedSkills.addView(row)
        }
        holder.itemView.setOnClickListener { v ->
            val correctPos = holder.adapterPosition
            onItemClickListener(correctPos, items[correctPos])
        }
        holder.itemView.setOnLongClickListener { v ->
            val correctPos = holder.adapterPosition
            onItemLongclickListener(correctPos, items[correctPos])
            return@setOnLongClickListener true
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val effectName = view.findViewById<TextView>(R.id.effect_name)
        val attachedSkills = view.findViewById<LinearLayout>(R.id.attached_skills)
    }
}