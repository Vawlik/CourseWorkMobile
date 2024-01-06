package com.example.newkursach

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.newkursach.databinding.RecyclerItemBinding
import java.text.SimpleDateFormat
import java.util.Date

class AudioAdapter(var records: ArrayList<AudioRecord>, var listener: OnItemClickListener) :
    RecyclerView.Adapter<AudioAdapter.CardViewHolder>() {
    private var editMode = false
    fun isEditMode(): Boolean {
        return editMode
    }

    fun setEditMode(mode: Boolean) {
        if (editMode != mode) {
            editMode = mode
            notifyDataSetChanged()
        }
    }

    inner class CardViewHolder(binding: RecyclerItemBinding) : ViewHolder(binding.root),
        View.OnClickListener, View.OnLongClickListener {
        val fileNameText: TextView = binding.filename
        val metaText: TextView = binding.meta
        val checkBoxText: CheckBox = binding.checkbox
        val shareButton: ImageView = binding.sharebut

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            shareButton.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            var position = adapterPosition
            if (position != RecyclerView.NO_POSITION)
                listener.onItemClickListener(position)
        }

        override fun onLongClick(v: View?): Boolean {
            var position = adapterPosition
            if (position != RecyclerView.NO_POSITION)
                listener.onItemLongClickListener(position)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding =
            RecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            var record = records[position]
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            var date = Date(record.timestamp)
            var strDate = dateFormat.format(date)
            holder.fileNameText.text = record.filename
            holder.metaText.text = "${record.duration} $strDate"

            holder.shareButton.setOnClickListener {
                listener.onShareClickListener(position)
            }

            if (editMode) {
                holder.checkBoxText.visibility = View.VISIBLE
                holder.checkBoxText.isChecked = record.isChecked
            } else {
                holder.checkBoxText.visibility = View.GONE
                holder.checkBoxText.isChecked = false
            }
        }
    }

}