package com.mhss.app.mynotes.ui.recyclerview

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mhss.app.mynotes.database.Note
import com.mhss.app.mynotes.databinding.RecCustomItemBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteRecAdapter(
    private val onItemClicked: (Note) -> Unit)
    : ListAdapter<Note, NoteRecAdapter.NoteViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            RecCustomItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class NoteViewHolder(private var binding: RecCustomItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note){
            binding.apply {
                titleTv.text = note.title
                noteTv.text = note.note
                starIcRecItem.visibility = if (note.favorite) View.VISIBLE else View.GONE

                holderCard.setCardBackgroundColor(ContextCompat.getColor(itemView.context, note.color))
                holderCard.setOnClickListener { onItemClicked(note) }

                dateTv.text = getFormattedDate(note.date)

            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun getFormattedDate(date: Long): String{
        val sdf = SimpleDateFormat("MMM dd,yyyy", Locale.getDefault())
        val stf = SimpleDateFormat("h:mm a", Locale.getDefault())

        val calender = Calendar.getInstance()
        calender.timeInMillis = date
        return if (DateUtils.isToday(date)){
            stf.format(calender.time)
        }else
            sdf.format(calender.time)
    }
}