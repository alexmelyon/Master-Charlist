package com.github.alexmelyon.master_charlist.list_characters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.alexmelyon.master_charlist.R
import org.jetbrains.anko.*

class ListCharactersAdapter(val context: Context, val onLongclickListener: (Int, CharacterItem) -> Unit) : RecyclerView.Adapter<ListCharactersAdapter.ViewHolder>() {

    var items = mutableListOf<CharacterItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = parent.context.linearLayout {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)

            frameLayout {
                layoutParams = ViewGroup.LayoutParams(matchParent, dip(50))
                backgroundColor = Color.parseColor("#EEEEEE")
                textView(context.getString(R.string.character_name)) {
                    id = R.id.character_name
                    textSize = 20F
                    textColor = Color.parseColor("#555555")
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.BOTTOM
                    setPadding(dip(8), 0, 0, dip(8))
                }
            }
            linearLayout {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 8, 20, 8)

                textView {
                    id = R.id.character_hp
                    textColor = Color.BLACK
                }
                textView(context.getString(R.string.effects_colon)) {
                    typeface = Typeface.DEFAULT_BOLD
                    textColor = Color.BLACK
                }
                textView {
                    id = R.id.character_effects
                    textColor = Color.BLACK
                }
                textView(context.getString(R.string.skills_colon)) {
                    typeface = Typeface.DEFAULT_BOLD
                    textColor = Color.BLACK
                }
                textView {
                    id = R.id.character_skills
                    textColor = Color.BLACK
                }
                textView(context.getString(R.string.things_colon)) {
                    typeface = Typeface.DEFAULT_BOLD
                    textColor = Color.BLACK
                }
                textView {
                    id = R.id.character_things
                    textColor = Color.BLACK
                }
            }
        }
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = items[position].character.name
        holder.hp.text = "HP " + items[position].hp
        holder.effects.text = items[position].effects.joinToString(", ")
        holder.skills.text = items[position].skills.joinToString("\n")
        holder.things.text = items[position].things.map { "${it.first}: ${it.second}" }.joinToString("\n")
        holder.itemView.setOnLongClickListener {
            val correctPosition = holder.adapterPosition
            onLongclickListener(correctPosition, items[position])
            return@setOnLongClickListener true
        }
    }

    fun adddedAt(index: Int, item: CharacterItem) {
        items.add(index, item)
        notifyItemInserted(index)
    }

    fun removedAt(pos: Int) {
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.character_name)
        val hp = view.findViewById<TextView>(R.id.character_hp)
        val effects = view.findViewById<TextView>(R.id.character_effects)
        val skills = view.findViewById<TextView>(R.id.character_skills)
        val things = view.findViewById<TextView>(R.id.character_things)
    }
}