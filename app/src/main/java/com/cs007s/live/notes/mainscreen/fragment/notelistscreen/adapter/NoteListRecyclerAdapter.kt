package com.cs007s.live.notes.mainscreen.fragment.notelistscreen.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.cs007s.live.notes.R
import com.cs007s.live.notes.mainscreen.fragment.notelistscreen.data.NoteModel
import kotlinx.android.synthetic.main.item_note.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Chitransh Shrivastav on 10/11/2020 for Notes
 */
class NoteListRecyclerAdapter (private val noteList: ArrayList<NoteModel>,
                               private val listener: OnItemClickListener) :
    RecyclerView.Adapter<NoteListRecyclerAdapter.NoteListViewHolder>() {

    private var filteredNoteList = ArrayList<NoteModel>()

    init {
        filteredNoteList = noteList
    }

    interface OnItemClickListener {
        fun onTap(callType: String, position: Int, noteModel: NoteModel)
        fun onResultUpdate(listSize: Int)
    }

    inner class NoteListViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        fun bind(noteModel: NoteModel, position: Int) {

            itemView.item_note_title.text = noteModel.title
            itemView.item_note_content.text = noteModel.content
            itemView.item_note_date.text = noteModel.date

            if (noteModel.isPinned) {
                itemView.item_note_pin.visibility = View.VISIBLE
                itemView.item_note_pin_helper_view.visibility = View.VISIBLE

                itemView.item_note_pin_helper_view.setOnClickListener{
                    listener.onTap("un_pin", position, noteModel)
                }

            } else{
                itemView.item_note_pin.visibility = View.GONE
                itemView.item_note_pin_helper_view.visibility = View.GONE
            }

            itemView.item_note_view_group.setCardBackgroundColor(noteModel.colorCode)

            itemView.setOnClickListener {
                listener.onTap("view", position, noteModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteListViewHolder, position: Int) {
        holder.bind(filteredNoteList[position], position)
    }

    fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    filteredNoteList = noteList
                } else {
                    val filteredList: ArrayList<NoteModel> = ArrayList()

                    for (row in noteList) {
                        if (row.title.toLowerCase().contains(charString.toLowerCase())
                            || row.content.toLowerCase().contains(charString.toLowerCase())
                            || row.date.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row)
                        }
                    }

                    filteredNoteList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredNoteList
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                filteredNoteList = filterResults.values as ArrayList<NoteModel>
                notifyDataSetChanged()

                listener.onResultUpdate(filteredNoteList.size)
            }
        }
    }

    override fun getItemCount(): Int {
        return filteredNoteList.size
    }
}